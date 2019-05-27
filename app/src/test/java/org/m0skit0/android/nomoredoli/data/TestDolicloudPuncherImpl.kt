package org.m0skit0.android.nomoredoli.data

import arrow.core.Either
import com.google.gson.Gson
import io.kotlintest.assertions.arrow.either.shouldBeLeftOfType
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.maps.shouldContain
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.m0skit0.android.nomoredoli.data.http.HTTPClient
import org.m0skit0.android.nomoredoli.data.http.HTTPException
import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import org.m0skit0.android.nomoredoli.di.stringModules

class TestDolicloudPuncherImpl : AutoCloseKoinTest() {

    private val loginGetResponse by lazy {
        val body = javaClass.getResourceAsStream("/login_page.html")!!.bufferedReader().use { it.readText() }
        val headers = javaClass.getResourceAsStream("/login_page_headers.json")!!.bufferedReader()
            .use { it.readText() }
            .run { Gson().fromJson<Map<String, String>>(this, Map::class.java) }
        HTTPResponse(200, headers, body)
    }

    private val loginPostResponse by lazy {
        val body = javaClass.getResourceAsStream("/login_post.html")!!.bufferedReader().use { it.readText() }
        HTTPResponse(200, mapOf(), body)
    }

    private val punchSignInGetResponse by lazy {
        val body = javaClass.getResourceAsStream("/punch_page_sign_in.html")!!.bufferedReader().use { it.readText() }
        HTTPResponse(200, mapOf(), body)
    }

    private val punchSignOutPostResponse by lazy {
        val body = javaClass.getResourceAsStream("/punch_page_sign_out.html")!!.bufferedReader().use { it.readText() }
        HTTPResponse(200, mapOf(), body)
    }

    // Session id and token from the login page HTML
    private val sessionId = "DOLSESSID_0affcf6bac3579bc73e1f0a0bd88e161=8uv5in1p8ojq8bq6l2te74kjk2"
    private val token = "99e6df850222cdd97495944817ce73b2"
    private val session = Session(sessionId, token)

    // Parameters from the punch page
    private val idUser = "95"
    private val action = "add"
    private val boutonE = "Sign+In"
    private val boutonS = "Sign Out"

    private val user = "user"
    private val password = "password"

    @MockK
    private lateinit var httpClient: HTTPClient

    private val testModule = module {
        single<DolicloudPuncher> { DolicloudPuncherImpl() }
        factory { httpClient }
    }

    private val dolicloudPuncher by inject<DolicloudPuncher>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        startKoin { modules(testModule, stringModules) }
    }

    @Test
    fun `when getSession error should return error`() {
        every { httpClient.httpGet(any(), any(), any()) } throws HTTPException()
        getSession().shouldBeLeftOfType<HTTPException>()
    }

    @Test
    fun `when getSession success should return session`() {
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(loginGetResponse)
        getSession().shouldBeRight(Session(sessionId, token))
    }

    @Test
    fun `when login error should return error`() {
        every { httpClient.httpPost(any(), any(), any()) } throws HTTPException()
        login(session, "", "").shouldBeLeftOfType<HTTPException>()
    }

    @Test
    fun `when login success should return nothing`() {
        every { httpClient.httpPost(any(), any(), any()) } returns Either.right(loginPostResponse)
        login(session, user, password).shouldBeRight()
    }

    @Test
    fun `when punch error should return error`() {
        every { httpClient.httpGet(any(), any(), any()) } throws HTTPException()
        every { httpClient.httpPost(any(), any(), any()) } throws HTTPException()
        punch(session).shouldBeLeftOfType<HTTPException>()
    }

    @Test
    fun `when punch post error should return error`() {
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(punchSignInGetResponse)
        every { httpClient.httpPost(any(), any(), any()) } throws HTTPException()
        punch(session).shouldBeLeftOfType<HTTPException>()
    }

    @Test
    fun `when punch in success should return nothing`() {
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(punchSignInGetResponse)
        every { httpClient.httpPost(any(), any(), any()) } answers {
            thirdArg<Map<String, String>>().run {
                shouldContain("comment", "")
                shouldContain("idUser", idUser)
                shouldContain("action", action)
                shouldContain("boutonE", boutonE)
            }
            Either.right(punchSignOutPostResponse)
        }
        punch(session).shouldBeRight()
    }

    @Test
    fun `when punch in fails should return error`() {
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(punchSignInGetResponse)
        every { httpClient.httpPost(any(), any(), any()) } returns Either.right(punchSignInGetResponse)
        punch(session).shouldBeLeftOfType<NoMoreException>()
    }

    @Test
    fun `when punch out success should return nothing`() {
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(punchSignOutPostResponse)
        every { httpClient.httpPost(any(), any(), any()) } returns Either.right(punchSignInGetResponse)
        punch(session).shouldBeRight()
    }

    @Test
    fun `when punch out fails should return error`() {
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(punchSignOutPostResponse)
        every { httpClient.httpPost(any(), any(), any()) } returns Either.right(punchSignOutPostResponse)
        punch(session).shouldBeLeftOfType<NoMoreException>()
    }

    private fun getSession() = dolicloudPuncher.getSession().attempt().unsafeRunSync()

    private fun login(session: Session, user: String, password: String) =
        dolicloudPuncher.login(session, user, password).attempt().unsafeRunSync()

    private fun punch(session: Session) = dolicloudPuncher.punch(session).attempt().unsafeRunSync()
}
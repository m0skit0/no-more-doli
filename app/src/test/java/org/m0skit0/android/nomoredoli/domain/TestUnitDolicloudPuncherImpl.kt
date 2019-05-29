package org.m0skit0.android.nomoredoli.domain

import arrow.core.left
import arrow.core.right
import com.google.gson.Gson
import io.kotlintest.assertions.arrow.either.shouldBeLeftOfType
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.maps.shouldContain
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.m0skit0.android.nomoredoli.data.DolicloudPuncher
import org.m0skit0.android.nomoredoli.data.DolicloudPuncherImpl
import org.m0skit0.android.nomoredoli.data.Session
import org.m0skit0.android.nomoredoli.data.http.HTTPClient
import org.m0skit0.android.nomoredoli.data.http.HTTPException
import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import org.m0skit0.android.nomoredoli.di.stringModules
import org.m0skit0.android.nomoredoli.util.NoMoreException

class TestUnitDolicloudPuncherImpl : AutoCloseKoinTest() {

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

    private val punchSignInResponse by lazy {
        val body = javaClass.getResourceAsStream("/punch_page_sign_in.html")!!.bufferedReader().use { it.readText() }
        HTTPResponse(200, mapOf(), body)
    }

    private val punchSignOutResponse by lazy {
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
    private val boutonE = "Sign In"

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
        every { httpClient.httpGet(any(), any(), any()) } returns loginGetResponse.right()
        getSession().shouldBeRight(Session(sessionId, token))
    }

    @Test
    fun `when login error should return error`() {
        every { httpClient.httpPost(any(), any(), any()) } throws HTTPException()
        login(session, "", "").shouldBeLeftOfType<HTTPException>()
    }

    @Test
    fun `when login success should return nothing`() {
        every { httpClient.httpPost(any(), any(), any()) } returns loginPostResponse.right()
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
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignInResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } throws HTTPException()
        punch(session).shouldBeLeftOfType<HTTPException>()
    }

    @Test
    fun `when punch in success should return nothing`() {
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignInResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } answers {
            thirdArg<Map<String, String>>().run {
                shouldContain("comment", "")
                shouldContain("idUser", idUser)
                shouldContain("action", action)
                shouldContain("boutonE", boutonE)
            }
            punchSignOutResponse.right()
        }
        punch(session).shouldBeRight()
    }

    @Test
    fun `when punch in fails should return error`() {
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignInResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } returns punchSignInResponse.right()
        punch(session).shouldBeLeftOfType<NoMoreException>()
    }

    @Test
    fun `when punch out success should return nothing`() {
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignOutResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } returns punchSignInResponse.right()
        punch(session).shouldBeRight()
    }

    @Test
    fun `when punch out fails should return error`() {
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignOutResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } returns punchSignOutResponse.right()
        punch(session).shouldBeLeftOfType<NoMoreException>()
    }

    @Test
    fun `when punch in and page is punch out should not punch`() {
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignOutResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } returns NoMoreException("Should not be called!").left()
        punchIn(session).shouldBeRight()
        verify { httpClient.httpGet(any(), any(), any()) }
        verify(inverse = true) { httpClient.httpPost(any(), any(), any()) }
    }

    @Test
    fun `when punch out and page is punch in should not punch`() {
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignInResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } returns NoMoreException("Should not be called!").left()
        punchOut(session).shouldBeRight()
        verify { httpClient.httpGet(any(), any(), any()) }
        verify(inverse = true) { httpClient.httpPost(any(), any(), any()) }
    }

    @Test
    fun `when punch in and page is punch in should punch`() {
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignInResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } returns punchSignOutResponse.right()
        punchIn(session).shouldBeRight()
        verify { httpClient.httpGet(any(), any(), any()) }
        verify { httpClient.httpPost(any(), any(), any()) }
    }

    @Test
    fun `when punch out and page is punch out should punch`() {
        every { httpClient.httpGet(any(), any(), any()) } returns punchSignOutResponse.right()
        every { httpClient.httpPost(any(), any(), any()) } returns punchSignInResponse.right()
        punchOut(session).shouldBeRight()
        verify { httpClient.httpGet(any(), any(), any()) }
        verify { httpClient.httpPost(any(), any(), any()) }
    }

    private fun getSession() = dolicloudPuncher.getSession().attempt().unsafeRunSync()

    private fun login(session: Session, user: String, password: String) =
        dolicloudPuncher.login(session, user, password).attempt().unsafeRunSync()

    private fun punch(session: Session) = dolicloudPuncher.punch(session).attempt().unsafeRunSync()
    private fun punchIn(session: Session) = dolicloudPuncher.punchIn(session).attempt().unsafeRunSync()
    private fun punchOut(session: Session) = dolicloudPuncher.punchOut(session).attempt().unsafeRunSync()
}
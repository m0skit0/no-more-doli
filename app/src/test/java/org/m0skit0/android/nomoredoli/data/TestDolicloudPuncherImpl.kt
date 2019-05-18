package org.m0skit0.android.nomoredoli.data

import arrow.core.Either
import com.google.gson.Gson
import io.kotlintest.assertions.arrow.either.shouldBeLeftOfType
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.maps.shouldContain
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.m0skit0.android.nomoredoli.data.http.HTTPClient
import org.m0skit0.android.nomoredoli.data.http.HTTPException
import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import org.m0skit0.android.nomoredoli.di.stringModules

class TestDolicloudPuncherImpl {

    private val emptyOKResponse = HTTPResponse(200, mapOf(), "")

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

    private val punchGetResponse by lazy {
        val body = javaClass.getResourceAsStream("/punch_page.html")!!.bufferedReader().use { it.readText() }
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

    private val user = "user"
    private val password = "password"

    private val testModule = module {
        single(override = true) { httpClient }
    }

    @MockK
    private lateinit var httpClient: HTTPClient

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        startKoin { modules(testModule, stringModules) }
//        startKoin { modules(baseModules, stringModules) }
    }

    @After
    fun cleanup() {
        stopKoin()
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
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(punchGetResponse)
        every { httpClient.httpPost(any(), any(), any()) } throws HTTPException()
        punch(session).shouldBeLeftOfType<HTTPException>()
    }

    @Test
    fun `when punch success should return nothing`() {
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(punchGetResponse)
        every { httpClient.httpPost(any(), any(), any()) } answers {
            thirdArg<Map<String, String>>().run {
                shouldContain("comment", "")
                shouldContain("idUser", idUser)
                shouldContain("action", action)
                shouldContain("boutonE", boutonE)
            }
            Either.right(emptyOKResponse)
        }
        punch(session).shouldBeRight()
    }

    private fun getSession() = DolicloudPuncherImpl.getSession().attempt().unsafeRunSync()

    private fun login(session: Session, user: String, password: String) =
        DolicloudPuncherImpl.login(session, user, password).attempt().unsafeRunSync()

    private fun punch(session: Session) = DolicloudPuncherImpl.punch(session).attempt().unsafeRunSync()
}
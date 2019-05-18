package org.m0skit0.android.nomoredoli.data

import arrow.core.Either
import io.kotlintest.assertions.arrow.either.shouldBeLeftOfType
import io.kotlintest.assertions.arrow.either.shouldBeRight
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
import org.m0skit0.android.nomoredoli.di.baseModules
import org.m0skit0.android.nomoredoli.di.stringModules

class TestDolicloudPuncherImpl {

    private val sessionId = "DOLSESSID_0affcf6bac3579bc73e1f0a0bd88e161=af2lpe7alo90ev0fb9dh2v4ds3"
    private val token = "decf93bad0d6510f65cf8d918e2c1a37"
    private val user = "user"
    private val password = "password"
    private val userId = "95"
    private val session = Session(sessionId, token)

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
        val response = getSessionResponse()
        every { httpClient.httpGet(any(), any(), any()) } returns Either.right(response)
        getSession().shouldBeRight(Session(sessionId, token))
    }

    @Test
    fun `when login error should return error`() {
        every { httpClient.httpPost(any(), any(), any()) } throws HTTPException()
        login(session, "", "").shouldBeLeftOfType<HTTPException>()
    }

    @Test
    fun `when login success should return userId`() {
        val loginResponse = getLoginResponse()
        every { httpClient.httpPost(any(), any(), any()) } returns Either.right(loginResponse)
        login(session, user, password).shouldBeRight(userId)
    }

    private fun getSessionResponse(): HTTPResponse {
        val body = "<form id=\"login\" name=\"login\" method=\"post\" action=\"/index.php?mainmenu=home\">\n" +
                "<input type=\"hidden\" name=\"token\" value=\"$token\" />\n" +
                "<input type=\"hidden\" name=\"loginfunction\" value=\"loginfunction\" />\n" +
                "<!-- Add fields to send local user information -->\n" +
                "<input type=\"hidden\" name=\"tz\" id=\"tz\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"tz_string\" id=\"tz_string\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"dst_observed\" id=\"dst_observed\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"dst_first\" id=\"dst_first\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"dst_second\" id=\"dst_second\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"screenwidth\" id=\"screenwidth\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"screenheight\" id=\"screenheight\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"dol_hide_topmenu\" id=\"dol_hide_topmenu\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"dol_hide_leftmenu\" id=\"dol_hide_leftmenu\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"dol_optimize_smallscreen\" id=\"dol_optimize_smallscreen\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"dol_no_mouse_hover\" id=\"dol_no_mouse_hover\" value=\"\" />\n" +
                "<input type=\"hidden\" name=\"dol_use_jmobile\" id=\"dol_use_jmobile\" value=\"\" />"
        val headers = mapOf("Set-Cookie" to "$sessionId; path=/; HttpOnly")
        return HTTPResponse(200, headers, body)
    }

    private fun getLoginResponse(): HTTPResponse {
        val body = "<!-- Includes CSS for JQuery (Ajax library) -->\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/includes/jquery/css/base/jquery-ui.css?layout=classic&version=8.0.5\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/includes/jquery/plugins/jnotify/jquery.jnotify-alt.min.css?layout=classic&version=8.0.5\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/includes/jquery/plugins/select2/dist/css/select2.css?layout=classic&version=8.0.5\">\n" +
                "<!-- Includes CSS for font awesome -->\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/theme/common/fontawesome/css/font-awesome.min.css?layout=classic&version=8.0.5\">\n" +
                "<!-- Includes CSS for Dolibarr theme -->\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/theme/eldy/style.css.php?lang=en_US&amp;theme=eldy&amp;userid=$userId&amp;entity=1&amp;layout=classic&version=8.0.5\">"
        return HTTPResponse(200, mapOf(), body)
    }

    private fun getSession() = DolicloudPuncherImpl.getSession().attempt().unsafeRunSync()

    private fun login(session: Session, user: String, password: String) =
        DolicloudPuncherImpl.login(session, user, password).attempt().unsafeRunSync()
}
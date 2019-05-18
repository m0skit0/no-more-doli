package org.m0skit0.android.nomoredoli.data

import arrow.effects.IO
import com.github.kittinunf.fuel.httpPost
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.m0skit0.android.nomoredoli.data.http.HTTPClient

internal object DolicloudPuncherImpl : DolicloudPuncher, KoinComponent {

    private val httpClient by inject<HTTPClient>()
    private val getTokenUrl by inject<String>(named("index URL"))
    private val postLoginUrl by inject<String>(named("login URL"))

    private val tokenRegex = "<input type=\"hidden\" name=\"token\" value=\"(.*?)\" />".toRegex()
    private val userIdRegex = "userid=(\\d+)".toRegex()

    private const val baseCookie = "hibext_instdsigdipv2=1;"

    private val baseHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Host" to "innocv.on.dolicloud.com"
    )

    private val baseLoginParameters = mapOf(
        "loginfunction" to "loginfunction",
        "tz" to "1",
        "tz_string" to "Europe%2FMadrid",
        "dst_observed" to "1",
        "dst_first" to "2019-03-31T01%3A59%3A00Z",
        "dst_second" to "2019-10-27T02%3A59%3A00Z",
        "screenwidth" to "1920",
        "screenheight" to "947",
        "dol_hide_topmenu" to "",
        "dol_hide_leftmenu" to "",
        "dol_optimize_smallscreen" to "",
        "dol_no_mouse_hover" to "",
        "dol_use_jmobile" to ""
    )

    override fun getSession(): IO<Session> = IO {
        httpClient.httpGet(getTokenUrl).fold({ throw it }) {
            val sessionId = it.headers.getValue("Set-Cookie").split(";").first()
            val token = tokenRegex.find(it.body)!!.groups[1]!!.value
            Session(sessionId, token)
        }
    }

    override fun login(session: Session, user: String, password: String): IO<String> = IO {
        val headers = baseHeaders.toMutableMap()
        headers["Cookie"] = "$baseCookie${session.sessionId}"
        val parameters = baseLoginParameters.toMutableMap()
        parameters["token"] = session.token
        parameters["username"] = user
        parameters["password"] = password
        httpClient.httpPost(postLoginUrl, headers, parameters).fold({ throw it }) {
            userIdRegex.find(it.body)!!.groups[1]!!.value
        }
    }

    override fun punch(session: Session, userId: String): IO<Unit> = IO {
        val parameters = listOf<Pair<String, Any?>>()
        get<String>(named("punch URL")).httpPost(parameters).responseString().let { (request, response, result) ->

        }
    }
}
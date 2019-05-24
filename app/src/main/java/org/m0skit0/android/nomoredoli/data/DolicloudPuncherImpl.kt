package org.m0skit0.android.nomoredoli.data

import arrow.effects.IO
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.m0skit0.android.nomoredoli.data.http.HTTPClient
import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import kotlin.collections.set

internal object DolicloudPuncherImpl : DolicloudPuncher, KoinComponent {

    private val httpClient by inject<HTTPClient>()
    private val getTokenUrl by inject<String>(named("index URL"))
    private val postLoginUrl by inject<String>(named("login URL"))
    private val punchUrl by inject<String>(named("punch URL"))

    private val tokenRegex = "<input type=\"hidden\" name=\"token\" value=\"(.*?)\" />".toRegex()
    private val punchActionRegex = "<input type=\"hidden\" name=\"action\" value=\"(.*?)\">".toRegex()
    private val punchUserIdRegex = "<input type=\"hidden\" name=\"idUser\" value=\"(.*?)\">".toRegex()
    private val punchBoutonRegex = "<input type=\"submit\" class=\"button\" name=\"(bouton[ES])\" value=\"(.*?)\">".toRegex()

    private const val baseCookie = "hibext_instdsigdipv2=1;"

    private val baseHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Host" to "innocv.on.dolicloud.com",
        "Connection" to "keep-alive",
        "Accept-Encoding" to "gzip, deflate, br"
    )

    private val baseLoginParameters = mapOf(
        "loginfunction" to "loginfunction",
        "tz" to "1",
        "tz_string" to "Europe/Madrid",
        "dst_observed" to "1",
        "dst_first" to "2019-03-31T01:59:00Z",
        "dst_second" to "2019-10-27T02:59:00Z",
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
            val token = tokenRegex.find(it.body).getGroupOrThrow(1, "Cannot find token in body when getting session")
            Session(sessionId, token)
        }
    }

    override fun login(session: Session, user: String, password: String): IO<Unit> = IO {
        val headers = baseHeaders.addSessionId(session).addReferer(postLoginUrl)
        val parameters = baseLoginParameters.toMutableMap()
        parameters["token"] = session.token
        parameters["username"] = user
        parameters["password"] = password
        httpClient.httpPost(postLoginUrl, headers, parameters).fold({ throw it }) { Unit }
    }

    override fun punch(session: Session): IO<Unit> = IO {
        var headers = baseHeaders.addSessionId(session).addReferer(punchUrl)
        httpClient.httpGet(punchUrl, headers).fold({ throw it }) { getResponse ->
            val parameters = PunchParameters.fromResponse(getResponse).toMap()
            headers = baseHeaders.addSessionId(session).addReferer(punchUrl)
            httpClient.httpPost(punchUrl, headers, parameters).fold({ throw it }) { postResponse ->
                postResponse.checkPunchResponse(parameters)
                Unit
            }
        }
    }

    private fun Map<String, String>.addSessionId(session: Session) =
        toMutableMap().apply {
            this["Cookie"] = "$baseCookie${session.sessionId}"
        }

    private fun Map<String, String>.addReferer(url: String) = toMutableMap().apply { this["Referer"] = url }

    private fun HTTPResponse.checkPunchResponse(parameters: Map<String, String>) {
        val oldBoutonKey = if (parameters.containsKey("boutonE")) "boutonE" else "boutonS"
        val newBoutonKey = punchBoutonRegex.find(body).getGroupOrThrow(1, "Cannot find bouton in body")
        if (oldBoutonKey == newBoutonKey) throw NoMoreException("bouton value didn't change: still is $oldBoutonKey")
    }

    private fun MatchResult?.getGroupOrThrow(index: Int, errorMessage: String): String {
        this ?: throw NoMoreException(errorMessage)
        return groups[index]?.value ?: throw NoMoreException(errorMessage)
    }

    private data class PunchParameters(
        val idUser: Pair<String, String>,
        val action: Pair<String, String>,
        val bouton: Pair<String, String>,
        val comment: Pair<String, String>
    ) {
        companion object {
            fun fromResponse(response: HTTPResponse) = run {
                val idUser = "idUser" to punchUserIdRegex.find(response.body).getGroupOrThrow(1, "Cannot find idUser in body")
                val action = "action" to  punchActionRegex.find(response.body).getGroupOrThrow(1, "Cannot find action in body")
                val bouton = punchBoutonRegex.find(response.body).run {
                    val key = getGroupOrThrow(1, "Cannot find bouton key in body")
                    val value = getGroupOrThrow(2, "Cannot find bouton value in body")
                    key to value
                }
                val comment = "comment" to ""
                PunchParameters(idUser, action, bouton, comment)
            }
        }

        fun toMap() = mapOf(idUser, action, bouton, comment)
    }
}
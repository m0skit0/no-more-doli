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
    private val getTokenUrl = get<String>(named("index URL"))

    private val tokenRegex = "<input type=\"hidden\" name=\"token\" value=\"(.*?)\" />".toRegex()

    override fun getSession(): IO<Session> = IO {
        httpClient.httpGet(getTokenUrl).fold({ throw it }) {
            val sessionId = it.headers.getValue("Set-Cookie").split(";").first()
            val token = tokenRegex.find(it.body)?.groups?.get(1)?.value ?: ""
            Session(sessionId, token)
        }
    }

    override fun login(session: Session, user: String, password: String): IO<String> = IO {
        val parameters = listOf<Pair<String, Any?>>()
        get<String>(named("login URL")).httpPost(parameters).responseString().let { (request, response, result) ->
            ""
        }
    }

    override fun punch(session: Session, userId: String): IO<Unit> = IO {
        val parameters = listOf<Pair<String, Any?>>()
        get<String>(named("punch URL")).httpPost(parameters).responseString().let { (request, response, result) ->

        }
    }
}
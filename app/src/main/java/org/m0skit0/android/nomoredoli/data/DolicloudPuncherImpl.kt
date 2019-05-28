package org.m0skit0.android.nomoredoli.data

import arrow.effects.IO
import arrow.effects.extensions.io.monad.binding
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.m0skit0.android.nomoredoli.data.http.HTTPClient
import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import org.m0skit0.android.nomoredoli.util.NoMoreException
import org.m0skit0.android.nomoredoli.util.getGroupOrThrow
import kotlin.collections.set

internal class DolicloudPuncherImpl : DolicloudPuncher, KoinComponent {

    private val httpClient by inject<HTTPClient>()
    private val getTokenUrl by inject<String>(named("index URL"))
    private val postLoginUrl by inject<String>(named("login URL"))
    private val punchUrl by inject<String>(named("punch URL"))

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

    override fun punchIn(session: Session): IO<Unit> = httpPunchCondition(session) { isPunchIn() }

    override fun punchOut(session: Session): IO<Unit> = httpPunchCondition(session) { isPunchOut() }

    private fun getPunchParameters(session: Session): IO<PunchParameters> = IO {
        val headers = baseHeaders.addSessionId(session).addReferer(punchUrl)
        httpClient.httpGet(punchUrl, headers).fold({ throw it }) { PunchParameters.fromResponse(it) }
    }

    private fun httpPunch(session: Session, parameters: Map<String, String>): IO<Unit> = IO {
        val headers = baseHeaders.addSessionId(session).addReferer(punchUrl)
        httpClient.httpPost(punchUrl, headers, parameters).fold({ throw it }) { postResponse ->
            postResponse.checkPunchResponse(parameters)
            Unit
        }
    }

    private fun httpPunchCondition(session: Session, predicate: PunchParameters.() -> Boolean): IO<Unit> = binding {
        val punchParameters = bind { getPunchParameters(session) }
        if (punchParameters.predicate()) {
            bind { httpPunch(session, punchParameters.toMap()) }
        } else {
            Unit
        }
    }

    private fun HTTPResponse.checkPunchResponse(parameters: Map<String, String>) {
        val oldBoutonKey = if (parameters.containsKey(BOUTON_PUNCH_IN)) BOUTON_PUNCH_IN else BOUTON_PUNCH_OUT
        val newBoutonKey = punchBoutonRegex.find(body).getGroupOrThrow(1, "Cannot find bouton in body")
        if (oldBoutonKey == newBoutonKey) throw NoMoreException("bouton value didn't change: still is $oldBoutonKey")
    }
}
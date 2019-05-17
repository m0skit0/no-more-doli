package org.m0skit0.android.nomoredoli.data

import arrow.effects.IO
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.m0skit0.android.nomoredoli.data.http.HTTPClient

internal object DolicloudPuncherImpl : DolicloudPuncher, KoinComponent {

    private val httpClient by inject<HTTPClient>()

    override fun getToken(): IO<String> = IO {
        get<String>(named("index URL")).httpGet().responseString().let { (_, response, result) ->
            result.fold({
                response.headers["Set-Cookie"].first().split(";").first()
            }) { throw it.exception }
        }
    }

    override fun login(token: String, user: String, password: String): IO<String> = IO {
        val parameters = listOf<Pair<String, Any?>>()
        get<String>(named("login URL")).httpPost(parameters).responseString().let { (request, response, result) ->
            ""
        }
    }

    override fun punch(token: String, userId: String): IO<Unit> = IO {
        val parameters = listOf<Pair<String, Any?>>()
        get<String>(named("punch URL")).httpPost(parameters).responseString().let { (request, response, result) ->

        }
    }
}
package org.m0skit0.android.nomoredoli.data

import arrow.effects.IO
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost

private const val BASE_URL = "http://dolicloud.bla.bla"
private const val DOLICLOUD_TOKEN_KEY = "DOLICLOUDID"

internal object DolicloudPuncherImpl : DolicloudPuncher {

    override fun getToken(): IO<String> = IO {
        "$BASE_URL/index".httpGet().responseString().let { (request, response, result) ->
            ""
        }
    }

    override fun login(token: String, user: String, password: String): IO<String> = IO {
        val parameters = listOf<Pair<String, Any?>>()
        "$BASE_URL/login".httpPost(parameters).responseString().let { (request, response, result) ->
            ""
        }
    }

    override fun punch(token: String, userId: String): IO<Unit> = IO {
        val parameters = listOf<Pair<String, Any?>>()
        "$BASE_URL/punch".httpPost(parameters).responseString().let { (request, response, result) ->

        }
    }
}
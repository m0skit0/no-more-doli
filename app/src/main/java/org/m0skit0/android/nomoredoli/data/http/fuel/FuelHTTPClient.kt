package org.m0skit0.android.nomoredoli.data.http.fuel

import arrow.core.Either
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import org.m0skit0.android.nomoredoli.data.http.HTTPClient
import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import org.m0skit0.android.nomoredoli.data.http.Parameters

internal object FuelHTTPClient : HTTPClient {

    override fun httpGet(url: String, headers: Parameters, urlParameters: Parameters): Either<Throwable, HTTPResponse> =
        url.httpGet(urlParameters.toFuelParameters()).perform(headers)

    override fun httpPost(url: String, headers: Parameters, bodyParameters: Parameters): Either<Throwable, HTTPResponse> =
        url.httpPost(bodyParameters.toFuelParameters()).perform(headers)

    private fun Request.perform(headers: Parameters): Either<Throwable, HTTPResponse> =
        header(headers).responseString().let { (_, response, result) ->
            result.fold({
                response.toHttpResponse().run { Either.right(this) }
            }) { Either.left(it.exception) }
        }

    private fun Response.toHttpResponse() = HTTPResponse(
        statusCode,
        this.headers.toHeaders(),
        body().asString("text/text")
    )

    private fun Parameters.toFuelParameters() = map { it.key to it.value }

    private fun MutableMap<String, Collection<String>>.toHeaders() =
            map { it.key to it.value.joinToString(";") }.associate { it }
}
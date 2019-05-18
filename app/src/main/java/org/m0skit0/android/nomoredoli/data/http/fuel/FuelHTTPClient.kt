package org.m0skit0.android.nomoredoli.data.http.fuel

import arrow.core.Either
import arrow.core.Try
import arrow.core.extensions.either.monad.flatten
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.data.http.HTTPClient
import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import org.m0skit0.android.nomoredoli.data.http.Parameters
import org.m0skit0.android.nomoredoli.util.Logger

internal object FuelHTTPClient : HTTPClient, KoinComponent {

    private val logger by inject<Logger>()

    override fun httpGet(url: String, headers: Parameters, urlParameters: Parameters): Either<Throwable, HTTPResponse> =
        tryHttp { url.httpGet(urlParameters.toFuelParameters()).perform(headers) }

    override fun httpPost(url: String, headers: Parameters, bodyParameters: Parameters): Either<Throwable, HTTPResponse> =
        tryHttp { url.httpPost(bodyParameters.toFuelParameters()).perform(headers) }

    private fun tryHttp(block: () -> Either<Throwable, HTTPResponse>) = Try { block() }.toEither().flatten()

    private fun Request.perform(headers: Parameters): Either<Throwable, HTTPResponse> = header(headers).run {
        if (request.method == Method.POST) appendHeader("Content-Length", request.parameters.calculateSize())
        logger.logInfo("Request: $request")
        logger.logInfo("Request headers: ${request.headers}")
        responseString().let { (_, response, result) ->
            result.fold({
                response.toHttpResponse().run { Either.right(this) }
            }) {
                logger.log(it.exception)
                Either.left(it.exception)
            }
        }
    }

    private fun Response.toHttpResponse() = HTTPResponse(
        statusCode,
        this.headers.toHeaders(),
        body().asString("text/text")
    )

    private fun Parameters.toFuelParameters() = map { it.key to it.value }

    private fun MutableMap<String, Collection<String>>.toHeaders() =
            map { it.key to it.value.joinToString(";") }.associate { it }

    private fun List<Pair<String, Any?>>.calculateSize() =
        if (size == 0) 0 else {
            fold(0) { acc, pair ->
                acc + pair.first.length + pair.second.toString().length
            } + (2 * size) - 1
        }
}
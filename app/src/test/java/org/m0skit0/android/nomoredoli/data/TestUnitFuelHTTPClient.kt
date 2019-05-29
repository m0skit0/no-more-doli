package org.m0skit0.android.nomoredoli.data

import arrow.core.Either
import com.github.kittinunf.fuel.core.HttpException
import com.google.gson.Gson
import io.kotlintest.assertions.arrow.either.shouldBeLeftOfType
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.matchers.maps.shouldContainKeys
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.m0skit0.android.nomoredoli.data.http.HTTPResponse
import org.m0skit0.android.nomoredoli.data.http.fuel.FuelHTTPClient
import org.m0skit0.android.nomoredoli.util.Logger
import java.net.MalformedURLException
import java.net.UnknownHostException

// NOTE: this class needs an internet connection to work (or edit the URLs for local ones).
class TestUnitFuelHTTPClient {

    private val malformedURL = "blabla"
    private val nonExistentURL = "http://doesthisexist.xxx"
    private val getUrl = "https://httpbin.org/get"
    private val postUrl = "https://httpbin.org/post"
    private val status500 = "https://httpbin.org/status/500"
    private val headers = "https://httpbin.org/headers"

    private val httpClient by lazy { FuelHTTPClient }

    @MockK
    private lateinit var logger: Logger

    private val testModule = module {
        factory { logger }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { logger.logInfo(any()) } answers { println(firstArg<String>()) }
        every { logger.log(any()) } answers { firstArg<Throwable>().printStackTrace() }
        startKoin { modules(testModule) }
    }

    @After
    fun cleanup() {
        stopKoin()
    }

    @Test
    fun `when malformed URL should return error`() {
        httpClient.httpGet(malformedURL).shouldBeLeftOfType<MalformedURLException>()
        httpClient.httpPost(malformedURL).shouldBeLeftOfType<MalformedURLException>()
    }

    @Test
    fun `when non-existent URL should return error`() {
        httpClient.httpGet(nonExistentURL).shouldBeLeftOfType<UnknownHostException>()
        httpClient.httpPost(nonExistentURL).shouldBeLeftOfType<UnknownHostException>()
    }

    @Test
    fun `when real URL should return response`() {
        httpClient.httpGet(getUrl).shouldBeRight()
        httpClient.httpPost(postUrl).shouldBeRight()
    }

    @Test
    fun `when wrong method should return error`() {
        httpClient.httpGet(postUrl).shouldBeLeftOfType<HttpException>()
        httpClient.httpPost(getUrl).shouldBeLeftOfType<HttpException>()
    }

    @Test
    fun `when get response is not 200 method should return error`() {
        httpClient.httpGet(status500).shouldBeLeftOfType<HttpException>()
        httpClient.httpPost(status500).shouldBeLeftOfType<HttpException>()
    }

    @Test
    fun `when response should be default headers`() {
        httpClient.httpGet(headers).testHeaders()
    }

    @Test
    fun `when response should be sent headers`() {
        val testHeaders = mapOf("Header1" to "1", "Header2" to "2")
        httpClient.httpGet(headers, testHeaders).testHeaders(testHeaders)
    }

    @Test
    fun `when response should be sent overriden headers`() {
        val testHeaders = mapOf("User-Agent" to "1", "Accept" to "2")
        httpClient.httpGet(headers, testHeaders).run {
            testHeaders(testHeaders)
            toHeadersJson().run {
                shouldContain("User-Agent", "1")
                shouldContain("Accept", "2")
            }
        }
    }

    @Test
    fun `when response should be sent parameters headers`() {
        val testParams = mapOf("param1" to "1", "param2" to "2")
        httpClient.httpGet(getUrl, urlParameters = testParams).toGetParamsJson().run {
            shouldContain("param1", "1")
            shouldContain("param2", "2")
        }
        httpClient.httpPost(postUrl, bodyParameters = testParams).toPostParamsJson().run {
            shouldContain("param1", "1")
            shouldContain("param2", "2")
        }
    }

    @Test
    fun `when post with params should have correct Content-Length`() {
        var testParams = mapOf("param1" to "1")
        httpClient.httpPost(postUrl, bodyParameters = testParams).toHeadersJson().run {
            shouldContain("Content-Length", "8")
        }
        testParams = mapOf("param1" to "1", "param2" to "2")
        httpClient.httpPost(postUrl, bodyParameters = testParams).toHeadersJson().run {
            shouldContain("Content-Length", "17")
        }
    }

    private fun Either<Throwable, HTTPResponse>.testHeaders(headers: Map<String, String> = mapOf()) {
        shouldBeRight()
        toHeadersJson().run {
            if (headers.isEmpty()) {
                shouldContainKey("Accept")
                shouldContain("Host", "httpbin.org")
                shouldContainKey("User-Agent")
            } else {
                shouldContainKeys(*headers.keys.toTypedArray())
            }
        }
    }

    private fun Either<Throwable, HTTPResponse>.toHeadersJson() = toMapJson().getValue("headers")
    private fun Either<Throwable, HTTPResponse>.toGetParamsJson() = toMapJson().getValue("args")
    private fun Either<Throwable, HTTPResponse>.toPostParamsJson() = toMapJson().getValue("form")

    private fun Either<Throwable, HTTPResponse>.toMapJson() =
        fold({ throw it }) { response ->
            response.body.run {
                Gson().fromJson<Map<String, Map<String, String>>>(this, Map::class.java)
            }
        }
}
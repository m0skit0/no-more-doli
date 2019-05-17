package org.m0skit0.android.nomoredoli.data.http

import arrow.core.Either

internal typealias Parameters = Map<String, String>

internal interface HTTPClient {
    fun httpGet(url: String, headers: Parameters = mapOf(), urlParameters: Parameters = mapOf()): Either<Throwable, HTTPResponse>
    fun httpPost(url: String, headers: Parameters = mapOf(), bodyParameters: Parameters = mapOf()): Either<Throwable, HTTPResponse>
}
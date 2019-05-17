package org.m0skit0.android.nomoredoli.data.http

internal data class HTTPResponse(
    val status: Int,
    val headers: Parameters,
    val body: String
)
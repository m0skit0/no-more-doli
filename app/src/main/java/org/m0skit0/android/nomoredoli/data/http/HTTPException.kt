package org.m0skit0.android.nomoredoli.data.http

class HTTPException : Exception {
    constructor(): super()
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
}
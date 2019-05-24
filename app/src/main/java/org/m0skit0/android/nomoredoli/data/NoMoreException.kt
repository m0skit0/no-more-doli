package org.m0skit0.android.nomoredoli.data

internal class NoMoreException : Exception {
    constructor() : super()
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
}
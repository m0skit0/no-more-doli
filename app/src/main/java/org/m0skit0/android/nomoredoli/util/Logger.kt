package org.m0skit0.android.nomoredoli.util

internal interface Logger {
    fun log(e: Throwable)
    fun logInfo(message: String)
}
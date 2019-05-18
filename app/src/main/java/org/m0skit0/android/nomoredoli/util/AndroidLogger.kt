package org.m0skit0.android.nomoredoli.util

import android.util.Log

internal object AndroidLogger : Logger {

    private const val TAG = "Log"

    override fun log(e: Throwable) {
        Log.e(TAG, Log.getStackTraceString(e))
    }

    override fun logInfo(message: String) {
        Log.i(TAG, message)
    }

}
package org.m0skit0.android.nomoredoli.data

internal fun Map<String, String>.addSessionId(session: Session) =
    toMutableMap().apply {
        this["Cookie"] = "$baseCookie${session.sessionId}"
    }

internal fun Map<String, String>.addReferer(url: String) = toMutableMap().apply { this["Referer"] = url }
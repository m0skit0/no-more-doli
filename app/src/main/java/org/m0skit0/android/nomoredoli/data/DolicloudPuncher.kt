package org.m0skit0.android.nomoredoli.data

import arrow.effects.IO

internal interface DolicloudPuncher {
    fun getToken(): IO<String>
    fun login(token: String, user: String, password: String): IO<String>
    fun punch(token: String, userId: String): IO<Unit>
}
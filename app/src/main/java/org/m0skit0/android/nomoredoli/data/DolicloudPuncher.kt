package org.m0skit0.android.nomoredoli.data

import arrow.effects.IO

internal interface DolicloudPuncher {
    fun getSession(): IO<Session>
    fun login(session: Session, user: String, password: String): IO<String>
    fun punch(session: Session, userId: String): IO<Unit>
}
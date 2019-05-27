package org.m0skit0.android.nomoredoli.data

import arrow.effects.IO

internal interface DolicloudPuncher {
    fun getSession(): IO<Session>
    fun login(session: Session, user: String, password: String): IO<Unit>
    fun punch(session: Session): IO<Unit>
    fun punchIn(session: Session): IO<Unit>
    fun punchOut(session: Session): IO<Unit>
}
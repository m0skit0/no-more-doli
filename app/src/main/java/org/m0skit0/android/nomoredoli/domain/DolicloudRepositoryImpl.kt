package org.m0skit0.android.nomoredoli.domain

import arrow.core.Either
import arrow.core.flatMap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.data.DolicloudPuncher
import org.m0skit0.android.nomoredoli.data.Session

internal object DolicloudRepositoryImpl : DolicloudRepository, KoinComponent {

    private val puncher by inject<DolicloudPuncher>()

    override fun punchAsync(user: String, password: String): Deferred<Either<Throwable, Unit>> =
        punchGeneralAsync(user, password) { puncher.punch(it).attempt().unsafeRunSync() }

    override fun punchInAsync(user: String, password: String): Deferred<Either<Throwable, Unit>> =
        punchGeneralAsync(user, password) { puncher.punchIn(it).attempt().unsafeRunSync() }

    override fun punchOutAsync(user: String, password: String): Deferred<Either<Throwable, Unit>> =
        punchGeneralAsync(user, password) { puncher.punchOut(it).attempt().unsafeRunSync() }

    private fun punchGeneralAsync(user: String, password: String, punchFunction: (Session) -> Either<Throwable, Unit>): Deferred<Either<Throwable, Unit>> =
        GlobalScope.async {
            with (puncher) {
                getSession().attempt().unsafeRunSync().flatMap { session ->
                    login(session, user, password).attempt().unsafeRunSync().flatMap {
                        punchFunction(session)
                    }
                }
            }
        }
}
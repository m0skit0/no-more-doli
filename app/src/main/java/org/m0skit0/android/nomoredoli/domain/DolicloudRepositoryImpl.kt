package org.m0skit0.android.nomoredoli.domain

import arrow.core.Either
import arrow.core.flatMap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.data.DolicloudPuncher

internal object DolicloudRepositoryImpl : DolicloudRepository, KoinComponent {

    private val puncher by inject<DolicloudPuncher>()

    override fun punchAsync(user: String, password: String): Deferred<Either<Throwable, Unit>> =
        GlobalScope.async {
            with (puncher) {
                getSession().attempt().unsafeRunSync().flatMap { session ->
                    login(session, user, password).attempt().unsafeRunSync().flatMap { userId ->
                        punch(session, userId).attempt().unsafeRunSync()
                    }
                }
            }
        }
}
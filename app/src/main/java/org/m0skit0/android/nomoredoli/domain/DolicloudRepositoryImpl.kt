package org.m0skit0.android.nomoredoli.domain

import arrow.core.Either
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.data.DolicloudPuncher

internal object DolicloudRepositoryImpl : DolicloudRepository, KoinComponent {

    private val puncher by inject<DolicloudPuncher>()

    override fun punch(user: String, password: String): Deferred<Either<Throwable, Unit>> = GlobalScope.async {
        with (puncher) {
            getToken().attempt().unsafeRunSync().fold({ Either.left(it) }) { token ->
                login(token, user, password).attempt().unsafeRunSync().fold({ Either.left(it) }) { userId ->
                    punch(token, userId).attempt().unsafeRunSync()
                }
            }
        }
    }
}
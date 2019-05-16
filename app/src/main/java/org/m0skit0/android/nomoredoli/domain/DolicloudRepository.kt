package org.m0skit0.android.nomoredoli.domain

import arrow.core.Either
import kotlinx.coroutines.Deferred

internal interface DolicloudRepository {
    fun punch(user: String, password: String): Deferred<Either<Throwable, Unit>>
}
package org.m0skit0.android.nomoredoli.domain

import arrow.core.Either
import kotlinx.coroutines.Deferred

internal interface DolicloudRepository {
    fun punchAsync(user: String, password: String): Deferred<Either<Throwable, Unit>>
    fun punchInAsync(user: String, password: String): Deferred<Either<Throwable, Unit>>
    fun punchOutAsync(user: String, password: String): Deferred<Either<Throwable, Unit>>
}
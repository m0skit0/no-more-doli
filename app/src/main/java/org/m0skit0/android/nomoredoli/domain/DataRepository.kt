package org.m0skit0.android.nomoredoli.domain

import arrow.core.Option

internal interface DataRepository {
    fun saveUser(user: String)
    fun savePassword(password: String)
    fun saveUserId(userId: String)
    fun getUser(): Option<String>
    fun getPassword(): Option<String>
    fun getUserId(): Option<String>
}
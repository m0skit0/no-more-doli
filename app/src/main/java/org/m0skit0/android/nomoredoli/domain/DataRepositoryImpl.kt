package org.m0skit0.android.nomoredoli.domain

import android.content.Context
import arrow.core.Option
import org.koin.core.KoinComponent
import org.koin.core.get

private const val PREFERENCES_FILE = "preferences"
private const val USER_KEY = "user"
private const val PASSWORD_KEY = "password"
private const val USERID_KEY = "userId"

internal object DataRepositoryImpl : DataRepository, KoinComponent {

    private val sharedPreferences = get<Context>().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    override fun getUser(): Option<String> = getString(USER_KEY)

    override fun getPassword(): Option<String> = getString(PASSWORD_KEY)

    override fun getUserId(): Option<String> = getString(USERID_KEY)

    override fun saveUser(user: String) {
        saveString(USER_KEY, user)
    }

    override fun savePassword(password: String) {
        saveString(PASSWORD_KEY, password)
    }

    override fun saveUserId(userId: String) {
        saveString(USERID_KEY, userId)
    }

    private fun getString(key: String): Option<String> =
        sharedPreferences.getString(key, null).run { Option.fromNullable(this) }

    private fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}
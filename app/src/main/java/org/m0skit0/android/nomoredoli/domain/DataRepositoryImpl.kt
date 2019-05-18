package org.m0skit0.android.nomoredoli.domain

import android.content.Context
import arrow.core.Option
import org.koin.core.KoinComponent
import org.koin.core.get

private const val PREFERENCES_FILE = "preferences"
private const val USER_KEY = "user"
private const val PASSWORD_KEY = "password"

internal object DataRepositoryImpl : DataRepository, KoinComponent {

    private val sharedPreferences = get<Context>().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    override fun getLogin(): Option<Login> =
        getString(USER_KEY).flatMap { user ->
            getString(PASSWORD_KEY).flatMap { password ->
                Option.just(Login(user, password))
            }
        }

    override fun saveLogin(login: Login) {
        saveString(USER_KEY, login.user)
        saveString(PASSWORD_KEY, login.password)
    }

    private fun getString(key: String): Option<String> =
        sharedPreferences.getString(key, null).run { Option.fromNullable(this) }

    private fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}
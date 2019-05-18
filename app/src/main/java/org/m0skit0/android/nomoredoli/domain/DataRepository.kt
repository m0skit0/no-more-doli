package org.m0skit0.android.nomoredoli.domain

import android.util.Log
import arrow.core.Option

internal interface DataRepository {
    fun saveLogin(login: Login)
    fun getLogin(): Option<Login>
}
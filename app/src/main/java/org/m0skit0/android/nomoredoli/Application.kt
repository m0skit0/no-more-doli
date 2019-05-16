package org.m0skit0.android.nomoredoli

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.m0skit0.android.nomoredoli.di.koinModules

internal class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(koinModules)
        }
    }
}
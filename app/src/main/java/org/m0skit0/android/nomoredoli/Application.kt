package org.m0skit0.android.nomoredoli

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.m0skit0.android.nomoredoli.di.androidModules
import org.m0skit0.android.nomoredoli.di.baseModules
import org.m0skit0.android.nomoredoli.di.stringModules

internal class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(baseModules, androidModules, stringModules)
        }
    }
}
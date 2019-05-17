package org.m0skit0.android.nomoredoli.di

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.m0skit0.android.nomoredoli.BuildConfig
import org.m0skit0.android.nomoredoli.data.DolicloudPuncher
import org.m0skit0.android.nomoredoli.data.DolicloudPuncherImpl
import org.m0skit0.android.nomoredoli.domain.DataRepository
import org.m0skit0.android.nomoredoli.domain.DataRepositoryImpl
import org.m0skit0.android.nomoredoli.domain.DolicloudRepository
import org.m0skit0.android.nomoredoli.domain.DolicloudRepositoryImpl
import org.m0skit0.android.nomoredoli.viewmodel.PunchViewModel

private const val BASE_URL = BuildConfig.BASE_URL

internal val baseModules = module {
    single<DolicloudPuncher> { DolicloudPuncherImpl }
    single<DolicloudRepository> { DolicloudRepositoryImpl }
    single<DataRepository> { DataRepositoryImpl }
    viewModel { PunchViewModel() }
}

internal val stringModules = module {
    single(named("index URL")) { "$BASE_URL/index.php" }
    single(named("login URL")) { "$BASE_URL/index.php?mainmenu=home" }
    single(named("punch URL")) { "$BASE_URL/pointage/pointagetop_page.php" }
}
package org.m0skit0.android.nomoredoli.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.m0skit0.android.nomoredoli.data.DolicloudPuncher
import org.m0skit0.android.nomoredoli.data.DolicloudPuncherImpl
import org.m0skit0.android.nomoredoli.domain.DolicloudRepository
import org.m0skit0.android.nomoredoli.domain.DolicloudRepositoryImpl
import org.m0skit0.android.nomoredoli.viewmodel.PunchViewModel

internal val koinModules = module {
    single<DolicloudPuncher> { DolicloudPuncherImpl }
    single<DolicloudRepository> { DolicloudRepositoryImpl }
    viewModel<ViewModel> { PunchViewModel() }
}
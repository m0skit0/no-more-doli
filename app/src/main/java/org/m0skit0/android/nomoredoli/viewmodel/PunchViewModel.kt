package org.m0skit0.android.nomoredoli.viewmodel

import android.arch.lifecycle.ViewModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.domain.DolicloudRepository

internal class PunchViewModel : ViewModel(), KoinComponent {

    private val repository by inject<DolicloudRepository>()

    fun onClickPunch() {
        repository.punchAsync("", "")
    }
}
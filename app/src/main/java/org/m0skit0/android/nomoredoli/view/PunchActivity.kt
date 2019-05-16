package org.m0skit0.android.nomoredoli.view

import android.app.Activity
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.viewmodel.PunchViewModel

internal class PunchActivity : Activity(), KoinComponent {

    private val viewModel by viewModel<PunchViewModel>()
}
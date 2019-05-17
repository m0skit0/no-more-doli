package org.m0skit0.android.nomoredoli.view

import android.support.v7.app.AppCompatActivity
import org.koin.android.viewmodel.ext.android.viewModel
import org.m0skit0.android.nomoredoli.viewmodel.PunchViewModel

internal class PunchActivity : AppCompatActivity() {

    private val punchViewModel: PunchViewModel by viewModel()
}
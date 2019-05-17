package org.m0skit0.android.nomoredoli.view

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.koin.android.viewmodel.ext.android.viewModel
import org.m0skit0.android.nomoredoli.R
import org.m0skit0.android.nomoredoli.databinding.ActivityPunchBinding
import org.m0skit0.android.nomoredoli.viewmodel.PunchViewModel

internal class PunchActivity : AppCompatActivity() {

    private val punchViewModel: PunchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityPunchBinding>(this, R.layout.activity_punch).run {
            viewmodel = punchViewModel
        }
    }
}
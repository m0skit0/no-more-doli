package org.m0skit0.android.nomoredoli.view

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.koin.android.viewmodel.ext.android.viewModel
import org.m0skit0.android.nomoredoli.R
import org.m0skit0.android.nomoredoli.databinding.LoadBinding
import org.m0skit0.android.nomoredoli.databinding.PunchBinding
import org.m0skit0.android.nomoredoli.util.toast
import org.m0skit0.android.nomoredoli.viewmodel.PunchViewModel

internal class PunchActivity : AppCompatActivity() {

    private val punchViewModel: PunchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContenView()
        registerObservers()
    }

    private fun setContenView() {
        DataBindingUtil.setContentView<PunchBinding>(this, R.layout.punch).run {
            viewmodel = punchViewModel
        }
    }

    private fun registerObservers() {
        with (punchViewModel) {
            toastMessage.observe({ lifecycle }) { toast(it) }
            showLoading.observe({ lifecycle }) {
                if (it == true) {
                    DataBindingUtil.setContentView<LoadBinding>(this@PunchActivity, R.layout.load)
                } else {
                    setContenView()
                }
            }
        }
    }
}
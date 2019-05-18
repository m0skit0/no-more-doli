package org.m0skit0.android.nomoredoli.view

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.m0skit0.android.nomoredoli.R
import org.m0skit0.android.nomoredoli.databinding.LoadBinding
import org.m0skit0.android.nomoredoli.databinding.LoginBinding
import org.m0skit0.android.nomoredoli.util.toast
import org.m0skit0.android.nomoredoli.viewmodel.LoginViewModel

internal class LoginActivity : AppCompatActivity() {

    companion object : KoinComponent {
        fun launch() {
            with (get<Context>()) {
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }.run { startActivity(this) }
            }
        }
    }

    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLoadingLayout()
        registerObservers()
        loginViewModel.checkLoginExists()
    }

    private fun setLoginLayout() {
        DataBindingUtil.setContentView<LoginBinding>(this, R.layout.login).run {
            viewmodel = loginViewModel
            lifecycleOwner = this@LoginActivity
        }
    }

    private fun setLoadingLayout() {
        DataBindingUtil.setContentView<LoadBinding>(this@LoginActivity, R.layout.load)
    }

    private fun registerObservers() {
        with (loginViewModel) {
            toastMessage.observe({ lifecycle }) { toast(it) }
            showLoading.observe({ lifecycle }) {
                if (it == true) {
                    setLoadingLayout()
                } else {
                    setLoginLayout()
                }
            }
        }
    }
}
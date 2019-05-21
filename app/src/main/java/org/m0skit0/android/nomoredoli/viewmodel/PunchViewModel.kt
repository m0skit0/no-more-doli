package org.m0skit0.android.nomoredoli.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.res.Resources
import android.support.annotation.StringRes
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.R
import org.m0skit0.android.nomoredoli.domain.DataRepository
import org.m0skit0.android.nomoredoli.domain.DolicloudRepository
import org.m0skit0.android.nomoredoli.view.LoginActivity

internal class PunchViewModel : ViewModel(), KoinComponent {

    private val punchRepository by inject<DolicloudRepository>()
    private val dataRepository by inject<DataRepository>()
    private val resources by inject<Resources>()

    val toastMessage = MutableLiveData<String>()
    val showLoading = MutableLiveData<Boolean>()

    fun onClickPunch() {
        showLoading.postValue(true)
        GlobalScope.launch {
            dataRepository.getLogin().fold({ getString(R.string.error_no_user) }) { login ->
                with (login) {
                    punchRepository.punchAsync(user, password).await()
                        .fold({ getString(R.string.error_punch, it.getToastMessage()) }) {
                            getString(R.string.punch_success)
                        }
                }
            }.let { message ->
                showLoading.postValue(false)
                toastMessage.postValue(message)
            }
        }
    }

    fun onClickClearLogin() {
        dataRepository.clearLogin()
        LoginActivity.launch()
    }

    private fun getString(@StringRes id: Int, vararg params: String) = resources.getString(id, *params)

    private fun Throwable.getToastMessage() = "${javaClass.simpleName}: ${message ?: ""}"
}
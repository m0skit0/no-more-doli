package org.m0skit0.android.nomoredoli.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
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

    val toastMessage = MutableLiveData<Int>()
    val showLoading = MutableLiveData<Boolean>()

    fun onClickPunch() {
        showLoading.postValue(true)
        GlobalScope.launch {
            dataRepository.getLogin().fold({ R.string.error_no_user }) { login ->
                with (login) {
                    punchRepository.punchAsync(user, password).await()
                        .fold({ R.string.error_punch }) { R.string.punch_success }
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
}
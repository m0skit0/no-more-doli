package org.m0skit0.android.nomoredoli.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.R
import org.m0skit0.android.nomoredoli.domain.DataRepository
import org.m0skit0.android.nomoredoli.domain.Login
import org.m0skit0.android.nomoredoli.view.PunchActivity

internal class LoginViewModel : ViewModel(), KoinComponent {

    private val dataRepository by inject<DataRepository>()

    val loginText = MutableLiveData<String>()
    val passwordText = MutableLiveData<String>()
    val toastMessage = MutableLiveData<Int>()
    val showLoading = MutableLiveData<Boolean>().apply { postValue(true) }

    fun onClickSave() {
        if (loginText.value.isNullOrEmpty()) {
            toastMessage.value = R.string.invalid_user
            return
        }
        if (passwordText.value.isNullOrEmpty()) {
            toastMessage.value = R.string.invalid_password
            return
        }
        loginText.value?.let { user ->
            passwordText.value?.let { password ->
                val login = Login(user, password)
                dataRepository.saveLogin(login)
                PunchActivity.launch()
            }
        }
    }

    fun checkLoginExists() {
        dataRepository.getLogin().fold({
            showLoading.postValue(false)
        }) {
            PunchActivity.launch()
        }
    }

}
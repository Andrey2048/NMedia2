package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: AppAuth): ViewModel() {
    val data = auth
        .authStateFlow
        .asLiveData(Dispatchers.Default)
    val isAuthorized: Boolean
        get() = auth.authStateFlow.value.token != null
}

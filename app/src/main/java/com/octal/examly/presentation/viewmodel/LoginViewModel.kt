package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.usecase.auth.LoginUseCase
import com.octal.examly.presentation.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                if (username.isBlank()) {
                    _loginState.value = LoginState.Error("Username cannot be empty")
                    return@launch
                }

                if (password.isBlank()) {
                    _loginState.value = LoginState.Error("Password cannot be empty")
                    return@launch
                }

                val result = loginUseCase(username, password)

                result.fold(
                    onSuccess = { user ->
                        _loginState.value = LoginState.Success(user)
                    },
                    onFailure = { exception ->
                        _loginState.value = LoginState.Error(
                            exception.message ?: "Login failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

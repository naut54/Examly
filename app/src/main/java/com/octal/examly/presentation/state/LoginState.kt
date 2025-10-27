package com.octal.examly.presentation.state

import com.octal.examly.domain.model.User

sealed class LoginState {
    object Idle : LoginState()

    object Loading : LoginState()

    data class Success(val user: User) : LoginState()

    data class Error(val message: String) : LoginState()
}

package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.User
import com.octal.examly.domain.model.UserRole
import com.octal.examly.domain.usecase.user.CreateUserUseCase
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateUserViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase
) : ViewModel() {

    private val _creationState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val creationState: StateFlow<UiState<Long>> = _creationState.asStateFlow()

    fun createUser(username: String, password: String, role: UserRole) {
        viewModelScope.launch {
            try {
                _creationState.value = UiState.Loading

                if (username.isBlank()) {
                    _creationState.value = UiState.Error("Username cannot be empty")
                    return@launch
                }

                if (password.length < 6) {
                    _creationState.value = UiState.Error("Password must be at least 6 characters")
                    return@launch
                }

                val result = createUserUseCase(username, password, role)
                result.fold(
                    onSuccess = { id ->
                        _creationState.value = UiState.Success(id)
                    },
                    onFailure = { exception ->
                        _creationState.value = UiState.Error(
                            exception.message ?: "Failed to create user"
                        )
                    }
                )
            } catch (e: Exception) {
                _creationState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}

package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.usecase.subject.CreateSubjectUseCase
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateSubjectViewModel @Inject constructor(
    private val createSubjectUseCase: CreateSubjectUseCase
) : ViewModel() {

    private val _creationState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val creationState: StateFlow<UiState<Long>> = _creationState.asStateFlow()

    fun createSubject(name: String, description: String) {
        viewModelScope.launch {
            try {
                _creationState.value = UiState.Loading

                if (name.isBlank()) {
                    _creationState.value = UiState.Error("Name cannot be empty")
                    return@launch
                }

                val result = createSubjectUseCase(name, description)
                result.fold(
                    onSuccess = { id ->
                        _creationState.value = UiState.Success(id)
                    },
                    onFailure = { exception ->
                        _creationState.value = UiState.Error(
                            exception.message ?: "Failed to create subject"
                        )
                    }
                )
            } catch (e: Exception) {
                _creationState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}

package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.User
import com.octal.examly.domain.usecase.test.AssignTestToUsersUseCase
import com.octal.examly.domain.usecase.test.GetAllTestsUseCase
import com.octal.examly.domain.usecase.user.GetAllUsersUseCase
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignTestViewModel @Inject constructor(
    private val getAllTestsUseCase: GetAllTestsUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val assignTestToUsersUseCase: AssignTestToUsersUseCase
) : ViewModel() {

    private val _testsState = MutableStateFlow<UiState<List<Test>>>(UiState.Loading)
    val testsState: StateFlow<UiState<List<Test>>> = _testsState.asStateFlow()

    private val _usersState = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val usersState: StateFlow<UiState<List<User>>> = _usersState.asStateFlow()

    private val _assignmentState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val assignmentState: StateFlow<UiState<Unit>> = _assignmentState.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private var selectedTest: Test? = null

    fun setCurrentStep(step: Int) {
        _currentStep.value = step
    }

    fun loadAllTests() {
        viewModelScope.launch {
            try {
                _testsState.value = UiState.Loading

                getAllTestsUseCase().collect { tests ->
                    _testsState.value = UiState.Success(tests)
                }
            } catch (e: Exception) {
                _testsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _usersState.value = UiState.Loading

                getAllUsersUseCase().collect { users ->
                    val onlyUsers = users.filter { it.role.isUser() }
                    _usersState.value = UiState.Success(onlyUsers)
                }
            } catch (e: Exception) {
                _usersState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun setSelectedTest(test: Test) {
        selectedTest = test
    }

    fun assignTestToUsers(userIds: List<Long>, deadline: Long) {
        viewModelScope.launch {
            try {
                _assignmentState.value = UiState.Loading

                selectedTest?.let { test ->
                    val result = assignTestToUsersUseCase(test.id, userIds, deadline)
                    result.fold(
                        onSuccess = {
                            _assignmentState.value = UiState.Success(Unit)
                        },
                        onFailure = { exception ->
                            _assignmentState.value = UiState.Error(
                                exception.message ?: "Failed to assign test"
                            )
                        }
                    )
                } ?: run {
                    _assignmentState.value = UiState.Error("No test selected")
                }
            } catch (e: Exception) {
                _assignmentState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}

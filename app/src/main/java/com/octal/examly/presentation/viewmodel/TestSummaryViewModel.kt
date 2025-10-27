package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.usecase.assignment.GetAssignmentByIdUseCase
import com.octal.examly.domain.usecase.attempt.GetPendingTestAttemptUseCase
import com.octal.examly.domain.usecase.test.GetTestByIdUseCase
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestSummaryViewModel @Inject constructor(
    private val getAssignmentByIdUseCase: GetAssignmentByIdUseCase,
    private val getPendingTestAttemptUseCase: GetPendingTestAttemptUseCase,
    private val getTestByIdUseCase: GetTestByIdUseCase
) : ViewModel() {

    private val _assignmentState = MutableStateFlow<UiState<TestAssignment>>(UiState.Loading)
    val assignmentState: StateFlow<UiState<TestAssignment>> = _assignmentState.asStateFlow()

    private val _testState = MutableStateFlow<UiState<Test>>(UiState.Loading)
    val testState: StateFlow<UiState<Test>> = _testState.asStateFlow()

    private val _pendingAttempt = MutableStateFlow<TestAttempt?>(null)
    val pendingAttempt: StateFlow<TestAttempt?> = _pendingAttempt.asStateFlow()

    fun loadAssignment(assignmentId: Long) {
        viewModelScope.launch {
            try {
                _assignmentState.value = UiState.Loading

                val result = getAssignmentByIdUseCase(assignmentId)
                result.fold(
                    onSuccess = { assignment ->
                        _assignmentState.value = UiState.Success(assignment)
                        loadTestDetails(assignment.testId)
                        checkPendingAttempt(assignment.userId)
                    },
                    onFailure = { exception ->
                        _assignmentState.value = UiState.Error(
                            exception.message ?: "Failed to load assignment"
                        )
                    }
                )
            } catch (e: Exception) {
                _assignmentState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun loadTestDetails(testId: Long) {
        viewModelScope.launch {
            try {
                _testState.value = UiState.Loading

                val result = getTestByIdUseCase(testId)
                result.fold(
                    onSuccess = { test ->
                        _testState.value = UiState.Success(test)
                    },
                    onFailure = { exception ->
                        _testState.value = UiState.Error(
                            exception.message ?: "Failed to load test"
                        )
                    }
                )
            } catch (e: Exception) {
                _testState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun checkPendingAttempt(userId: Long) {
        viewModelScope.launch {
            try {
                val result = getPendingTestAttemptUseCase(userId)
                result.fold(
                    onSuccess = { attempt ->
                        _pendingAttempt.value = attempt
                    },
                    onFailure = {
                        _pendingAttempt.value = null
                    }
                )
            } catch (e: Exception) {
                _pendingAttempt.value = null
            }
        }
    }
}

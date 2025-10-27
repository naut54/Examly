package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.domain.usecase.assignment.GetAssignedTestsUseCase
import com.octal.examly.presentation.state.FilterState
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestListViewModel @Inject constructor(
    private val getAssignedTestsUseCase: GetAssignedTestsUseCase
) : ViewModel() {

    private val _testsState = MutableStateFlow<UiState<List<TestAssignment>>>(UiState.Loading)
    val testsState: StateFlow<UiState<List<TestAssignment>>> = _testsState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private var allTests: List<TestAssignment> = emptyList()

    fun loadAssignedTests(userId: Long) {
        viewModelScope.launch {
            try {
                _testsState.value = UiState.Loading

                getAssignedTestsUseCase(userId).collect { tests ->
                    allTests = tests
                    applyFiltersAndSort()
                }
            } catch (e: Exception) {
                _testsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun applyFilters(filterState: FilterState) {
        _filterState.value = filterState
        applyFiltersAndSort()
    }

    fun clearFilters() {
        _filterState.value = FilterState()
        applyFiltersAndSort()
    }

    fun sortTests(sortBy: String) {
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        var filtered = allTests

        val filter = _filterState.value

        when (filter.isCompleted) {
            true -> {
                filtered = filtered.filter { it.status == com.octal.examly.domain.model.TestAssignmentStatus.COMPLETED }
            }
            false -> {
                filtered = filtered.filter { it.status != com.octal.examly.domain.model.TestAssignmentStatus.COMPLETED }
            }
            null -> {
            }
        }

        _testsState.value = if (filtered.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(filtered)
        }
    }
}

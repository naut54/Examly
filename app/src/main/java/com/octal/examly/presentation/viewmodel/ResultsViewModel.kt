package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.usecase.result.GetAllResultsUseCase
import com.octal.examly.domain.usecase.result.GetUserResultsUseCase
import com.octal.examly.presentation.state.FilterState
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val getUserResultsUseCase: GetUserResultsUseCase,
    private val getAllResultsUseCase: GetAllResultsUseCase
) : ViewModel() {

    private val _resultsState = MutableStateFlow<UiState<List<TestResult>>>(UiState.Loading)
    val resultsState: StateFlow<UiState<List<TestResult>>> = _resultsState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    fun loadUserResults(userId: Long) {
        viewModelScope.launch {
            try {
                _resultsState.value = UiState.Loading

                getUserResultsUseCase(userId).collect { results ->
                    _resultsState.value = if (results.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(results)
                    }
                }
            } catch (e: Exception) {
                _resultsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadAllResults() {
        viewModelScope.launch {
            try {
                _resultsState.value = UiState.Loading

                getAllResultsUseCase().collect { results ->
                    _resultsState.value = if (results.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(results)
                    }
                }
            } catch (e: Exception) {
                _resultsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun applyFilters(filterState: FilterState) {
        _filterState.value = filterState
    }

    fun clearFilters() {
        _filterState.value = FilterState()
    }
}

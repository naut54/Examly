package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.GlobalMetrics
import com.octal.examly.domain.model.PlatformMetrics
import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.usecase.metrics.GetGlobalMetricsUseCase
import com.octal.examly.domain.usecase.result.GetMetricsBySubjectUseCase
import com.octal.examly.domain.usecase.result.GetMetricsByUserUseCase
import com.octal.examly.domain.usecase.subject.GetAllSubjectsUseCase
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetricsViewModel @Inject constructor(
    private val getGlobalMetricsUseCase: GetGlobalMetricsUseCase,
    private val getMetricsBySubjectUseCase: GetMetricsBySubjectUseCase,
    private val getMetricsByUserUseCase: GetMetricsByUserUseCase,
    private val getAllSubjectsUseCase: GetAllSubjectsUseCase
) : ViewModel() {

    private val _metricsState = MutableStateFlow<UiState<GlobalMetrics>>(UiState.Loading)
    val metricsState: StateFlow<UiState<GlobalMetrics>> = _metricsState.asStateFlow()

    private val _subjectsState = MutableStateFlow<UiState<List<Subject>>>(UiState.Loading)
    val subjectsState: StateFlow<UiState<List<Subject>>> = _subjectsState.asStateFlow()

    private val _platformMetrics = MutableStateFlow<PlatformMetrics?>(null)
    val platformMetrics: StateFlow<PlatformMetrics?> = _platformMetrics.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            try {
                getAllSubjectsUseCase().collect { subjects ->
                    _subjectsState.value = UiState.Success(subjects)
                }
            } catch (e: Exception) {
                _subjectsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadGlobalMetrics() {
        viewModelScope.launch {
            try {
                _metricsState.value = UiState.Loading

                val result = getGlobalMetricsUseCase()

                result.fold(
                    onSuccess = { metrics ->
                        _metricsState.value = UiState.Success(metrics)
                    },
                    onFailure = { exception ->
                        _metricsState.value = UiState.Error(
                            exception.message ?: "Error al cargar métricas globales"
                        )
                    }
                )
            } catch (e: Exception) {
                _metricsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadMetricsBySubject(subjectId: Long, userId: Long? = null) {
        viewModelScope.launch {
            try {
                _metricsState.value = UiState.Loading

                val result = getMetricsBySubjectUseCase(subjectId, userId)

                result.fold(
                    onSuccess = { metricsMap ->
                        val metrics = mapToGlobalMetrics(metricsMap)
                        _metricsState.value = UiState.Success(metrics)
                    },
                    onFailure = { exception ->
                        _metricsState.value = UiState.Error(
                            exception.message ?: "Error al cargar métricas de asignatura"
                        )
                    }
                )
            } catch (e: Exception) {
                _metricsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadMetricsByUser(userId: Long) {
        viewModelScope.launch {
            try {
                _metricsState.value = UiState.Loading

                val result = getMetricsByUserUseCase(userId)

                result.fold(
                    onSuccess = { metricsMap ->
                        val metrics = mapToGlobalMetrics(metricsMap)
                        _metricsState.value = UiState.Success(metrics)
                    },
                    onFailure = { exception ->
                        _metricsState.value = UiState.Error(
                            exception.message ?: "Error al cargar métricas de usuario"
                        )
                    }
                )
            } catch (e: Exception) {
                _metricsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadPlatformMetrics() {
        viewModelScope.launch {
            try {
                val result = getGlobalMetricsUseCase()

                result.fold(
                    onSuccess = { globalMetrics ->
                        _platformMetrics.value = PlatformMetrics(
                            totalUsers = globalMetrics.totalUsers,
                            totalTests = globalMetrics.totalTests,
                            totalResults = globalMetrics.totalResults,
                            averageScore = globalMetrics.averageScore
                        )
                    },
                    onFailure = {
                        _platformMetrics.value = PlatformMetrics()
                    }
                )
            } catch (e: Exception) {
                _platformMetrics.value = PlatformMetrics()
            }
        }
    }

    private fun mapToGlobalMetrics(metricsMap: Map<String, Any>): GlobalMetrics {
        val average = metricsMap["average"] as? Double ?: 0.0
        val total = metricsMap["total"] as? Int ?: 0
        val passed = metricsMap["passed"] as? Int ?: 0
        val failed = metricsMap["failed"] as? Int ?: 0

        val passRate = if (total > 0) passed.toDouble() / total else 0.0
        val failRate = if (total > 0) failed.toDouble() / total else 0.0

        return GlobalMetrics(
            totalResults = total,
            averageScore = average,
            passRate = passRate,
            failRate = failRate
        )
    }
}

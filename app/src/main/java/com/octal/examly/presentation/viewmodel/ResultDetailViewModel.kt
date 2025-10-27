package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.model.UserAnswer
import com.octal.examly.domain.repository.QuestionRepository
import com.octal.examly.domain.repository.TestAttemptRepository
import com.octal.examly.domain.usecase.result.GetResultDetailUseCase
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultDetailViewModel @Inject constructor(
    private val getResultDetailUseCase: GetResultDetailUseCase,
    private val testAttemptRepository: TestAttemptRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _resultDetailState = MutableStateFlow<UiState<TestResult>>(UiState.Idle)
    val resultDetailState: StateFlow<UiState<TestResult>> = _resultDetailState.asStateFlow()

    private var cachedUserAnswers: List<UserAnswer> = emptyList()

    fun loadResultDetail(resultId: Long) {
        viewModelScope.launch {
            try {
                _resultDetailState.value = UiState.Loading

                if (resultId <= 0) {
                    _resultDetailState.value = UiState.Error("ID de resultado inválido")
                    return@launch
                }

                val resultResult = getResultDetailUseCase(resultId)

                resultResult.fold(
                    onSuccess = { result ->
                        val attemptResult = testAttemptRepository.resumeAttempt(result.attemptId)

                        attemptResult.fold(
                            onSuccess = { attempt ->
                                val enrichedAnswers = attempt.userAnswers.map { userAnswer ->
                                    val questionResult = questionRepository.getQuestionById(userAnswer.questionId)

                                    questionResult.fold(
                                        onSuccess = { question ->
                                            val selectedAnswers = question.answers.filter { answer ->
                                                userAnswer.selectedAnswerIds.contains(answer.id)
                                            }

                                            userAnswer.copy(
                                                question = question,
                                                selectedAnswers = selectedAnswers
                                            )
                                        },
                                        onFailure = {
                                            userAnswer
                                        }
                                    )
                                }

                                cachedUserAnswers = enrichedAnswers
                                _resultDetailState.value = UiState.Success(result)
                            },
                            onFailure = { exception ->
                                cachedUserAnswers = emptyList()
                                _resultDetailState.value = UiState.Success(result)
                            }
                        )
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("no encontrado", ignoreCase = true) == true ||
                            exception.message?.contains("not found", ignoreCase = true) == true) {
                            _resultDetailState.value = UiState.Empty
                        } else {
                            _resultDetailState.value = UiState.Error(
                                exception.message ?: "Error al cargar resultado"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _resultDetailState.value = UiState.Error(
                    e.message ?: "Error inesperado al cargar resultado"
                )
            }
        }
    }

    fun getUserAnswers(result: TestResult): List<UserAnswer> {
        return cachedUserAnswers
    }
}

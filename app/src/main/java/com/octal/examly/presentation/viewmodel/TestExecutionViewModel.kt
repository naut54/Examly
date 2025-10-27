package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.usecase.attempt.*
import com.octal.examly.presentation.state.TestExecutionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestExecutionViewModel @Inject constructor(
    private val startTestAttemptUseCase: StartTestAttemptUseCase,
    private val resumeTestAttemptUseCase: ResumeTestAttemptUseCase,
    private val saveTestProgressUseCase: SaveTestProgressUseCase,
    private val saveUserAnswerUseCase: SaveUserAnswerUseCase,
    private val submitTestUseCase: SubmitTestUseCase,
    private val getAssignmentByIdUseCase: com.octal.examly.domain.usecase.assignment.GetAssignmentByIdUseCase,
    private val getTestQuestionsUseCase: com.octal.examly.domain.usecase.test.GetTestQuestionsUseCase
) : ViewModel() {

    private val _executionState = MutableStateFlow<TestExecutionState>(TestExecutionState.NotStarted)
    val executionState: StateFlow<TestExecutionState> = _executionState.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _timeRemaining = MutableStateFlow<Long?>(null)
    val timeRemaining: StateFlow<Long?> = _timeRemaining.asStateFlow()

    private var timerJob: Job? = null
    private var currentAttempt: TestAttempt? = null

    fun startTest(assignmentId: Long, isPracticeMode: Boolean) {
        viewModelScope.launch {
            try {
                _executionState.value = TestExecutionState.Loading

                val mode = if (isPracticeMode) com.octal.examly.domain.model.TestAttemptMode.PRACTICE
                else com.octal.examly.domain.model.TestAttemptMode.EXAM

                val assignmentResult = getAssignmentByIdUseCase(assignmentId)
                assignmentResult.fold(
                    onSuccess = { assignment ->
                        val startResult = startTestAttemptUseCase(assignmentId, assignment.testId, assignment.userId, mode)
                        startResult.fold(
                            onSuccess = { attemptId ->
                                val resumeResult = resumeTestAttemptUseCase(attemptId)
                                resumeResult.fold(
                                    onSuccess = { attempt ->
                                        val questionsResult = getTestQuestionsUseCase(assignment.testId)
                                        questionsResult.fold(
                                            onSuccess = { questions ->
                                                if (questions.isEmpty()) {
                                                    _executionState.value = TestExecutionState.Error("No hay preguntas para este test", canRetry = false)
                                                    return@fold
                                                }

                                                val attemptWithQuestions = attempt.copy(questions = questions)
                                                currentAttempt = attemptWithQuestions
                                                _currentQuestionIndex.value = attemptWithQuestions.currentQuestionIndex
                                                _executionState.value = TestExecutionState.InProgress(attemptWithQuestions)

                                                attemptWithQuestions.timeRemaining?.let { remaining ->
                                                    if (remaining > 0) startTimer(remaining.toLong() * 1000L)
                                                }
                                            },
                                            onFailure = { qEx ->
                                                _executionState.value = TestExecutionState.Error(qEx.message ?: "No se pudieron cargar las preguntas")
                                            }
                                        )
                                    },
                                    onFailure = { ex ->
                                        _executionState.value = TestExecutionState.Error(ex.message ?: "Failed to initialize test")
                                    }
                                )
                            },
                            onFailure = { exception ->
                                _executionState.value = TestExecutionState.Error(exception.message ?: "Failed to start test")
                            }
                        )
                    },
                    onFailure = { aex ->
                        _executionState.value = TestExecutionState.Error(aex.message ?: "Asignación no encontrada")
                    }
                )
            } catch (e: Exception) {
                _executionState.value = TestExecutionState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resumeTest(attemptId: Long) {
        viewModelScope.launch {
            try {
                _executionState.value = TestExecutionState.Loading

                val result = resumeTestAttemptUseCase(attemptId)
                result.fold(
                    onSuccess = { attempt ->
                        val assignmentResult = getAssignmentByIdUseCase(attempt.assignmentId)
                        assignmentResult.fold(
                            onSuccess = { assignment ->
                                val questionsResult = getTestQuestionsUseCase(assignment.testId)
                                questionsResult.fold(
                                    onSuccess = { questions ->
                                        if (questions.isEmpty()) {
                                            _executionState.value = TestExecutionState.Error("No hay preguntas para este test", canRetry = false)
                                            return@fold
                                        }

                                        val attemptWithQuestions = attempt.copy(questions = questions)
                                        currentAttempt = attemptWithQuestions
                                        _currentQuestionIndex.value = attemptWithQuestions.currentQuestionIndex
                                        _executionState.value = TestExecutionState.InProgress(attemptWithQuestions)

                                        attemptWithQuestions.timeRemaining?.let { remainingSeconds ->
                                            val remaining = remainingSeconds.toLong() * 1000L
                                            if (remaining > 0L) {
                                                startTimer(remaining)
                                            }
                                        }
                                    },
                                    onFailure = { qex ->
                                        _executionState.value = TestExecutionState.Error(qex.message ?: "No se pudieron cargar las preguntas")
                                    }
                                )
                            },
                            onFailure = { aex ->
                                _executionState.value = TestExecutionState.Error(aex.message ?: "Asignación no encontrada")
                            }
                        )
                    },
                    onFailure = { exception ->
                        _executionState.value = TestExecutionState.Error(
                            exception.message ?: "Failed to resume test"
                        )
                    }
                )
            } catch (e: Exception) {
                _executionState.value = TestExecutionState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun saveProgress(questionIndex: Int) {
        viewModelScope.launch {
            currentAttempt?.let { attempt ->
                saveTestProgressUseCase(attempt.id, questionIndex)
                _currentQuestionIndex.value = questionIndex
            }
        }
    }

    fun saveAnswer(questionId: Long, answerIds: List<Long>) {
        viewModelScope.launch {
            currentAttempt?.let { attempt ->
                val result = saveUserAnswerUseCase(attempt.id, questionId, answerIds)

                result.onSuccess { answerId ->
                    val newUserAnswer = com.octal.examly.domain.model.UserAnswer(
                        id = answerId,
                        attemptId = attempt.id,
                        questionId = questionId,
                        selectedAnswerIds = answerIds,
                        answeredAt = System.currentTimeMillis()
                    )

                    val updatedAnswers = attempt.userAnswers
                        .filter { it.questionId != questionId }
                        .plus(newUserAnswer)

                    currentAttempt = attempt.copy(userAnswers = updatedAnswers)
                }
            }
        }
    }

    fun submitTest() {
        viewModelScope.launch {
            try {
                _executionState.value = TestExecutionState.Submitting

                currentAttempt?.let { attempt ->
                    stopTimer()

                    val result = submitTestUseCase(attempt.id, 0.0)
                    result.fold(
                        onSuccess = { resultId ->
                            _executionState.value = TestExecutionState.Completed(resultId, 0f)
                        },
                        onFailure = { exception ->
                            _executionState.value = TestExecutionState.Error(
                                exception.message ?: "Failed to submit test"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _executionState.value = TestExecutionState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun startTimer(initialTime: Long = 0) {
        stopTimer()

        var time = initialTime
        if (time == 0L) {
            currentAttempt?.let { attempt ->
                attempt.timeRemaining?.let { remainingSeconds ->
                    time = remainingSeconds.toLong() * 1000L
                }
            }
        }

        timerJob = viewModelScope.launch {
            _timeRemaining.value = time
            while (time > 0) {
                delay(1000)
                time -= 1000
                _timeRemaining.value = time
            }
            _timeRemaining.value = 0
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun resumeTimer() {
        _timeRemaining.value?.let { remaining ->
            if (remaining > 0) {
                startTimer(remaining)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun getAnsweredQuestionCount(): Int {
        return currentAttempt?.userAnswers?.size ?: 0
    }

    fun getUnansweredQuestionCount(): Int {
        return currentAttempt?.let { attempt ->
            val answeredCount = attempt.userAnswers.size
            attempt.questions.size - answeredCount
        } ?: 0
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}

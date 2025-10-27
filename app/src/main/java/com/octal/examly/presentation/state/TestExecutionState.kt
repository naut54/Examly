package com.octal.examly.presentation.state

import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.model.TestAttemptMode
import com.octal.examly.domain.model.UserAnswer

sealed class TestExecutionState {
    object NotStarted : TestExecutionState()

    object Loading : TestExecutionState()
    object Initializing : TestExecutionState()

    data class InProgress(
        val attempt: TestAttempt,
        val questions: List<Question> = attempt.questions,
        val currentQuestionIndex: Int = attempt.currentQuestionIndex,
        val userAnswers: Map<Long, UserAnswer> = emptyMap(),
        val timeRemaining: Long? = attempt.timeRemaining?.toLong(),
        val mode: TestAttemptMode = attempt.mode
    ) : TestExecutionState() {
        fun getCurrentQuestion(): Question? =
            questions.getOrNull(currentQuestionIndex)

        fun getCurrentAnswer(): UserAnswer? =
            getCurrentQuestion()?.let { userAnswers[it.id] }

        fun isCurrentQuestionAnswered(): Boolean =
            getCurrentAnswer() != null

        fun getAnsweredCount(): Int = userAnswers.size

        fun getTotalQuestions(): Int = questions.size

        fun getProgressPercentage(): Float =
            if (questions.isEmpty()) 0f
            else (userAnswers.size.toFloat() / questions.size.toFloat()) * 100f

        fun isFirstQuestion(): Boolean = currentQuestionIndex == 0

        fun isLastQuestion(): Boolean =
            currentQuestionIndex == questions.size - 1

        fun areAllQuestionsAnswered(): Boolean =
            userAnswers.size == questions.size

        fun isTimerLow(): Boolean =
            timeRemaining != null && timeRemaining < 60_000

        fun isTimerExpired(): Boolean =
            timeRemaining != null && timeRemaining <= 0
    }

    data class Paused(
        val attempt: TestAttempt,
        val currentQuestionIndex: Int
    ) : TestExecutionState()

    data class ReadyToSubmit(
        val attempt: TestAttempt,
        val answeredCount: Int,
        val totalQuestions: Int
    ) : TestExecutionState() {
        fun getUnansweredCount(): Int = totalQuestions - answeredCount
    }

    object Submitting : TestExecutionState()

    data class Completed(
        val resultId: Long,
        val score: Float = 0f
    ) : TestExecutionState()

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : TestExecutionState()
}

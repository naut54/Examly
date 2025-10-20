package com.octal.examly.domain.model

data class TestAttempt(
    val id: Long = 0,
    val assignmentId: Long,
    val userId: Long,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val score: Double? = null,
    val mode: TestAttemptMode,
    val currentQuestionIndex: Int = 0,
    val isPaused: Boolean = false,
    val timeRemaining: Int?,
    val questions: List<Question> = emptyList(),
    val userAnswers: List<UserAnswer> = emptyList()
)
package com.octal.examly.domain.model

data class UserAnswer(
    val id: Long = 0,
    val attemptId: Long,
    val questionId: Long,
    val question: Question? = null,
    val selectedAnswerIds: List<Long>,
    val selectedAnswers: List<Answer> = emptyList(),
    val isCorrect: Boolean = false,
    val answeredAt: Long = System.currentTimeMillis()
)
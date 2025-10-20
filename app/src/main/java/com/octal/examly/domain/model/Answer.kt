package com.octal.examly.domain.model

data class Answer(
    val id: Long = 0,
    val questionId: Long = 0,
    val answerText: String,
    val isCorrect: Boolean
)
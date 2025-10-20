package com.octal.examly.domain.model

data class Question(
    val id: Long = 0,
    val subjectId: Long,
    val questionText: String,
    val imageUri: String? = null,
    val explanation: String? = null,
    val type: QuestionType,
    val answers: List<Answer> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
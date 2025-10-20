package com.octal.examly.domain.model

data class TestConfiguration(
    val hasPracticeMode: Boolean = true,
    val hasTimer: Boolean = false,
    val timeLimit: Int? = null,
    val numberOfQuestions: Int?,
    val questionSelection: QuestionSelection = QuestionSelection.MANUAL
)

enum class QuestionSelection {
    MANUAL,
    RANDOM
}
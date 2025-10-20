package com.octal.examly.domain.model

data class TestResult(
    val id: Long = 0,
    val attemptId: Long,
    val userId: Long,
    val testId: Long,
    val score: Double,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val timeSpent: Long? = null,
    val mode: TestAttemptMode,
    val completedAt: Long = System.currentTimeMillis(),
    val isPassed: Boolean = false
) {
    companion object {
        const val PASSING_SCORE = 50.0
    }

    fun calculateIsPassed(): Boolean {
        return score >= PASSING_SCORE
    }
}
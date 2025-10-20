package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.TestResultEntity
import com.octal.examly.domain.model.TestAttemptMode
import com.octal.examly.domain.model.TestResult

fun TestResultEntity.toDomain(): TestResult {
    return TestResult(
        id = this.id,
        attemptId = this.attemptId,
        userId = this.userId,
        testId = this.testId,
        score = this.score,
        totalQuestions = this.totalQuestions,
        correctAnswers = this.correctAnswers,
        wrongAnswers = this.wrongAnswers,
        timeSpent = this.timeSpent,
        mode = TestAttemptMode.valueOf(this.mode),
        completedAt = this.completedAt,
        isPassed = this.score >= TestResult.PASSING_SCORE
    )
}

fun TestResult.toEntity(): TestResultEntity {
    return TestResultEntity(
        id = this.id,
        attemptId = this.attemptId,
        userId = this.userId,
        testId = this.testId,
        score = this.score,
        totalQuestions = this.totalQuestions,
        correctAnswers = this.correctAnswers,
        wrongAnswers = this.wrongAnswers,
        timeSpent = this.timeSpent,
        mode = this.mode.name,
        completedAt = this.completedAt
    )
}
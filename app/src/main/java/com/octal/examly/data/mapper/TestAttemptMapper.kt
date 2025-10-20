package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.TestAttemptEntity
import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.model.TestAttemptMode

fun TestAttemptEntity.toDomain(): TestAttempt {
    return TestAttempt(
        id = this.id,
        assignmentId = this.assignmentId,
        userId = this.userId,
        startedAt = this.startedAt,
        completedAt = this.completedAt,
        score = this.score,
        mode = TestAttemptMode.valueOf(this.mode),
        currentQuestionIndex = this.currentQuestionIndex,
        isPaused = this.isPaused,
        timeRemaining = this.timeRemaining,
        questions = emptyList(),
        userAnswers = emptyList()
    )
}

fun TestAttempt.toEntity(): TestAttemptEntity {
    return TestAttemptEntity(
        id = this.id,
        assignmentId = this.assignmentId,
        userId = this.userId,
        startedAt = this.startedAt,
        completedAt = this.completedAt,
        score = this.score,
        mode = this.mode.name,
        currentQuestionIndex = this.currentQuestionIndex,
        isPaused = this.isPaused,
        timeRemaining = this.timeRemaining
    )
}
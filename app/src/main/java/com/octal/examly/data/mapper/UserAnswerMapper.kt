package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.UserAnswerEntity
import com.octal.examly.domain.model.UserAnswer
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

fun UserAnswerEntity.toDomain(): UserAnswer {
    val answerIds = try {
        Json.decodeFromString<List<Long>>(this.selectedAnswerIds)
    } catch (e: Exception) {
        emptyList()
    }

    return UserAnswer(
        id = this.id,
        attemptId = this.attemptId,
        questionId = this.questionId,
        selectedAnswerIds = answerIds,
        isCorrect = this.isCorrect,
        answeredAt = this.answeredAt
    )
}

fun UserAnswer.toEntity(): UserAnswerEntity {
    val answerIdsJson = Json.encodeToString(this.selectedAnswerIds)

    return UserAnswerEntity(
        id = this.id,
        attemptId = this.attemptId,
        questionId = this.questionId,
        selectedAnswerIds = answerIdsJson,
        isCorrect = this.isCorrect,
        answeredAt = this.answeredAt
    )
}
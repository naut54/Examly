package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.AnswerEntity
import com.octal.examly.domain.model.Answer

fun AnswerEntity.toDomain(): Answer {
    return Answer(
        id = this.id,
        questionId = this.questionId,
        answerText = this.answerText,
        isCorrect = this.isCorrect
    )
}

fun Answer.toEntity(questionId: Long): AnswerEntity {
    return AnswerEntity(
        id = this.id,
        questionId = questionId,
        answerText = this.answerText,
        isCorrect = this.isCorrect
    )
}
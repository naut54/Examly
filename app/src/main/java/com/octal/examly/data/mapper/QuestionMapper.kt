package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.QuestionEntity
import com.octal.examly.domain.model.Answer
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.QuestionType

fun QuestionEntity.toDomain(answers: List<Answer> = emptyList()): Question {
    return Question(
        id = this.id,
        subjectId = this.subjectId,
        questionText = this.questionText,
        imageUri = this.imageUri,
        explanation = this.explanation,
        type = QuestionType.valueOf(this.type),
        answers = answers,
        createdAt = this.createdAt
    )
}

fun Question.toEntity(): QuestionEntity {
    return QuestionEntity(
        id = this.id,
        subjectId = this.subjectId,
        questionText = this.questionText,
        imageUri = this.imageUri,
        explanation = this.explanation,
        type = this.type.name,
        createdAt = this.createdAt
    )
}
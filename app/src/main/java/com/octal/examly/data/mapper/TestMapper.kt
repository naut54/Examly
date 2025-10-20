package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.TestEntity
import com.octal.examly.domain.model.QuestionSelection
import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.TestConfiguration
import com.octal.examly.domain.model.TestMode

fun TestEntity.toDomain(): Test {
    return Test(
        id = this.id,
        title = this.title,
        description = this.description,
        subjectId = this.subjectId,
        mode = TestMode.valueOf(this.mode),
        configuration = TestConfiguration(
            hasPracticeMode = this.hasPracticeMode,
            hasTimer = this.hasTimer,
            timeLimit = this.timeLimit,
            numberOfQuestions = this.numberOfQuestions,
            questionSelection = if (this.mode == "FIXED")
                QuestionSelection.MANUAL
            else
                QuestionSelection.RANDOM
        ),
        createdBy = this.createdBy,
        createdAt = this.createdAt
    )
}

fun Test.toEntity(): TestEntity {
    return TestEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        subjectId = this.subjectId,
        mode = this.mode.name,
        hasPracticeMode = this.configuration.hasPracticeMode,
        hasTimer = this.configuration.hasTimer,
        timeLimit = this.configuration.timeLimit,
        numberOfQuestions = this.configuration.numberOfQuestions,
        createdBy = this.createdBy,
        createdAt = this.createdAt
    )
}
package com.octal.examly.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_questions",
    foreignKeys = [
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["testId"]),
        Index(value = ["questionId"]),
        Index(value = ["testId", "orderIndex"], unique = true)
    ]
)
data class TestQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val testId: Int,

    val questionId: Int,

    val orderIndex: Int
)
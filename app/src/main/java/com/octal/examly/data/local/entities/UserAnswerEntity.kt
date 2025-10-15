package com.octal.examly.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_answers",
    foreignKeys = [
        ForeignKey(
            entity = TestAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
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
        Index(value = ["attemptId"]),
        Index(value = ["questionId"]),
        Index(value = ["attemptId", "questionId"], unique = true)
    ]
)
data class UserAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val attemptId: Int,

    val questionId: Int,

    val selectedAnswerIds: String,

    val isCorrect: Boolean,

    val answeredAt: Long
)
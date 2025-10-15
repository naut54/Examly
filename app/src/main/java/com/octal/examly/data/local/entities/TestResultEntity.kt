package com.octal.examly.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_results",
    foreignKeys = [
        ForeignKey(
            entity = TestAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["attemptId"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["testId"]),
        Index(value = ["completedAt"]),
        Index(value = ["mode"])
    ]
)
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val attemptId: Int,

    val userId: Int,

    val testId: Int,

    val score: Double,

    val totalQuestions: Int,

    val correctAnswers: Int,

    val wrongAnswers: Int,

    val timeSpent: Int?,

    val mode: String,

    val completedAt: Long
)
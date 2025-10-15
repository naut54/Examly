package com.octal.examly.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_attempts",
    foreignKeys = [
        ForeignKey(
            entity = TestAssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assignmentId"])]
)
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val assignmentId: Int,

    val startedAt: Long,

    val completedAt: Long?,

    val score: Double?,

    val mode: String,

    val currentQuestionIndex: Int,

    val isPaused: Boolean,

    val timeRemaining: Int?
)
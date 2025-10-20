package com.octal.examly.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_assignments",
    foreignKeys = [
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignedBy"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["testId"]),
        Index(value = ["userId"]),
        Index(value = ["assignedBy"]),
        Index(value = ["status"]),
        Index(value = ["userId", "testId"], unique = true)
    ]
)
data class TestAssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val testId: Long,

    val userId: Long,

    val assignedBy: Long?,

    val assignedAt: Long,

    val deadline: Long?,

    val status: String
)
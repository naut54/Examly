package com.octal.examly.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tests",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["createdBy"])
    ]
)
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String,

    val subjectId: Int,

    val mode: String, // "FIXED" o "RANDOM"

    val hasPracticeMode: Boolean,

    val hasTimer: Boolean,

    val timeLimit: Int?,

    val numberOfQuestions: Int?,

    val createdBy: Int?, // ID del admin que lo creo

    val createdAt: Long
)
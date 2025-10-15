package com.octal.examly.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.octal.examly.data.local.dao.*
import com.octal.examly.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        QuestionEntity::class,
        AnswerEntity::class,
        TestEntity::class,
        TestQuestionEntity::class,
        TestAssignmentEntity::class,
        TestAttemptEntity::class,
        UserAnswerEntity::class,
        TestResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ExamDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun subjectDao(): SubjectDao

    abstract fun questionDao(): QuestionDao

    abstract fun answerDao(): AnswerDao

    abstract fun testDao(): TestDao

    abstract fun testQuestionDao(): TestQuestionDao

    abstract fun testAssignmentDao(): TestAssignmentDao

    abstract fun testAttemptDao(): TestAttemptDao

    abstract fun userAnswerDao(): UserAnswerDao

    abstract fun testResultDao(): TestResultDao
}
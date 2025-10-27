package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.TestQuestionEntity

@Dao
interface TestQuestionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(testQuestion: TestQuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(testQuestions: List<TestQuestionEntity>): List<Long>

    @Query("SELECT * FROM test_questions WHERE testId = :testId")
    suspend fun getByTestId(testId: Int): List<TestQuestionEntity>

    @Query("DELETE FROM test_questions WHERE testId = :testId")
    suspend fun deleteByTestId(testId: Long): Int

    @Query("SELECT * FROM test_questions WHERE testId = :testId")
    suspend fun getQuestionsByTestId(testId: Long): List<TestQuestionEntity>

    @Delete
    suspend fun delete(testQuestion: TestQuestionEntity): Int
}
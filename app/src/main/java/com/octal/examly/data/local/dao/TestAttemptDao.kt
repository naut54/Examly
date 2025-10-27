package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.TestAttemptEntity

@Dao
interface TestAttemptDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(testAttempt: TestAttemptEntity): Long

    @Query("SELECT * FROM test_attempts WHERE id = :id")
    suspend fun getById(id: Long): TestAttemptEntity?

    @Query("SELECT * FROM test_attempts WHERE assignmentId = :assignmentId")
    suspend fun getByAssignmentId(assignmentId: Long): TestAttemptEntity?

    @Query("SELECT * FROM test_attempts WHERE userId = :userId AND isPaused = 1")
    suspend fun getPendingAttemptByUser(userId: Long): TestAttemptEntity?

    @Query("UPDATE test_attempts SET currentQuestionIndex = :questionIndex WHERE id = :attemptId")
    suspend fun updateProgress(attemptId: Long, questionIndex: Int): Int

    @Query("UPDATE test_attempts SET score = :score, completedAt = :completedAt, isPaused = 0 WHERE id = :attemptId")
    suspend fun complete(attemptId: Long, score: Double, completedAt: Long): Int

    @Query("DELETE FROM test_attempts WHERE id = :attemptId")
    suspend fun deleteById(attemptId: Long): Int

    @Delete
    suspend fun delete(testAttempt: TestAttemptEntity): Int
}
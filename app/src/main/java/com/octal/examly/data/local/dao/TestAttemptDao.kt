package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.TestAttemptEntity

@Dao
interface TestAttemptDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(testAttempt: TestAttemptEntity)

    @Query("SELECT * FROM test_attempts WHERE id = :id")
    suspend fun getById(id: Int): TestAttemptEntity?

    @Query("SELECT * FROM test_attempts WHERE assignmentId = :assignmentId")
    suspend fun getByAssignmentId(assignmentId: Int): TestAttemptEntity?

    @Query("SELECT * FROM test_attempts WHERE userId = :userId AND isPaused = 1")
    suspend fun getPendingAttemptByUser(userId: Int)

    @Update
    suspend fun updateProgress(testAttempt: TestAttemptEntity)

    @Update
    suspend fun complete(testAttempt: TestAttemptEntity)

    @Delete
    suspend fun delete(testAttempt: TestAttemptEntity)
}
package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.TestResultEntity

@Dao
interface TestResultDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(testResult: TestResultEntity)

    @Query("SELECT * FROM test_results WHERE userId = :userId")
    suspend fun getByUserId(userId: Int): List<TestResultEntity>

    @Query("SELECT * FROM test_results WHERE id = :testResultId")
    suspend fun getById(testResultId: Int): TestResultEntity?

    @Query("SELECT * FROM test_results")
    suspend fun getAll(): List<TestResultEntity>
}
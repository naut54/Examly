package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.TestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(testResult: TestResultEntity): Long

    @Query("SELECT * FROM test_results WHERE userId = :userId")
    fun getByUserId(userId: Long): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE id = :testResultId")
    suspend fun getById(testResultId: Long): TestResultEntity?

    @Query("SELECT * FROM test_results")
    fun getAllResults(): Flow<List<TestResultEntity>>

    // New queries for metrics calculation

    @Query("SELECT * FROM test_results WHERE userId = :userId")
    suspend fun getResultsByUserId(userId: Long): List<TestResultEntity>

    @Query("""
        SELECT tr.* FROM test_results tr
        INNER JOIN test_attempts ta ON tr.attemptId = ta.id
        INNER JOIN test_assignments tass ON ta.assignmentId = tass.id
        WHERE tass.testId IN (SELECT id FROM tests WHERE subjectId = :subjectId)
    """)
    suspend fun getResultsBySubjectId(subjectId: Long): List<TestResultEntity>

    @Query("""
        SELECT tr.* FROM test_results tr
        INNER JOIN test_attempts ta ON tr.attemptId = ta.id
        INNER JOIN test_assignments tass ON ta.assignmentId = tass.id
        WHERE tass.testId IN (SELECT id FROM tests WHERE subjectId = :subjectId)
        AND tr.userId = :userId
    """)
    suspend fun getResultsBySubjectAndUser(subjectId: Long, userId: Long): List<TestResultEntity>

    @Query("SELECT COUNT(*) FROM test_results")
    suspend fun getTotalResultsCount(): Int

    @Query("SELECT AVG(score) FROM test_results")
    suspend fun getAverageScore(): Double?

    @Query("SELECT AVG(timeSpent) FROM test_results WHERE timeSpent IS NOT NULL")
    suspend fun getAverageTimeSpent(): Long?

    @Query("SELECT COUNT(*) FROM test_results WHERE score >= 50.0")
    suspend fun getPassedCount(): Int

    @Query("SELECT COUNT(*) FROM test_results WHERE score < 50.0")
    suspend fun getFailedCount(): Int

    @Query("SELECT COUNT(*) FROM test_results WHERE userId = :userId ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentCountByUserId(userId: Long, limit: Int = 5): Int

    @Query("SELECT * FROM test_results ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentResults(limit: Int = 10): List<TestResultEntity>
}
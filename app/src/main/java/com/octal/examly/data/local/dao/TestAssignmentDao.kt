package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.TestAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(testAssignment: TestAssignmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(testAssignments: List<TestAssignmentEntity>): List<Long>

    @Query("SELECT * FROM test_assignments WHERE userId = :userId")
    fun getByUserId(userId: Long): Flow<List<TestAssignmentEntity>>

    @Query("SELECT * FROM test_assignments WHERE testId = :testId")
    suspend fun getByTestId(testId: Long): List<TestAssignmentEntity>

    @Query("SELECT * FROM test_assignments WHERE id = :testAssignmentId")
    suspend fun getById(testAssignmentId: Long): TestAssignmentEntity?

    @Query("UPDATE test_assignments SET status = :status WHERE id = :assignmentId")
    suspend fun updateStatus(assignmentId: Long, status: String): Int

    @Delete
    suspend fun delete(testAssignment: TestAssignmentEntity): Int

    @Query("SELECT COUNT(*) FROM test_assignments")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(DISTINCT ta.id) FROM test_assignments ta INNER JOIN test_results tr ON ta.id = (SELECT assignmentId FROM test_attempts WHERE id = tr.attemptId)")
    suspend fun getCompletedCount(): Int

    @Query("SELECT COUNT(*) FROM test_assignments WHERE userId = :userId")
    suspend fun getCountByUserId(userId: Long): Int

    @Query("""
        SELECT COUNT(*) FROM test_assignments ta
        WHERE ta.userId = :userId
        AND ta.id NOT IN (
            SELECT DISTINCT assignmentId FROM test_attempts tat
            INNER JOIN test_results tr ON tr.attemptId = tat.id
            WHERE tat.assignmentId = ta.id
        )
    """)
    suspend fun getPendingCountByUserId(userId: Long): Int

    @Query("""
        SELECT * FROM test_assignments
        WHERE userId = :userId
        AND deadline > :currentTime
        AND id NOT IN (
            SELECT DISTINCT assignmentId FROM test_attempts tat
            INNER JOIN test_results tr ON tr.attemptId = tat.id
            WHERE tat.assignmentId = test_assignments.id
        )
        ORDER BY deadline ASC
        LIMIT :limit
    """)
    suspend fun getUpcomingByUserId(userId: Long, currentTime: Long, limit: Int = 5): List<TestAssignmentEntity>
}
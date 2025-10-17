package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.TestAssignmentEntity

@Dao
interface TestAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(testAssignment: TestAssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(testAssignments: List<TestAssignmentEntity>)

    @Query("SELECT * FROM test_assignments WHERE userId = :userId")
    suspend fun getByUserId(userId: Int): List<TestAssignmentEntity>

    @Query("SELECT * FROM test_assignments WHERE testId = :testId")
    suspend fun getByTestId(testId: Int): List<TestAssignmentEntity>

    @Query("SELECT * FROM test_assignments WHERE id = :testAssignmentId")
    suspend fun getById(testAssignmentId: Int): TestAssignmentEntity

    @Update
    suspend fun updateStatus(testAssignment: TestAssignmentEntity)

    @Delete
    suspend fun delete(testAssignment: TestAssignmentEntity)
}
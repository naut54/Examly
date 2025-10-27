package com.octal.examly.domain.repository

import com.octal.examly.domain.model.TestAssignment
import kotlinx.coroutines.flow.Flow

interface TestAssignmentRepository {
    suspend fun assignTest(
        testId: Long,
        userIds: List<Long>,
        assignedBy: Long? = null,
        deadline: Long? = null
    ): Result<List<Long>>

    fun getAssignedTests(userId: Long): Flow<List<TestAssignment>>
    suspend fun getAssignmentById(assignmentId: Long): Result<TestAssignment>
    suspend fun updateAssignmentStatus(assignmentId: Long, status: String): Result<Unit>
}
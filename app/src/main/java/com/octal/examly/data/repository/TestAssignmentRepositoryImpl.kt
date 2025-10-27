package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.TestAssignmentDao
import com.octal.examly.data.local.entities.TestAssignmentEntity
import com.octal.examly.data.mapper.toDomain
import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.domain.repository.TestAssignmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TestAssignmentRepositoryImpl @Inject constructor(
    private val testAssignmentDao: TestAssignmentDao
) : TestAssignmentRepository {

    override suspend fun assignTest(
        testId: Long,
        userIds: List<Long>,
        assignedBy: Long?,
        deadline: Long?
    ): Result<List<Long>> {
        return try {
            if (userIds.isEmpty()) {
                return Result.failure(Exception("Debe seleccionar al menos un usuario"))
            }

            val currentTime = System.currentTimeMillis()
            val assignments = userIds.map { userId ->
                TestAssignmentEntity(
                    id = 0,
                    testId = testId,
                    userId = userId,
                    assignedBy = assignedBy,
                    assignedAt = currentTime,
                    deadline = deadline,
                    status = "PENDING"
                )
            }

            val assignmentIds = testAssignmentDao.insertAll(assignments)
            Result.success(assignmentIds)
        } catch (e: Exception) {
            Result.failure(Exception("Error al asignar test: ${e.message}", e))
        }
    }

    override fun getAssignedTests(userId: Long): Flow<List<TestAssignment>> {
        return testAssignmentDao.getByUserId(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getAssignmentById(assignmentId: Long): Result<TestAssignment> {
        return try {
            val assignmentEntity = testAssignmentDao.getById(assignmentId)
                ?: return Result.failure(Exception("Asignación no encontrada"))

            Result.success(assignmentEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener asignación: ${e.message}", e))
        }
    }

    override suspend fun updateAssignmentStatus(
        assignmentId: Long,
        status: String
    ): Result<Unit> {
        return try {
            testAssignmentDao.updateStatus(assignmentId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar estado: ${e.message}", e))
        }
    }
}
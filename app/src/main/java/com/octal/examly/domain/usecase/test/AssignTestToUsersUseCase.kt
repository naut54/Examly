package com.octal.examly.domain.usecase.test

import com.octal.examly.domain.repository.TestAssignmentRepository
import javax.inject.Inject

class AssignTestToUsersUseCase @Inject constructor(
    private val testAssignmentRepository: TestAssignmentRepository
) {
    suspend operator fun invoke(
        testId: Long,
        userIds: List<Long>,
        deadline: Long? = null,
        assignedBy: Long? = null
    ): Result<List<Long>> {
        if (testId <= 0) {
            return Result.failure(Exception("ID de test inválido"))
        }
        if (userIds.isEmpty()) {
            return Result.failure(Exception("Debe seleccionar al menos un usuario"))
        }
        val normalizedDeadline = deadline?.let { if (it <= 0L) null else it }
        return testAssignmentRepository.assignTest(testId, userIds, assignedBy, normalizedDeadline)
    }
}
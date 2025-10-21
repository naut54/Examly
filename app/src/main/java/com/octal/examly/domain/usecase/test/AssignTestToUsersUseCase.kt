package com.octal.examly.domain.usecase.test

import com.octal.examly.domain.repository.TestAssignmentRepository
import javax.inject.Inject

class AssignTestToUsersUseCase @Inject constructor(
    private val testAssignmentRepository: TestAssignmentRepository
) {
    suspend operator fun invoke(
        testId: Long,
        userIds: List<Long>,
        assignedBy: Long,
        deadline: Long? = null
    ): Result<List<Long>> {
        if (testId <= 0) {
            return Result.failure(Exception("ID de test inválido"))
        }
        if (userIds.isEmpty()) {
            return Result.failure(Exception("Debe seleccionar al menos un usuario"))
        }
        if (assignedBy <= 0) {
            return Result.failure(Exception("ID de asignador inválido"))
        }

        return testAssignmentRepository.assignTest(testId, userIds, assignedBy, deadline)
    }
}
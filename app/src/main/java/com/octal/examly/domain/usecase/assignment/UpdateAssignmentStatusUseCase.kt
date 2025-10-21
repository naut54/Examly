package com.octal.examly.domain.usecase.assignment

import com.octal.examly.domain.repository.TestAssignmentRepository
import javax.inject.Inject

class UpdateAssignmentStatusUseCase @Inject constructor(
    private val testAssignmentRepository: TestAssignmentRepository
) {
    suspend operator fun invoke(assignmentId: Long, status: String): Result<Unit> {
        if (assignmentId <= 0) {
            return Result.failure(Exception("ID de asignación inválido"))
        }
        if (status.isBlank()) {
            return Result.failure(Exception("El estado no puede estar vacío"))
        }

        return testAssignmentRepository.updateAssignmentStatus(assignmentId, status)
    }
}
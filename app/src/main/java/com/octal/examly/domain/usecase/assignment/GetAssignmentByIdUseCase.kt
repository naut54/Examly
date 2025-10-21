package com.octal.examly.domain.usecase.assignment

import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.domain.repository.TestAssignmentRepository
import javax.inject.Inject

class GetAssignmentByIdUseCase @Inject constructor(
    private val testAssignmentRepository: TestAssignmentRepository
) {
    suspend operator fun invoke(assignmentId: Long): Result<TestAssignment> {
        if (assignmentId <= 0) {
            return Result.failure(Exception("ID de asignación inválido"))
        }
        return testAssignmentRepository.getAssignmentById(assignmentId)
    }
}
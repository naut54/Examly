package com.octal.examly.domain.usecase.assignment

import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.domain.repository.TestAssignmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAssignedTestsUseCase @Inject constructor(
    private val testAssignmentRepository: TestAssignmentRepository
) {
    operator fun invoke(userId: Long): Flow<List<TestAssignment>> {
        return testAssignmentRepository.getAssignedTests(userId)
    }
}
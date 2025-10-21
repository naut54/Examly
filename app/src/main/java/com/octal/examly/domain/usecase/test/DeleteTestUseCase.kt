package com.octal.examly.domain.usecase.test

import com.octal.examly.domain.repository.TestRepository
import javax.inject.Inject

class DeleteTestUseCase @Inject constructor(
    private val testRepository: TestRepository
) {
    suspend operator fun invoke(testId: Long): Result<Unit> {
        if (testId <= 0) {
            return Result.failure(Exception("ID de test inválido"))
        }
        return testRepository.deleteTest(testId)
    }
}
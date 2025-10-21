package com.octal.examly.domain.usecase.test

import com.octal.examly.domain.model.Test
import com.octal.examly.domain.repository.TestRepository
import javax.inject.Inject

class GetTestByIdUseCase @Inject constructor(
    private val testRepository: TestRepository
) {
    suspend operator fun invoke(testId: Long): Result<Test> {
        if (testId <= 0) {
            return Result.failure(Exception("ID de test inválido"))
        }
        return testRepository.getTestById(testId)
    }
}
package com.octal.examly.domain.usecase.test

import com.octal.examly.domain.model.Question
import com.octal.examly.domain.repository.TestRepository
import javax.inject.Inject

class GetTestQuestionsUseCase @Inject constructor(
    private val testRepository: TestRepository
) {
    suspend operator fun invoke(testId: Long): Result<List<Question>> {
        if (testId <= 0) {
            return Result.failure(Exception("ID de test inválido"))
        }
        return testRepository.getTestQuestions(testId)
    }
}
package com.octal.examly.domain.usecase.attempt

import com.octal.examly.domain.repository.TestAttemptRepository
import javax.inject.Inject

class SubmitTestUseCase @Inject constructor(
    private val testAttemptRepository: TestAttemptRepository
) {
    suspend operator fun invoke(attemptId: Long, score: Double): Result<Unit> {
        if (attemptId <= 0) {
            return Result.failure(Exception("ID de intento inválido"))
        }
        if (score < 0 || score > 100) {
            return Result.failure(Exception("La puntuación debe estar entre 0 y 100"))
        }

        return testAttemptRepository.submitTest(attemptId, score)
    }
}
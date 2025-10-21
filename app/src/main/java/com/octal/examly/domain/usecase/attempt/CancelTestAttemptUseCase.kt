package com.octal.examly.domain.usecase.attempt

import com.octal.examly.domain.repository.TestAttemptRepository
import javax.inject.Inject

class CancelTestAttemptUseCase @Inject constructor(
    private val testAttemptRepository: TestAttemptRepository
) {
    suspend operator fun invoke(attemptId: Long): Result<Unit> {
        if (attemptId <= 0) {
            return Result.failure(Exception("ID de intento inválido"))
        }
        return testAttemptRepository.cancelAttempt(attemptId)
    }
}
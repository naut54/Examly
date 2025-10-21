package com.octal.examly.domain.usecase.attempt

import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.repository.TestAttemptRepository
import javax.inject.Inject

class GetPendingTestAttemptUseCase @Inject constructor(
    private val testAttemptRepository: TestAttemptRepository
) {
    suspend operator fun invoke(userId: Long): Result<TestAttempt?> {
        if (userId <= 0) {
            return Result.failure(Exception("ID de usuario inválido"))
        }
        return testAttemptRepository.getPendingAttempt(userId)
    }
}
package com.octal.examly.domain.usecase.attempt

import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.repository.TestAttemptRepository
import javax.inject.Inject

class ResumeTestAttemptUseCase @Inject constructor(
    private val testAttemptRepository: TestAttemptRepository
) {
    suspend operator fun invoke(attemptId: Long): Result<TestAttempt> {
        if (attemptId <= 0) {
            return Result.failure(Exception("ID de intento inválido"))
        }
        return testAttemptRepository.resumeAttempt(attemptId)
    }
}
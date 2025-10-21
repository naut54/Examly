package com.octal.examly.domain.usecase.attempt

import com.octal.examly.domain.repository.TestAttemptRepository
import javax.inject.Inject

class SaveTestProgressUseCase @Inject constructor(
    private val testAttemptRepository: TestAttemptRepository
) {
    suspend operator fun invoke(attemptId: Long, currentQuestionIndex: Int): Result<Unit> {
        if (attemptId <= 0) {
            return Result.failure(Exception("ID de intento inválido"))
        }
        if (currentQuestionIndex < 0) {
            return Result.failure(Exception("Índice de pregunta inválido"))
        }

        return testAttemptRepository.saveProgress(attemptId, currentQuestionIndex)
    }
}
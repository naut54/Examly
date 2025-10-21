package com.octal.examly.domain.usecase.attempt

import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.model.TestAttemptMode
import com.octal.examly.domain.repository.TestAttemptRepository
import com.octal.examly.domain.repository.TestRepository
import javax.inject.Inject

class StartTestAttemptUseCase @Inject constructor(
    private val testAttemptRepository: TestAttemptRepository,
    private val testRepository: TestRepository
) {
    suspend operator fun invoke(
        assignmentId: Long,
        userId: Long,
        mode: TestAttemptMode
    ): Result<Long> {
        if (assignmentId <= 0) {
            return Result.failure(Exception("ID de asignación inválido"))
        }
        if (userId <= 0) {
            return Result.failure(Exception("ID de usuario inválido"))
        }

        val attempt = TestAttempt(
            id = 0,
            assignmentId = assignmentId,
            userId = userId,
            startedAt = System.currentTimeMillis(),
            completedAt = null,
            score = null,
            mode = mode,
            currentQuestionIndex = 0,
            isPaused = false,
            timeRemaining = null
        )

        return testAttemptRepository.startAttempt(attempt)
    }
}
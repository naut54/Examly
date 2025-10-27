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
        testId: Long,
        userId: Long,
        mode: TestAttemptMode
    ): Result<Long> {
        if (assignmentId <= 0) {
            return Result.failure(Exception("ID de asignación inválido"))
        }
        if (testId <= 0) {
            return Result.failure(Exception("ID de test inválido"))
        }
        if (userId <= 0) {
            return Result.failure(Exception("ID de usuario inválido"))
        }

        // Fetch test configuration to initialize timer when applicable
        val timeRemainingSeconds: Int? = try {
            val testResult = testRepository.getTestById(testId)
            testResult.getOrNull()?.let { test ->
                val hasTimer = test.configuration.hasTimer // default false if not set
                val minutes = test.configuration.timeLimit
                if (hasTimer && minutes != null && minutes > 0) {
                    minutes * 60
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
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
            timeRemaining = timeRemainingSeconds
        )

        return testAttemptRepository.startAttempt(attempt)
    }
}
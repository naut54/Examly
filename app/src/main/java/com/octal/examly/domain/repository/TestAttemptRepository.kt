package com.octal.examly.domain.repository

import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.model.UserAnswer

interface TestAttemptRepository {
    suspend fun startAttempt(attempt: TestAttempt): Result<Long>
    suspend fun saveProgress(attemptId: Long, currentQuestionIndex: Int): Result<Unit>
    suspend fun saveAnswer(answer: UserAnswer): Result<Long>
    suspend fun submitTest(attemptId: Long, score: Double): Result<Unit>
    suspend fun getPendingAttempt(userId: Long): Result<TestAttempt?>
    suspend fun resumeAttempt(attemptId: Long): Result<TestAttempt>
    suspend fun cancelAttempt(attemptId: Long): Result<Unit>
}
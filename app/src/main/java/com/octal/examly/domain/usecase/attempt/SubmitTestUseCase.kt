package com.octal.examly.domain.usecase.attempt

import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.repository.ResultRepository
import com.octal.examly.domain.repository.TestAssignmentRepository
import com.octal.examly.domain.repository.TestAttemptRepository
import javax.inject.Inject

class SubmitTestUseCase @Inject constructor(
    private val testAttemptRepository: TestAttemptRepository,
    private val testAssignmentRepository: TestAssignmentRepository,
    private val resultRepository: ResultRepository
) {
    suspend operator fun invoke(attemptId: Long, score: Double): Result<Long> {
        if (attemptId <= 0) {
            return Result.failure(Exception("ID de intento inválido"))
        }
        if (score < 0 || score > 100) {
            return Result.failure(Exception("La puntuación debe estar entre 0 y 100"))
        }

        val submitResult = testAttemptRepository.submitTest(attemptId, score)
        if (submitResult.isFailure) {
            return Result.failure(submitResult.exceptionOrNull() ?: Exception("Error al enviar test"))
        }

        val attemptResult = testAttemptRepository.resumeAttempt(attemptId)
        if (attemptResult.isFailure) {
            return Result.failure(attemptResult.exceptionOrNull() ?: Exception("Error al obtener intento"))
        }

        val attempt = attemptResult.getOrNull()!!

        val assignmentResult = testAssignmentRepository.getAssignmentById(attempt.assignmentId)
        if (assignmentResult.isFailure) {
            return Result.failure(assignmentResult.exceptionOrNull() ?: Exception("Error al obtener asignación"))
        }
        val assignment = assignmentResult.getOrNull()!!

        val answeredDistinct = attempt.userAnswers.map { it.questionId }.distinct().size
        val totalQuestions = maxOf(attempt.questions.size, answeredDistinct)
        val correctAnswers = attempt.userAnswers.count { it.isCorrect }
        val wrongAnswersRaw = totalQuestions - correctAnswers
        val wrongAnswers = if (wrongAnswersRaw < 0) 0 else wrongAnswersRaw
        val calculatedScore = if (totalQuestions > 0) {
            (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100.0
        } else {
            0.0
        }

        val timeSpent = attempt.completedAt?.let { completedAt ->
            completedAt - attempt.startedAt
        }

        val testResult = TestResult(
            id = 0,
            attemptId = attemptId,
            userId = attempt.userId,
            testId = assignment.testId,
            score = calculatedScore,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            timeSpent = timeSpent,
            mode = attempt.mode,
            completedAt = System.currentTimeMillis(),
            isPassed = calculatedScore >= TestResult.PASSING_SCORE
        )

        return resultRepository.saveResult(testResult)
    }
}
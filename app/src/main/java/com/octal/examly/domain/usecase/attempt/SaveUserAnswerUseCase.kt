package com.octal.examly.domain.usecase.attempt

import com.octal.examly.domain.model.UserAnswer
import com.octal.examly.domain.repository.QuestionRepository
import com.octal.examly.domain.repository.TestAttemptRepository
import javax.inject.Inject

class SaveUserAnswerUseCase @Inject constructor(
    private val testAttemptRepository: TestAttemptRepository,
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(
        attemptId: Long,
        questionId: Long,
        selectedAnswerIds: List<Long>
    ): Result<Long> {
        if (attemptId <= 0) {
            return Result.failure(Exception("ID de intento inválido"))
        }
        if (questionId <= 0) {
            return Result.failure(Exception("ID de pregunta inválido"))
        }
        if (selectedAnswerIds.isEmpty()) {
            return Result.failure(Exception("Debe seleccionar al menos una respuesta"))
        }

        val questionResult = questionRepository.getQuestionById(questionId)
        if (questionResult.isFailure) {
            return Result.failure(questionResult.exceptionOrNull() ?: Exception("Error al obtener pregunta"))
        }

        val question = questionResult.getOrNull()!!
        val correctAnswerIds = question.answers.filter { it.isCorrect }.map { it.id }
        val isCorrect = selectedAnswerIds.sorted() == correctAnswerIds.sorted()

        val userAnswer = UserAnswer(
            id = 0,
            attemptId = attemptId,
            questionId = questionId,
            selectedAnswerIds = selectedAnswerIds,
            isCorrect = isCorrect,
            answeredAt = System.currentTimeMillis()
        )

        return testAttemptRepository.saveAnswer(userAnswer)
    }
}
package com.octal.examly.domain.usecase.question

import com.octal.examly.domain.model.Question
import com.octal.examly.domain.repository.QuestionRepository
import javax.inject.Inject

class UpdateQuestionUseCase @Inject constructor(
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(question: Question): Result<Unit> {
        if (question.questionText.isBlank()) {
            return Result.failure(Exception("El texto de la pregunta no puede estar vacío"))
        }
        if (question.answers.isEmpty()) {
            return Result.failure(Exception("La pregunta debe tener al menos una respuesta"))
        }
        if (question.answers.none { it.isCorrect }) {
            return Result.failure(Exception("Debe haber al menos una respuesta correcta"))
        }

        return questionRepository.updateQuestion(question)
    }
}
package com.octal.examly.domain.usecase.question

import com.octal.examly.domain.model.Question
import com.octal.examly.domain.repository.QuestionRepository
import javax.inject.Inject

class CreateQuestionUseCase @Inject constructor(
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(question: Question): Result<Long> {
        if (question.questionText.isBlank()) {
            return Result.failure(Exception("El texto de la pregunta no puede estar vacío"))
        }
        if (question.answers.isEmpty()) {
            return Result.failure(Exception("La pregunta debe tener al menos una respuesta"))
        }
        if (question.answers.none { it.isCorrect }) {
            return Result.failure(Exception("Debe haber al menos una respuesta correcta"))
        }
        if (question.answers.size < 2) {
            return Result.failure(Exception("La pregunta debe tener al menos 2 opciones de respuesta"))
        }

        return questionRepository.createQuestion(question)
    }
}
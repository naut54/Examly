package com.octal.examly.domain.usecase.question

import com.octal.examly.domain.repository.QuestionRepository
import javax.inject.Inject

class DeleteQuestionUseCase @Inject constructor(
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(questionId: Long): Result<Unit> {
        if (questionId <= 0) {
            return Result.failure(Exception("ID de pregunta inválido"))
        }
        return questionRepository.deleteQuestion(questionId)
    }
}
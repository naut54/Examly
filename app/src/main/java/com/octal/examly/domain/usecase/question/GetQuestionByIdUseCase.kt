package com.octal.examly.domain.usecase.question

import com.octal.examly.domain.model.Question
import com.octal.examly.domain.repository.QuestionRepository
import javax.inject.Inject

class GetQuestionByIdUseCase @Inject constructor(
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(questionId: Long): Result<Question> {
        if (questionId <= 0) {
            return Result.failure(Exception("ID de pregunta inválido"))
        }
        return questionRepository.getQuestionById(questionId)
    }
}
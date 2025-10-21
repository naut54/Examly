package com.octal.examly.domain.usecase.question

import com.octal.examly.domain.model.Question
import com.octal.examly.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuestionsBySubjectUseCase @Inject constructor(
    private val questionRepository: QuestionRepository
) {
    operator fun invoke(subjectId: Long): Flow<List<Question>> {
        return questionRepository.getQuestionsBySubject(subjectId)
    }
}
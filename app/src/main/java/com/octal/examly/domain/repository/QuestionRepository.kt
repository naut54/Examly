package com.octal.examly.domain.repository

import com.octal.examly.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    suspend fun createQuestion(question: Question): Result<Long>
    fun getQuestionsBySubject(subjectId: Long): Flow<List<Question>>
    suspend fun getQuestionById(questionId: Long): Result<Question>
    suspend fun updateQuestion(question: Question): Result<Unit>
    suspend fun deleteQuestion(questionId: Long): Result<Unit>
}
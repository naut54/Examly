package com.octal.examly.domain.repository

import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.Test
import kotlinx.coroutines.flow.Flow

interface TestRepository {
    suspend fun createTest(test: Test, questionIds: List<Long>? = null): Result<Long>
    fun getAllTests(): Flow<List<Test>>
    suspend fun getTestById(testId: Long): Result<Test>
    suspend fun deleteTest(testId: Long): Result<Unit>
    suspend fun getTestQuestions(testId: Long): Result<List<Question>>
}
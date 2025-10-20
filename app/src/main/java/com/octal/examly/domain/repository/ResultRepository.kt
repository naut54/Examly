package com.octal.examly.domain.repository

import com.octal.examly.domain.model.TestResult
import kotlinx.coroutines.flow.Flow

interface ResultRepository {
    suspend fun saveResult(result: TestResult): Result<Long>
    fun getUserResults(userId: Long): Flow<List<TestResult>>
    fun getAllResults(): Flow<List<TestResult>>
    suspend fun getResultDetail(resultId: Long): Result<TestResult>
    suspend fun getMetricsBySubject(subjectId: Long, userId: Long? = null): Result<Map<String, Any>>
    suspend fun getMetricsByUser(userId: Long): Result<Map<String, Any>>
}
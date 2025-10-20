package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.TestResultDao
import com.octal.examly.data.mapper.toDomain
import com.octal.examly.data.mapper.toEntity
import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.repository.ResultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ResultRepositoryImpl @Inject constructor(
    private val testResultDao: TestResultDao
) : ResultRepository {

    override suspend fun saveResult(result: TestResult): Result<Long> {
        return try {
            val resultEntity = result.toEntity()
            val resultId = testResultDao.insert(resultEntity)
            Result.success(resultId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar resultado: ${e.message}", e))
        }
    }

    override fun getUserResults(userId: Long): Flow<List<TestResult>> {
        return testResultDao.getByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllResults(): Flow<List<TestResult>> {
        return testResultDao.getAllResults().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getResultDetail(resultId: Long): Result<TestResult> {
        return try {
            val resultEntity = testResultDao.getById(resultId)
                ?: return Result.failure(Exception("Resultado no encontrado"))

            Result.success(resultEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener resultado: ${e.message}", e))
        }
    }

    override suspend fun getMetricsBySubject(subjectId: Long, userId: Long?): Result<Map<String, Any>> {
        return try {
            // Aquí implementarías la lógica para calcular métricas
            // Por ahora retornamos un mapa vacío como ejemplo
            val metrics = mapOf(
                "average" to 0.0,
                "total" to 0,
                "passed" to 0,
                "failed" to 0
            )
            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(Exception("Error al calcular métricas: ${e.message}", e))
        }
    }

    override suspend fun getMetricsByUser(userId: Long): Result<Map<String, Any>> {
        return try {
            // Aquí implementarías la lógica para calcular métricas del usuario
            val metrics = mapOf(
                "average" to 0.0,
                "total" to 0,
                "passed" to 0,
                "failed" to 0
            )
            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(Exception("Error al calcular métricas: ${e.message}", e))
        }
    }
}
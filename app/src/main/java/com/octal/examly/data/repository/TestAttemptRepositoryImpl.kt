package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.TestAttemptDao
import com.octal.examly.data.local.dao.UserAnswerDao
import com.octal.examly.data.mapper.toDomain
import com.octal.examly.data.mapper.toEntity
import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.domain.model.UserAnswer
import com.octal.examly.domain.repository.TestAttemptRepository
import javax.inject.Inject

class TestAttemptRepositoryImpl @Inject constructor(
    private val testAttemptDao: TestAttemptDao,
    private val userAnswerDao: UserAnswerDao
) : TestAttemptRepository {

    override suspend fun startAttempt(attempt: TestAttempt): Result<Long> {
        return try {
            val attemptEntity = attempt.toEntity()
            val attemptId = testAttemptDao.insert(attemptEntity)
            Result.success(attemptId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al iniciar intento: ${e.message}", e))
        }
    }

    override suspend fun saveProgress(
        attemptId: Long,
        currentQuestionIndex: Int
    ): Result<Unit> {
        return try {
            testAttemptDao.updateProgress(attemptId, currentQuestionIndex)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar progreso: ${e.message}", e))
        }
    }

    override suspend fun saveAnswer(answer: UserAnswer): Result<Long> {
        return try {
            val answerEntity = answer.toEntity()
            val answerId = userAnswerDao.insert(answerEntity)
            Result.success(answerId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar respuesta: ${e.message}", e))
        }
    }

    override suspend fun submitTest(attemptId: Long, score: Double): Result<Unit> {
        return try {
            val completedAt = System.currentTimeMillis()
            testAttemptDao.complete(attemptId, score, completedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al enviar test: ${e.message}", e))
        }
    }

    override suspend fun getPendingAttempt(userId: Long): Result<TestAttempt?> {
        return try {
            val attemptEntity = testAttemptDao.getPendingAttemptByUser(userId)
            Result.success(attemptEntity?.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener intento pendiente: ${e.message}", e))
        }
    }

    override suspend fun resumeAttempt(attemptId: Long): Result<TestAttempt> {
        return try {
            val attemptEntity = testAttemptDao.getById(attemptId)
                ?: return Result.failure(Exception("Intento no encontrado"))

            val userAnswerEntities = userAnswerDao.getByAttemptId(attemptId)
            val userAnswers = userAnswerEntities.map { it.toDomain() }

            val attempt = attemptEntity.toDomain().copy(userAnswers = userAnswers)

            Result.success(attempt)
        } catch (e: Exception) {
            Result.failure(Exception("Error al reanudar intento: ${e.message}", e))
        }
    }

    override suspend fun cancelAttempt(attemptId: Long): Result<Unit> {
        return try {
            testAttemptDao.deleteById(attemptId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al cancelar intento: ${e.message}", e))
        }
    }
}
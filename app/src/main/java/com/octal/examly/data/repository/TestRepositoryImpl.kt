package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.QuestionDao
import com.octal.examly.data.local.dao.TestDao
import com.octal.examly.data.local.dao.TestQuestionDao
import com.octal.examly.data.local.entities.TestQuestionEntity
import com.octal.examly.data.mapper.toDomain
import com.octal.examly.data.mapper.toEntity
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.TestMode
import com.octal.examly.domain.repository.TestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    private val testDao: TestDao,
    private val testQuestionDao: TestQuestionDao,
    private val questionDao: QuestionDao
) : TestRepository {

    override suspend fun createTest(test: Test, questionIds: List<Long>?): Result<Long> {
        return try {
            when (test.mode) {
                TestMode.FIXED -> {
                    if (questionIds.isNullOrEmpty()) {
                        return Result.failure(Exception("Los tests fijos requieren preguntas seleccionadas"))
                    }
                }
                TestMode.RANDOM -> {
                    if (test.configuration.numberOfQuestions <= 0) {
                        return Result.failure(Exception("El número de preguntas debe ser mayor a 0"))
                    }
                }
            }

            val testEntity = test.toEntity()
            val testId = testDao.insert(testEntity)

            if (test.mode == TestMode.FIXED && !questionIds.isNullOrEmpty()) {
                val testQuestions = questionIds.mapIndexed { index, questionId ->
                    TestQuestionEntity(
                        id = 0,
                        testId = testId,
                        questionId = questionId,
                        orderIndex = index
                    )
                }
                testQuestionDao.insertAll(testQuestions)
            }

            Result.success(testId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear test: ${e.message}", e))
        }
    }

    override fun getAllTests(): Flow<List<Test>> {
        return testDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTestById(testId: Long): Result<Test> {
        return try {
            val testEntity = testDao.getById(testId)
                ?: return Result.failure(Exception("Test no encontrado"))

            Result.success(testEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener test: ${e.message}", e))
        }
    }

    override suspend fun deleteTest(testId: Long): Result<Unit> {
        return try {
            val testEntity = testDao.getById(testId)
                ?: return Result.failure(Exception("Test no encontrado"))

            testQuestionDao.deleteByTestId(testId)

            testDao.delete(testEntity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar test: ${e.message}", e))
        }
    }

    override suspend fun getTestQuestions(testId: Long): Result<List<Question>> {
        return try {
            val test = testDao.getById(testId)
                ?: return Result.failure(Exception("Test no encontrado"))

            val questions = when (test.mode) {
                "FIXED" -> {
                    val testQuestions = testQuestionDao.getQuestionsByTestId(testId)
                    testQuestions.mapNotNull { tq ->
                        questionDao.getById(tq.questionId)?.toDomain(emptyList())
                    }
                }
                "RANDOM" -> {
                    questionDao.getRandomQuestionsBySubject(
                        test.subjectId,
                        test.numberOfQuestions
                    ).map { it.toDomain(emptyList()) }
                }
                else -> emptyList()
            }

            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener preguntas del test: ${e.message}", e))
        }
    }
}
package com.octal.examly.data.repository

import android.util.Log
import com.octal.examly.data.local.dao.QuestionDao
import com.octal.examly.data.local.dao.TestDao
import com.octal.examly.data.local.dao.TestQuestionDao
import com.octal.examly.data.local.dao.AnswerDao
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
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao
) : TestRepository {

    companion object {
        private const val TAG = "TestRepositoryImpl"
    }

    override suspend fun createTest(test: Test, questionIds: List<Long>?): Result<Long> {
        Log.d(TAG, "createTest: Starting test creation")
        Log.d(TAG, "createTest: test.mode=${test.mode}, test.title='${test.title}', questionIds=$questionIds")

        return try {
            when (test.mode) {
                TestMode.FIXED -> {
                    if (questionIds.isNullOrEmpty()) {
                        Log.e(TAG, "createTest: FIXED mode validation failed - no questions")
                        return Result.failure(Exception("Los tests fijos requieren preguntas seleccionadas"))
                    }
                    Log.d(TAG, "createTest: FIXED mode - ${questionIds.size} questions provided")
                }
                TestMode.RANDOM -> {
                    test.configuration.numberOfQuestions?.let {
                        if (it <= 0) {
                            Log.e(TAG, "createTest: RANDOM mode validation failed - numberOfQuestions=$it")
                            return Result.failure(Exception("El número de preguntas debe ser mayor a 0"))
                        }
                        Log.d(TAG, "createTest: RANDOM mode - numberOfQuestions=$it")
                    }
                }
            }

            Log.d(TAG, "createTest: Converting test to entity")
            val testEntity = test.toEntity()
            Log.d(TAG, "createTest: Test entity created, inserting into database")

            val testId = testDao.insert(testEntity)
            Log.d(TAG, "createTest: Test inserted with ID=$testId")

            if (test.mode == TestMode.FIXED && !questionIds.isNullOrEmpty()) {
                Log.d(TAG, "createTest: Inserting ${questionIds.size} test questions")
                val testQuestions = questionIds.mapIndexed { index, questionId ->
                    TestQuestionEntity(
                        id = 0,
                        testId = testId,
                        questionId = questionId,
                        orderIndex = index
                    )
                }
                testQuestionDao.insertAll(testQuestions)
                Log.d(TAG, "createTest: Test questions inserted successfully")
            }

            Log.d(TAG, "createTest: Success! Returning testId=$testId")
            Result.success(testId)
        } catch (e: Exception) {
            Log.e(TAG, "createTest: Exception occurred", e)
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
                        val qEntity = questionDao.getById(tq.questionId)
                        qEntity?.let { qe ->
                            val answers = answerDao.getByQuestionId(qe.id).map { it.toDomain() }
                            qe.toDomain(answers)
                        }
                    }
                }
                "RANDOM" -> {
                    questionDao.getRandomQuestionsBySubject(
                        test.subjectId,
                        test.numberOfQuestions
                    ).map { qe ->
                        val answers = answerDao.getByQuestionId(qe.id).map { it.toDomain() }
                        qe.toDomain(answers)
                    }
                }
                else -> emptyList()
            }

            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener preguntas del test: ${e.message}", e))
        }
    }
}
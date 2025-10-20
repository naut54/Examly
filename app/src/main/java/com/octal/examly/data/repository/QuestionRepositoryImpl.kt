package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.AnswerDao
import com.octal.examly.data.local.dao.QuestionDao
import com.octal.examly.data.mapper.toDomain
import com.octal.examly.data.mapper.toEntity
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao
) : QuestionRepository {

    override suspend fun createQuestion(question: Question): Result<Long> {
        return try {
            if (question.answers.none { it.isCorrect }) {
                return Result.failure(Exception("La pregunta debe tener al menos una respuesta correcta"))
            }

            val questionEntity = question.toEntity()
            val questionId = questionDao.insert(questionEntity)

            val answerEntities = question.answers.map { answer ->
                answer.toEntity(questionId)
            }
            answerDao.insertAll(answerEntities)

            Result.success(questionId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear pregunta: ${e.message}", e))
        }
    }

    override fun getQuestionsBySubject(subjectId: Long): Flow<List<Question>> {
        return questionDao.getBySubjectId(subjectId).map { questionEntities ->
            questionEntities.map { questionEntity ->
                val answers = answerDao.getByQuestionId(questionEntity.id)
                    .map { it.toDomain() }
                questionEntity.toDomain(answers)
            }
        }
    }

    override suspend fun getQuestionById(questionId: Long): Result<Question> {
        return try {
            val questionEntity = questionDao.getById(questionId)
                ?: return Result.failure(Exception("Pregunta no encontrada"))

            val answers = answerDao.getByQuestionId(questionId)
                .map { it.toDomain() }

            Result.success(questionEntity.toDomain(answers))
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener pregunta: ${e.message}", e))
        }
    }

    override suspend fun updateQuestion(question: Question): Result<Unit> {
        return try {
            if (question.answers.none { it.isCorrect }) {
                return Result.failure(Exception("La pregunta debe tener al menos una respuesta correcta"))
            }

            val questionEntity = question.toEntity()
            questionDao.update(questionEntity)

            val oldAnswers = answerDao.getByQuestionId(question.id)
            oldAnswers.forEach { answerDao.delete(it) }

            val answerEntities = question.answers.map { it.toEntity(question.id) }
            answerDao.insertAll(answerEntities)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar pregunta: ${e.message}", e))
        }
    }

    override suspend fun deleteQuestion(questionId: Long): Result<Unit> {
        return try {
            val questionEntity = questionDao.getById(questionId)
                ?: return Result.failure(Exception("Pregunta no encontrada"))

            val answers = answerDao.getByQuestionId(questionId)
            answers.forEach { answerDao.delete(it) }

            questionDao.delete(questionEntity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar pregunta: ${e.message}", e))
        }
    }
}
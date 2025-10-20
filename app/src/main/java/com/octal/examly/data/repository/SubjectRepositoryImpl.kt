package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.SubjectDao
import com.octal.examly.data.mapper.toDomain
import com.octal.examly.data.mapper.toEntity
import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao
) : SubjectRepository {

    override suspend fun createSubject(subject: Subject): Result<Long> {
        return try {
            val subjectEntity = subject.toEntity()
            val subjectId = subjectDao.insert(subjectEntity)
            Result.success(subjectId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear asignatura: ${e.message}", e))
        }
    }

    override fun getAllSubjects(): Flow<List<Subject>> {
        return subjectDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSubjectById(subjectId: Long): Result<Subject> {
        return try {
            val subjectEntity = subjectDao.getById(subjectId)
                ?: return Result.failure(Exception("Asignatura no encontrada"))

            Result.success(subjectEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener asignatura: ${e.message}", e))
        }
    }

    override suspend fun deleteSubject(subjectId: Long): Result<Unit> {
        return try {
            val subjectEntity = subjectDao.getById(subjectId)
                ?: return Result.failure(Exception("Asignatura no encontrada"))

            subjectDao.delete(subjectEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar asignatura: ${e.message}", e))
        }
    }
}
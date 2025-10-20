package com.octal.examly.domain.repository

import com.octal.examly.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    suspend fun createSubject(subject: Subject): Result<Long>
    fun getAllSubjects(): Flow<List<Subject>>
    suspend fun getSubjectById(subjectId: Long): Result<Subject>
    suspend fun deleteSubject(subjectId: Long): Result<Unit>
}
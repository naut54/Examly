package com.octal.examly.domain.usecase.subject

import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSubjectsUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(): Flow<List<Subject>> {
        return subjectRepository.getAllSubjects()
    }
}
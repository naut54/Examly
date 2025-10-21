package com.octal.examly.domain.usecase.subject

import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.repository.SubjectRepository
import javax.inject.Inject

class GetSubjectByIdUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(subjectId: Long): Result<Subject> {
        if (subjectId <= 0) {
            return Result.failure(Exception("ID de asignatura inválido"))
        }
        return subjectRepository.getSubjectById(subjectId)
    }
}
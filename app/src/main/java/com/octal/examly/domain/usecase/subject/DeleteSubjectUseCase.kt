package com.octal.examly.domain.usecase.subject

import com.octal.examly.domain.repository.SubjectRepository
import javax.inject.Inject

class DeleteSubjectUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(subjectId: Long): Result<Unit> {
        if (subjectId <= 0) {
            return Result.failure(Exception("ID de asignatura inválido"))
        }
        return subjectRepository.deleteSubject(subjectId)
    }
}
package com.octal.examly.domain.usecase.subject

import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.repository.SubjectRepository
import javax.inject.Inject

class CreateSubjectUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(name: String, description: String): Result<Long> {
        if (name.isBlank()) {
            return Result.failure(Exception("El nombre de la asignatura no puede estar vacío"))
        }
        if (description.isBlank()) {
            return Result.failure(Exception("La descripción no puede estar vacía"))
        }

        val subject = Subject(
            id = 0,
            name = name,
            description = description,
            createdAt = System.currentTimeMillis()
        )

        return subjectRepository.createSubject(subject)
    }
}
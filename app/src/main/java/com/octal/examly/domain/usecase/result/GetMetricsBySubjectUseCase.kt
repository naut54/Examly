package com.octal.examly.domain.usecase.result

import com.octal.examly.domain.repository.ResultRepository
import javax.inject.Inject

class GetMetricsBySubjectUseCase @Inject constructor(
    private val resultRepository: ResultRepository
) {
    suspend operator fun invoke(subjectId: Long, userId: Long? = null): Result<Map<String, Any>> {
        if (subjectId <= 0) {
            return Result.failure(Exception("ID de asignatura inválido"))
        }
        return resultRepository.getMetricsBySubject(subjectId, userId)
    }
}
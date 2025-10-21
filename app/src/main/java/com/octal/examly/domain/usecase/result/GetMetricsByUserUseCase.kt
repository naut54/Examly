package com.octal.examly.domain.usecase.result

import com.octal.examly.domain.repository.ResultRepository
import javax.inject.Inject

class GetMetricsByUserUseCase @Inject constructor(
    private val resultRepository: ResultRepository
) {
    suspend operator fun invoke(userId: Long): Result<Map<String, Any>> {
        if (userId <= 0) {
            return Result.failure(Exception("ID de usuario inválido"))
        }
        return resultRepository.getMetricsByUser(userId)
    }
}
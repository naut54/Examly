package com.octal.examly.domain.usecase.result

import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.repository.ResultRepository
import javax.inject.Inject

class GetResultDetailUseCase @Inject constructor(
    private val resultRepository: ResultRepository
) {
    suspend operator fun invoke(resultId: Long): Result<TestResult> {
        if (resultId <= 0) {
            return Result.failure(Exception("ID de resultado inválido"))
        }
        return resultRepository.getResultDetail(resultId)
    }
}
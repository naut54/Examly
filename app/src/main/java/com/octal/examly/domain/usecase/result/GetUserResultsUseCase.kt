package com.octal.examly.domain.usecase.result

import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.repository.ResultRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserResultsUseCase @Inject constructor(
    private val resultRepository: ResultRepository
) {
    operator fun invoke(userId: Long): Flow<List<TestResult>> {
        return resultRepository.getUserResults(userId)
    }
}
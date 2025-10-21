package com.octal.examly.domain.usecase.result

import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.repository.ResultRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllResultsUseCase @Inject constructor(
    private val resultRepository: ResultRepository
) {
    operator fun invoke(): Flow<List<TestResult>> {
        return resultRepository.getAllResults()
    }
}
package com.octal.examly.domain.usecase.test

import com.octal.examly.domain.model.Test
import com.octal.examly.domain.repository.TestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTestsUseCase @Inject constructor(
    private val testRepository: TestRepository
) {
    operator fun invoke(): Flow<List<Test>> {
        return testRepository.getAllTests()
    }
}
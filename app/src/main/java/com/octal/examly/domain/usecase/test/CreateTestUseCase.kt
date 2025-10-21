package com.octal.examly.domain.usecase.test

import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.TestMode
import com.octal.examly.domain.repository.TestRepository
import javax.inject.Inject

class CreateTestUseCase @Inject constructor(
    private val testRepository: TestRepository
) {
    suspend operator fun invoke(test: Test, questionIds: List<Long>? = null): Result<Long> {
        if (test.title.isBlank()) {
            return Result.failure(Exception("El título del test no puede estar vacío"))
        }
        if (test.description.isBlank()) {
            return Result.failure(Exception("La descripción no puede estar vacía"))
        }

        when (test.mode) {
            TestMode.FIXED -> {
                if (questionIds.isNullOrEmpty()) {
                    return Result.failure(Exception("Debe seleccionar al menos una pregunta para un test fijo"))
                }
            }
            TestMode.RANDOM -> {
                if (test.configuration.numberOfQuestions == null || test.configuration.numberOfQuestions <= 0) {
                    return Result.failure(Exception("Debe especificar el número de preguntas para un test aleatorio"))
                }
            }
        }

        if (test.configuration.hasTimer && (test.configuration.timeLimit == null || test.configuration.timeLimit <= 0)) {
            return Result.failure(Exception("Debe especificar un límite de tiempo válido"))
        }

        return testRepository.createTest(test, questionIds)
    }
}
package com.octal.examly.domain.usecase.test

import android.util.Log
import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.TestMode
import com.octal.examly.domain.repository.TestRepository
import javax.inject.Inject

class CreateTestUseCase @Inject constructor(
    private val testRepository: TestRepository
) {
    companion object {
        private const val TAG = "CreateTestUseCase"
    }

    suspend operator fun invoke(test: Test, questionIds: List<Long>? = null): Result<Long> {
        Log.d(TAG, "invoke: Starting validation")
        Log.d(TAG, "invoke: test.title='${test.title}', test.mode=${test.mode}, questionIds=$questionIds")

        if (test.title.isBlank()) {
            Log.e(TAG, "invoke: Validation failed - title is blank")
            return Result.failure(Exception("El título del test no puede estar vacío"))
        }

        when (test.mode) {
            TestMode.FIXED -> {
                if (questionIds.isNullOrEmpty()) {
                    Log.e(TAG, "invoke: Validation failed - FIXED mode requires questions but got none")
                    return Result.failure(Exception("Debe seleccionar al menos una pregunta para un test fijo"))
                }
                Log.d(TAG, "invoke: FIXED mode validation passed - ${questionIds.size} questions")
            }
            TestMode.RANDOM -> {
                if (test.configuration.numberOfQuestions == null || test.configuration.numberOfQuestions <= 0) {
                    Log.e(TAG, "invoke: Validation failed - RANDOM mode requires numberOfQuestions")
                    return Result.failure(Exception("Debe especificar el número de preguntas para un test aleatorio"))
                }
                Log.d(TAG, "invoke: RANDOM mode validation passed - ${test.configuration.numberOfQuestions} questions")
            }
        }

        if (test.configuration.hasTimer && (test.configuration.timeLimit == null || test.configuration.timeLimit <= 0)) {
            Log.e(TAG, "invoke: Validation failed - hasTimer=true but invalid timeLimit")
            return Result.failure(Exception("Debe especificar un límite de tiempo válido"))
        }

        Log.d(TAG, "invoke: All validations passed, calling repository")
        val result = testRepository.createTest(test, questionIds)
        result.fold(
            onSuccess = { id -> Log.d(TAG, "invoke: Repository returned success with ID=$id") },
            onFailure = { e -> Log.e(TAG, "invoke: Repository returned failure", e) }
        )
        return result
    }
}
package com.octal.examly.domain.usecase.user

import com.octal.examly.domain.repository.UserRepository
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long): Result<Unit> {
        if (userId <= 0) {
            return Result.failure(Exception("ID de usuario inválido"))
        }
        return userRepository.deleteUser(userId)
    }
}
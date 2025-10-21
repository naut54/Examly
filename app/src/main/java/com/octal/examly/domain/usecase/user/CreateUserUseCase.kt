package com.octal.examly.domain.usecase.user

import com.octal.examly.domain.model.User
import com.octal.examly.domain.model.UserRole
import com.octal.examly.domain.repository.UserRepository
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        role: UserRole
    ): Result<Long> {
        if (username.isBlank()) {
            return Result.failure(Exception("El nombre de usuario no puede estar vacío"))
        }
        if (username.length < 3) {
            return Result.failure(Exception("El nombre de usuario debe tener al menos 3 caracteres"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("La contraseña no puede estar vacía"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        val user = User(
            id = 0,
            username = username,
            role = role,
            createdAt = System.currentTimeMillis()
        )

        return userRepository.createUser(user, password)
    }
}
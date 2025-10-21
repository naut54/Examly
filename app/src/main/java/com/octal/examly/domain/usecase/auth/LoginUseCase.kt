package com.octal.examly.domain.usecase.auth

import com.octal.examly.domain.model.User
import com.octal.examly.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank()) {
            return Result.failure(Exception("El nombre de usuario no puede estar vacío"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("La contraseña no puede estar vacía"))
        }

        return authRepository.login(username, password)
    }
}
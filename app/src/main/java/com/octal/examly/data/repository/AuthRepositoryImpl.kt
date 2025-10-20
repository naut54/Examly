package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.UserDao
import com.octal.examly.data.mapper.toDomain
import com.octal.examly.domain.model.User
import com.octal.examly.domain.repository.AuthRepository
import com.octal.examly.util.PasswordHasher
import com.octal.examly.util.SessionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val passwordHasher: PasswordHasher
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val userEntity = userDao.getByUsername(username)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            val isPasswordCorrect = passwordHasher.verify(password, userEntity.passwordHash)

            if (!isPasswordCorrect) {
                return Result.failure(Exception("Contraseña incorrecta"))
            }

            val user = userEntity.toDomain()

            sessionManager.saveUser(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error al iniciar sesión: ${e.message}", e))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al cerrar sesión: ${e.message}", e))
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return sessionManager.getUser()
    }

    override suspend fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}
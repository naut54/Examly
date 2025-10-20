package com.octal.examly.domain.repository

import com.octal.examly.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    suspend fun isLoggedIn(): Boolean
}
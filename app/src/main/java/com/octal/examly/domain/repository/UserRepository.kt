package com.octal.examly.domain.repository

import com.octal.examly.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createUser(user: User, password: String): Result<Long>
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserById(userId: Long): Result<User>
    suspend fun deleteUser(userId: Long): Result<Unit>
}
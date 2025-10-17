package com.octal.examly.domain.repository

class AuthRepository {
    suspend fun login(email: String, password: String) {}

    suspend fun logout() {}

    suspend fun getCurrentUser() {}
}
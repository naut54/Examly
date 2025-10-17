package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.UserDao
import com.octal.examly.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val dao: UserDao
) : AuthRepository {
    override suspend fun login(email: String, password: String) {

    }
}
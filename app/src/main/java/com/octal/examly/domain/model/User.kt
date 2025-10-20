package com.octal.examly.domain.model

data class User(
    val id: Long,
    val username: String,
    val role: UserRole,
    val createdAt: Long
)
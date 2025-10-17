package com.octal.examly.domain.model

data class User(
    val id: Int,
    val username: String,
    val role: String,
    val createdAt: Long
)
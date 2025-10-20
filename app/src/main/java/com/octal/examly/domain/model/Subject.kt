package com.octal.examly.domain.model

data class Subject(
    val id: Long = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
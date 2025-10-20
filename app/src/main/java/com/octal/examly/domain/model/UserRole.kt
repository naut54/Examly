package com.octal.examly.domain.model

enum class UserRole {
    USER,

    ADMIN;

    fun isAdmin(): Boolean = this == ADMIN

    fun isUser(): Boolean = this == USER

    companion object {
        fun fromString(value: String): UserRole {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                USER
            }
        }
    }
}
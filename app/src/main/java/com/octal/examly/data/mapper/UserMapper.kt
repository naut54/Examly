package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.UserEntity
import com.octal.examly.domain.model.User
import com.octal.examly.domain.model.UserRole

fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
        username = this.username,
        role = UserRole.valueOf(this.role),
        createdAt = this.createdAt
    )
}

fun User.toEntity(passwordHash: String): UserEntity {
    return UserEntity(
        id = this.id,
        username = this.username,
        passwordHash = passwordHash,
        role = this.role.name,
        createdAt = this.createdAt
    )
}
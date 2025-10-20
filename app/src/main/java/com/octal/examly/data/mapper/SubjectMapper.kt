package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.SubjectEntity
import com.octal.examly.domain.model.Subject

fun SubjectEntity.toDomain(): Subject {
    return Subject(
        id = this.id,
        name = this.name,
        description = this.description,
        createdAt = this.createdAt
    )
}

fun Subject.toEntity(): SubjectEntity {
    return SubjectEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        createdAt = this.createdAt
    )
}
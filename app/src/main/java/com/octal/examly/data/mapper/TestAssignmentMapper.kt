package com.octal.examly.data.mapper

import com.octal.examly.data.local.entities.TestAssignmentEntity
import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.domain.model.TestAssignmentStatus

fun TestAssignmentEntity.toDomain(): TestAssignment {
    return TestAssignment(
        id = this.id,
        testId = this.testId,
        userId = this.userId,
        assignedBy = this.assignedBy,
        assignedAt = this.assignedAt,
        deadline = this.deadline,
        status = TestAssignmentStatus.valueOf(this.status)
    )
}

fun TestAssignment.toEntity(): TestAssignmentEntity {
    return TestAssignmentEntity(
        id = this.id,
        testId = this.testId,
        userId = this.userId,
        assignedBy = this.assignedBy,
        assignedAt = this.assignedAt,
        deadline = this.deadline,
        status = this.status.name
    )
}
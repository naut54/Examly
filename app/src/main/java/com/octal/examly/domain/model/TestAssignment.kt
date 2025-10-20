package com.octal.examly.domain.model

data class TestAssignment(
    val id: Long = 0,
    val testId: Long,
    val userId: Long,
    val assignedBy: Long?,
    val assignedAt: Long = System.currentTimeMillis(),
    val deadline: Long? = null,
    val status: TestAssignmentStatus
)

enum class TestAssignmentStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
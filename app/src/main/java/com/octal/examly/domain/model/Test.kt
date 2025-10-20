package com.octal.examly.domain.model

data class Test(
    val id: Long = 0,
    val title: String,
    val description: String,
    val subjectId: Long,
    val mode: TestMode,
    val configuration: TestConfiguration,
    val createdBy: Int?,
    val createdAt: Long = System.currentTimeMillis()
)
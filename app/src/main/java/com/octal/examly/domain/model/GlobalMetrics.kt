package com.octal.examly.domain.model

data class GlobalMetrics(
    val totalTests: Int = 0,
    val totalUsers: Int = 0,
    val totalResults: Int = 0,
    val averageScore: Double = 0.0,
    val passRate: Double = 0.0,
    val failRate: Double = 0.0,
    val averageTimeSpent: Long = 0L,
    val completionRate: Double = 0.0,
    val subjectBreakdown: List<SubjectStats> = emptyList()
)

data class SubjectStats(
    val subjectId: Long,
    val subjectName: String,
    val testsCount: Int,
    val averageScore: Double
)
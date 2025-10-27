package com.octal.examly.domain.model

data class PlatformMetrics(
    val totalUsers: Int = 0,
    val totalTests: Int = 0,
    val totalSubjects: Int = 0,
    val totalQuestions: Int = 0,
    val totalResults: Int = 0,
    val activeAssignments: Int = 0,
    val recentActivityCount: Int = 0,
    val totalAssignments: Int = 0,
    val completedAssignments: Int = 0,
    val averageScore: Double = 0.0
)
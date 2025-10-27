package com.octal.examly.domain.usecase.dashboard

import com.octal.examly.data.local.dao.TestAssignmentDao
import com.octal.examly.data.local.dao.TestResultDao
import javax.inject.Inject

class GetUserDashboardStatsUseCase @Inject constructor(
    private val testAssignmentDao: TestAssignmentDao,
    private val testResultDao: TestResultDao
) {
    suspend operator fun invoke(userId: Long): Result<UserDashboardStats> {
        return try {
            val assignedCount = testAssignmentDao.getCountByUserId(userId)
            val pendingCount = testAssignmentDao.getPendingCountByUserId(userId)
            val recentResultsCount = testResultDao.getRecentCountByUserId(userId, 5)

            val stats = UserDashboardStats(
                assignedTestsCount = assignedCount,
                pendingTestsCount = pendingCount,
                recentResultsCount = recentResultsCount
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener estadísticas del usuario: ${e.message}", e))
        }
    }
}

data class UserDashboardStats(
    val assignedTestsCount: Int,
    val pendingTestsCount: Int,
    val recentResultsCount: Int
)

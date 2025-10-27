package com.octal.examly.domain.usecase.dashboard

import com.octal.examly.data.local.dao.SubjectDao
import com.octal.examly.data.local.dao.TestDao
import com.octal.examly.data.local.dao.UserDao
import javax.inject.Inject

class GetAdminDashboardStatsUseCase @Inject constructor(
    private val userDao: UserDao,
    private val testDao: TestDao,
    private val subjectDao: SubjectDao
) {
    suspend operator fun invoke(): Result<AdminDashboardStats> {
        return try {
            val totalUsers = userDao.getTotalCount()
            val totalTests = testDao.getTotalCount()
            val totalSubjects = subjectDao.getTotalCount()

            val stats = AdminDashboardStats(
                totalUsersCount = totalUsers,
                totalTestsCount = totalTests,
                totalSubjectsCount = totalSubjects
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener estadísticas del administrador: ${e.message}", e))
        }
    }
}

data class AdminDashboardStats(
    val totalUsersCount: Int,
    val totalTestsCount: Int,
    val totalSubjectsCount: Int
)

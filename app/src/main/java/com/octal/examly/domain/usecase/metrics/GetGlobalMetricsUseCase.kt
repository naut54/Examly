package com.octal.examly.domain.usecase.metrics

import com.octal.examly.data.local.dao.*
import com.octal.examly.domain.model.GlobalMetrics
import com.octal.examly.domain.model.SubjectStats
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetGlobalMetricsUseCase @Inject constructor(
    private val testResultDao: TestResultDao,
    private val userDao: UserDao,
    private val testDao: TestDao,
    private val subjectDao: SubjectDao,
    private val testAssignmentDao: TestAssignmentDao
) {
    suspend operator fun invoke(): Result<GlobalMetrics> {
        return try {
            val totalTests = testDao.getTotalCount()
            val totalUsers = userDao.getTotalCount()
            val totalResults = testResultDao.getTotalResultsCount()

            val averageScore = (testResultDao.getAverageScore() ?: 0.0) / 100.0
            val averageTimeSpent = testResultDao.getAverageTimeSpent() ?: 0L

            val passedCount = testResultDao.getPassedCount()
            val failedCount = testResultDao.getFailedCount()
            val totalWithResults = passedCount + failedCount

            val passRate = if (totalWithResults > 0) {
                passedCount.toDouble() / totalWithResults
            } else {
                0.0
            }

            val failRate = if (totalWithResults > 0) {
                failedCount.toDouble() / totalWithResults
            } else {
                0.0
            }

            val totalAssignments = testAssignmentDao.getTotalCount()
            val completedAssignments = testResultDao.getTotalResultsCount()

            val completionRate = if (totalAssignments > 0) {
                completedAssignments.toDouble() / totalAssignments
            } else {
                0.0
            }

            val subjectBreakdown = calculateSubjectBreakdown()

            val metrics = GlobalMetrics(
                totalTests = totalTests,
                totalUsers = totalUsers,
                totalResults = totalResults,
                averageScore = averageScore,
                passRate = passRate,
                failRate = failRate,
                averageTimeSpent = averageTimeSpent,
                completionRate = completionRate,
                subjectBreakdown = subjectBreakdown
            )

            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(Exception("Error al calcular métricas globales: ${e.message}", e))
        }
    }

    private suspend fun calculateSubjectBreakdown(): List<SubjectStats> {
        return try {
            val subjects = subjectDao.getAll().first()

            subjects.map { subjectEntity ->
                val results = testResultDao.getResultsBySubjectId(subjectEntity.id)

                val testsCount = results.size
                val averageScore = if (testsCount > 0) {
                    (results.map { it.score }.average()) / 100.0
                } else {
                    0.0
                }

                SubjectStats(
                    subjectId = subjectEntity.id,
                    subjectName = subjectEntity.name,
                    testsCount = testsCount,
                    averageScore = averageScore
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

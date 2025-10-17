package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.UserAnswerEntity

@Dao
interface UserAnswerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(userAnswer: UserAnswerEntity)

    @Query("SELECT * FROM user_answers WHERE attemptId = :attemptId")
    suspend fun getByAttemptId(attemptId: Int): List<UserAnswerEntity>

    @Query("SELECT * FROM user_answers WHERE questionId = :questionId AND attemptId = :attemptId")
    suspend fun getByQuestionAndAttempt(questionId: Int, attemptId: Int): UserAnswerEntity?

    @Update
    suspend fun update(userAnswer: UserAnswerEntity)

    @Delete
    suspend fun delete(userAnswer: UserAnswerEntity)
}
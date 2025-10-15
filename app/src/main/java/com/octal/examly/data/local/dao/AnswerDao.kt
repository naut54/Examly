package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.AnswerEntity

@Dao
interface AnswerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(answer: AnswerEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(answers: List<AnswerEntity>): List<Long>

    @Query("SELECT * FROM answers WHERE questionId = :questionId")
    suspend fun getByQuestionId(questionId: Int): List<AnswerEntity>

    @Query("SELECT * FROM answers WHERE questionId = :questionId AND isCorrect = 1")
    suspend fun getCorrectAnswersByQuestionId(questionId: Int): List<AnswerEntity>

    @Update
    suspend fun update(answer: AnswerEntity): Int

    @Delete
    suspend fun delete(answer: AnswerEntity): Int
}
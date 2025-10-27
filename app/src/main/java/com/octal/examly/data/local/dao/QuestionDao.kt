package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(question: QuestionEntity): Long

    @Query("SELECT * FROM questions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getById(questionId: Long): QuestionEntity

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun getBySubjectId(subjectId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsBySubject(subjectId: Long, limit: Int?): List<QuestionEntity>

    @Update
    suspend fun update(question: QuestionEntity): Int

    @Delete
    suspend fun delete(question: QuestionEntity): Int
}
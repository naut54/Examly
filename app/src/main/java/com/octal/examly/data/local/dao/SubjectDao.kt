package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(subject: SubjectEntity): Long

    @Query("SELECT * FROM subjects ORDER BY createdAt DESC")
    fun getAll(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    suspend fun getById(subjectId: Int): SubjectEntity

    @Update
    suspend fun update(subject: SubjectEntity): Int


    @Delete
    suspend fun delete(subject: SubjectEntity): Int
}
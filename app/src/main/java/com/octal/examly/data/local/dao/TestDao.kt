package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.TestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(test: TestEntity): Long

    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE id = :testId")
    suspend fun getById(testId: Int): TestEntity

    @Query("SELECT * FROM tests WHERE createdBy = :creatorId ORDER BY createdAt DESC")
    fun getByCreatorId(creatorId: Int): Flow<List<TestEntity>>

    @Update
    suspend fun update(test: TestEntity): Int

    @Delete
    suspend fun delete(test: TestEntity): Int
}
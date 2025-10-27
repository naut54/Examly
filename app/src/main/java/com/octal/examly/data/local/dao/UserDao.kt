package com.octal.examly.data.local.dao

import androidx.room.*
import com.octal.examly.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getByUsername(username: String): UserEntity?

    @Update
    suspend fun update(user: UserEntity): Int

    @Delete
    suspend fun delete(user: UserEntity): Int

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalCount(): Int
}
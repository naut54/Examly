package com.octal.examly.data.repository

import com.octal.examly.data.local.dao.UserDao
import com.octal.examly.data.mapper.toDomain
import com.octal.examly.data.mapper.toEntity
import com.octal.examly.domain.model.User
import com.octal.examly.domain.repository.UserRepository
import com.octal.examly.util.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher
) : UserRepository {

    override suspend fun createUser(user: User, password: String): Result<Long> {
        return try {
            val existingUser = userDao.getByUsername(user.username)
            if (existingUser != null) {
                return Result.failure(Exception("El usuario ya existe"))
            }

            val passwordHash = passwordHasher.hash(password)

            val userEntity = user.toEntity(passwordHash)
            val userId = userDao.insert(userEntity)

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear usuario: ${e.message}", e))
        }
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getUserById(userId: Long): Result<User> {
        return try {
            val userEntity = userDao.getById(userId)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            Result.success(userEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuario: ${e.message}", e))
        }
    }

    override suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            val userEntity = userDao.getById(userId)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            userDao.delete(userEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar usuario: ${e.message}", e))
        }
    }
}
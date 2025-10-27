package com.octal.examly.data.local.database

import com.octal.examly.data.local.dao.UserDao
import com.octal.examly.data.local.entities.UserEntity
import com.octal.examly.domain.model.UserRole
import com.octal.examly.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher
) {

    companion object {
        private const val DEFAULT_ADMIN_USERNAME = "admin"
        private const val DEFAULT_ADMIN_PASSWORD = "admin123"
    }

    fun seedDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingAdmin = userDao.getByUsername(DEFAULT_ADMIN_USERNAME)

                if (existingAdmin == null) {
                    val hashedPassword = passwordHasher.hash(DEFAULT_ADMIN_PASSWORD)

                    val adminUser = UserEntity(
                        username = DEFAULT_ADMIN_USERNAME,
                        passwordHash = hashedPassword,
                        role = UserRole.ADMIN.name,
                        createdAt = System.currentTimeMillis()
                    )

                    userDao.insert(adminUser)

                    android.util.Log.i(
                        "DatabaseSeeder",
                        "Default admin user created - Username: $DEFAULT_ADMIN_USERNAME, Password: $DEFAULT_ADMIN_PASSWORD"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DatabaseSeeder", "Error seeding database", e)
            }
        }
    }
}

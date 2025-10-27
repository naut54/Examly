package com.octal.examly.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.octal.examly.domain.model.User
import com.octal.examly.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = user.id
            preferences[PreferencesKeys.USERNAME] = user.username
            preferences[PreferencesKeys.USER_ROLE] = user.role.name
        }
    }

    fun getUserFlow(): Flow<User?> {
        return context.dataStore.data.map { preferences ->
            val userId = preferences[PreferencesKeys.USER_ID]
            val username = preferences[PreferencesKeys.USERNAME]
            val roleString = preferences[PreferencesKeys.USER_ROLE]

            if (userId != null && username != null && roleString != null) {
                User(
                    id = userId,
                    username = username,
                    role = UserRole.valueOf(roleString),
                    createdAt = System.currentTimeMillis()
                )
            } else {
                null
            }
        }
    }

    suspend fun getUser(): User? {
        val preferences = context.dataStore.data.first()
        val userId = preferences[PreferencesKeys.USER_ID]
        val username = preferences[PreferencesKeys.USERNAME]
        val roleString = preferences[PreferencesKeys.USER_ROLE]

        return if (userId != null && username != null && roleString != null) {
            User(
                id = userId,
                username = username,
                role = UserRole.valueOf(roleString),
                createdAt = System.currentTimeMillis()
            )
        } else {
            null
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.USER_ID] != null
    }
}
package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.User
import com.octal.examly.domain.usecase.auth.GetCurrentUserUseCase
import com.octal.examly.domain.usecase.auth.LogoutUseCase
import com.octal.examly.domain.usecase.dashboard.AdminDashboardStats
import com.octal.examly.domain.usecase.dashboard.GetAdminDashboardStatsUseCase
import com.octal.examly.domain.usecase.dashboard.GetUserDashboardStatsUseCase
import com.octal.examly.domain.usecase.dashboard.UserDashboardStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getUserDashboardStatsUseCase: GetUserDashboardStatsUseCase,
    private val getAdminDashboardStatsUseCase: GetAdminDashboardStatsUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userDashboardStats = MutableStateFlow<UserDashboardStats?>(null)
    val userDashboardStats: StateFlow<UserDashboardStats?> = _userDashboardStats.asStateFlow()

    private val _adminDashboardStats = MutableStateFlow<AdminDashboardStats?>(null)
    val adminDashboardStats: StateFlow<AdminDashboardStats?> = _adminDashboardStats.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun loadUserDashboardStats(userId: Long) {
        viewModelScope.launch {
            val result = getUserDashboardStatsUseCase(userId)
            result.onSuccess { stats ->
                _userDashboardStats.value = stats
            }
        }
    }

    fun loadAdminDashboardStats() {
        viewModelScope.launch {
            val result = getAdminDashboardStatsUseCase()
            result.onSuccess { stats ->
                _adminDashboardStats.value = stats
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _currentUser.value = null
            _userDashboardStats.value = null
            _adminDashboardStats.value = null
        }
    }
}

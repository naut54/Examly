package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _isDarkModeEnabled = MutableStateFlow(false)
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(true)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.isDarkModeEnabled().collect { enabled ->
                _isDarkModeEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            preferencesManager.areNotificationsEnabled().collect { enabled ->
                _isNotificationsEnabled.value = enabled
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDarkModeEnabled.value = enabled
            preferencesManager.setDarkMode(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _isNotificationsEnabled.value = enabled
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun clearAppData() {
        viewModelScope.launch {
            preferencesManager.clearAllPreferences()

            _isDarkModeEnabled.value = false
            _isNotificationsEnabled.value = true
        }
    }
}

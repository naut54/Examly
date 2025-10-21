package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationDestination>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase().first()

                if (currentUser != null) {
                    _navigationEvent.emit(NavigationDestination.Main)
                } else {
                    _navigationEvent.emit(NavigationDestination.Login)
                }
            } catch (e: Exception) {
                // En caso de error, ir a login
                _navigationEvent.emit(NavigationDestination.Login)
            }
        }
    }

    sealed class NavigationDestination {
        object Login : NavigationDestination()
        object Main : NavigationDestination()
    }
}
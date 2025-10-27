package com.octal.examly.presentation.state

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()

    object Loading : UiState<Nothing>()

    object Empty : UiState<Nothing>()

    data class Success<T>(val data: T) : UiState<T>()

    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : UiState<Nothing>()

    fun isLoading(): Boolean = this is Loading

    fun isSuccess(): Boolean = this is Success

    fun isError(): Boolean = this is Error

    fun getDataOrNull(): T? = if (this is Success) data else null
}

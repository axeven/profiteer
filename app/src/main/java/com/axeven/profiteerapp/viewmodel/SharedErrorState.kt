package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ErrorUiState(
    val message: String? = null,
    val shouldRetry: Boolean = false,
    val requiresReauth: Boolean = false,
    val isOffline: Boolean = false
)

@Singleton
class SharedErrorViewModel @Inject constructor() : ViewModel() {

    private val _errorState = MutableStateFlow(ErrorUiState())
    val errorState: StateFlow<ErrorUiState> = _errorState.asStateFlow()

    fun showError(
        message: String,
        shouldRetry: Boolean = false,
        requiresReauth: Boolean = false,
        isOffline: Boolean = false
    ) {
        _errorState.value = ErrorUiState(
            message = message,
            shouldRetry = shouldRetry,
            requiresReauth = requiresReauth,
            isOffline = isOffline
        )
    }

    fun clearError() {
        _errorState.value = ErrorUiState()
    }

    fun hasError(): Boolean = _errorState.value.message != null
}
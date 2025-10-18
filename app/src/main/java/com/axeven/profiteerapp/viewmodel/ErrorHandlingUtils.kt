package com.axeven.profiteerapp.viewmodel

import com.axeven.profiteerapp.data.model.RepositoryError

/**
 * Shared error handling utilities for ViewModels.
 *
 * Provides extension functions to convert RepositoryError exceptions into
 * user-friendly error messages and determine appropriate error handling strategies.
 *
 * Usage in ViewModels:
 * ```kotlin
 * try {
 *     repository.getData().collect { data ->
 *         // Process data
 *     }
 * } catch (e: Exception) {
 *     val errorMessage = e.toUserMessage()
 *     _uiState.update { it.copy(error = errorMessage) }
 *
 *     // Optionally show critical errors via SharedErrorViewModel
 *     if (e.isCriticalError()) {
 *         sharedErrorViewModel.showError(errorMessage)
 *     }
 * }
 * ```
 */

/**
 * Converts any exception to a user-friendly error message.
 *
 * For RepositoryError types, extracts the userMessage field.
 * For other exceptions, uses the exception message or a generic fallback.
 *
 * @return User-friendly error message suitable for display in UI
 */
fun Throwable.toUserMessage(): String {
    return when (this) {
        is RepositoryError.FirestoreListener -> this.userMessage
        is RepositoryError.FirestoreCrud -> this.userMessage
        is RepositoryError.NetworkError -> this.userMessage
        is RepositoryError.AuthenticationError -> this.userMessage
        is RepositoryError.DataValidationError -> this.userMessage
        is RepositoryError.ResourceNotFound -> this.userMessage
        is RepositoryError.UnknownError -> this.userMessage
        is RepositoryError.CompositeError -> this.userMessage
        else -> this.message ?: "An unexpected error occurred"
    }
}

/**
 * Determines if this error is critical and should be shown via SharedErrorViewModel.
 *
 * Critical errors are those that:
 * - Require re-authentication
 * - Are authentication/permission errors
 * - Are not network/offline errors (already handled locally)
 *
 * @return true if error should be shown via SharedErrorViewModel, false otherwise
 */
fun Throwable.isCriticalError(): Boolean {
    return when (this) {
        is RepositoryError.AuthenticationError -> true
        is RepositoryError.FirestoreListener -> this.requiresReauth
        is RepositoryError.FirestoreCrud -> this.requiresReauth
        is RepositoryError.CompositeError -> this.requiresReauth
        else -> false // Non-repository errors are not considered critical
    }
}

/**
 * Determines if this error is retryable by the user.
 *
 * Retryable errors include:
 * - Network/offline errors
 * - Errors with shouldRetry flag set
 *
 * Use this to show a "Retry" button in the UI.
 *
 * @return true if the operation should be retryable, false otherwise
 */
fun Throwable.isRetryable(): Boolean {
    return when (this) {
        is RepositoryError.NetworkError -> true
        is RepositoryError.FirestoreListener -> this.shouldRetry
        is RepositoryError.FirestoreCrud -> this.shouldRetry
        is RepositoryError.CompositeError -> this.shouldRetry
        else -> false
    }
}

/**
 * Determines if this error is due to offline/network issues.
 *
 * Use this to show an offline indicator or network error banner.
 *
 * @return true if error is network-related, false otherwise
 */
fun Throwable.isOfflineError(): Boolean {
    return when (this) {
        is RepositoryError.NetworkError -> true
        is RepositoryError.FirestoreListener -> this.isOffline
        is RepositoryError.CompositeError -> this.isOffline
        else -> false
    }
}

/**
 * Gets the operation that failed, if available.
 *
 * Useful for logging or showing context-specific error messages.
 *
 * @return Operation name (e.g., "getUserTransactions") or null
 */
fun Throwable.getFailedOperation(): String? {
    return when (this) {
        is RepositoryError.FirestoreListener -> this.operation
        is RepositoryError.FirestoreCrud -> this.operation
        is RepositoryError.NetworkError -> this.operation
        is RepositoryError.AuthenticationError -> this.operation
        is RepositoryError.DataValidationError -> this.operation
        is RepositoryError.ResourceNotFound -> this.operation
        is RepositoryError.UnknownError -> this.operation
        is RepositoryError.CompositeError -> this.operation
        else -> null
    }
}

/**
 * Data class for structured error information to update UI state.
 *
 * Contains all relevant error properties for ViewModels to update their UI state.
 *
 * Usage:
 * ```kotlin
 * catch (e: Exception) {
 *     val errorInfo = e.toErrorInfo()
 *     _uiState.update {
 *         it.copy(
 *             error = errorInfo.message,
 *             isOffline = errorInfo.isOffline,
 *             canRetry = errorInfo.isRetryable
 *         )
 *     }
 * }
 * ```
 */
data class ErrorInfo(
    val message: String,
    val isOffline: Boolean = false,
    val isRetryable: Boolean = false,
    val isCritical: Boolean = false,
    val operation: String? = null
)

/**
 * Converts any exception to structured ErrorInfo for UI state updates.
 *
 * @return ErrorInfo with all relevant properties extracted from the exception
 */
fun Throwable.toErrorInfo(): ErrorInfo {
    return ErrorInfo(
        message = this.toUserMessage(),
        isOffline = this.isOfflineError(),
        isRetryable = this.isRetryable(),
        isCritical = this.isCriticalError(),
        operation = this.getFailedOperation()
    )
}

/**
 * Handles repository errors by updating UI state and optionally showing critical errors.
 *
 * This is a convenience function that combines common error handling patterns.
 *
 * @param error The exception that occurred
 * @param updateState Lambda to update UI state with error info
 * @param sharedErrorViewModel Optional SharedErrorViewModel to show critical errors
 *
 * Usage:
 * ```kotlin
 * catch (e: Exception) {
 *     handleRepositoryError(
 *         error = e,
 *         updateState = { errorInfo ->
 *             _uiState.update { it.copy(error = errorInfo.message) }
 *         },
 *         sharedErrorViewModel = sharedErrorViewModel
 *     )
 * }
 * ```
 */
fun handleRepositoryError(
    error: Throwable,
    updateState: (ErrorInfo) -> Unit,
    sharedErrorViewModel: com.axeven.profiteerapp.viewmodel.SharedErrorViewModel? = null
) {
    val errorInfo = error.toErrorInfo()

    // Update ViewModel state
    updateState(errorInfo)

    // Show critical errors via SharedErrorViewModel if provided
    if (errorInfo.isCritical && sharedErrorViewModel != null) {
        sharedErrorViewModel.showError(errorInfo.message)
    }
}

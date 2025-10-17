package com.axeven.profiteerapp.data.model

/**
 * Domain-specific error types for repository layer.
 *
 * This sealed class hierarchy replaces direct UI calls (SharedErrorViewModel)
 * in repositories, enforcing proper separation of concerns while preserving
 * all error context needed for user-facing error messages.
 *
 * Usage:
 * ```kotlin
 * // In Repository
 * if (error != null) {
 *     val errorInfo = FirestoreErrorHandler.handleError(error, logger)
 *     close(RepositoryError.FirestoreListener(...))
 * }
 *
 * // In ViewModel
 * repository.getData(userId).catch { error ->
 *     when (error) {
 *         is RepositoryError.FirestoreListener -> handleFirestoreError(error)
 *         is RepositoryError.NetworkError -> handleNetworkError(error)
 *         else -> handleUnknownError(error)
 *     }
 * }.collect { data -> ... }
 * ```
 *
 * @see com.axeven.profiteerapp.utils.FirestoreErrorHandler
 * @see com.axeven.profiteerapp.viewmodel.SharedErrorViewModel
 */
sealed class RepositoryError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Firestore real-time listener encountered an error.
     *
     * This is the most common repository error type, occurring when
     * Firestore snapshot listeners fail due to permissions, network,
     * or authentication issues.
     *
     * **Affected Operations**: All Flow-based repository methods that use
     * `addSnapshotListener()` for real-time updates.
     *
     * @property operation Name of the repository operation that failed (e.g., "getUserWallets")
     * @property userMessage User-friendly error message (from FirestoreErrorHandler)
     * @property shouldRetry Whether the operation can/should be retried
     * @property requiresReauth Whether user needs to re-authenticate
     * @property isOffline Whether error is due to offline status
     * @property cause Original Firestore exception
     */
    data class FirestoreListener(
        val operation: String,
        val userMessage: String,
        val shouldRetry: Boolean,
        val requiresReauth: Boolean,
        val isOffline: Boolean,
        override val cause: Throwable?
    ) : RepositoryError(
        message = "Firestore listener error in $operation: $userMessage",
        cause = cause
    )

    /**
     * Firestore CRUD operation failed.
     *
     * Unlike FirestoreListener which is for real-time listeners,
     * this error type is for one-time suspend function calls
     * (create, read, update, delete operations).
     *
     * **Note**: Most CRUD operations already return `Result<T>` and don't
     * throw exceptions. This type is for consistency and future use.
     *
     * @property operation Name of the CRUD operation (e.g., "createWallet")
     * @property resourceType Type of resource being operated on (e.g., "Wallet", "Transaction")
     * @property resourceId Optional ID of the specific resource
     * @property userMessage User-friendly error message
     * @property shouldRetry Whether the operation can/should be retried
     * @property requiresReauth Whether user needs to re-authenticate
     * @property cause Original exception
     */
    data class FirestoreCrud(
        val operation: String,
        val resourceType: String,
        val resourceId: String? = null,
        val userMessage: String,
        val shouldRetry: Boolean,
        val requiresReauth: Boolean,
        override val cause: Throwable?
    ) : RepositoryError(
        message = "Firestore CRUD error in $operation for $resourceType${resourceId?.let { " (ID: $it)" } ?: ""}: $userMessage",
        cause = cause
    )

    /**
     * Network connectivity error.
     *
     * Special case of Firestore errors where the root cause is network
     * connectivity (offline, timeout, DNS failure, etc.).
     *
     * @property operation Name of the operation that failed
     * @property userMessage User-friendly error message
     * @property isTimeout Whether error was specifically a timeout
     * @property cause Original network exception
     */
    data class NetworkError(
        val operation: String,
        val userMessage: String,
        val isTimeout: Boolean = false,
        override val cause: Throwable?
    ) : RepositoryError(
        message = "Network error in $operation: $userMessage",
        cause = cause
    )

    /**
     * Authentication/authorization error.
     *
     * User lacks permission to perform the operation, or their
     * authentication token is invalid/expired.
     *
     * **Important**: Repository-level auth recovery (token refresh)
     * should still happen before creating this error. This error
     * indicates that recovery failed and user action is required.
     *
     * @property operation Name of the operation that failed
     * @property userMessage User-friendly error message
     * @property requiresReauth Whether re-authentication flow should be triggered
     * @property permissionType Type of permission that was denied (optional)
     * @property cause Original auth exception
     */
    data class AuthenticationError(
        val operation: String,
        val userMessage: String,
        val requiresReauth: Boolean,
        val permissionType: String? = null,
        override val cause: Throwable?
    ) : RepositoryError(
        message = "Authentication error in $operation: $userMessage",
        cause = cause
    )

    /**
     * Data validation or parsing error.
     *
     * Firebase document could not be parsed into expected model,
     * or data doesn't meet validation requirements.
     *
     * @property operation Name of the operation that failed
     * @property resourceType Type of resource being parsed (e.g., "Transaction", "Wallet")
     * @property documentId Firestore document ID that failed to parse
     * @property userMessage User-friendly error message
     * @property cause Original parsing/validation exception
     */
    data class DataValidationError(
        val operation: String,
        val resourceType: String,
        val documentId: String?,
        val userMessage: String,
        override val cause: Throwable?
    ) : RepositoryError(
        message = "Data validation error in $operation for $resourceType${documentId?.let { " (ID: $it)" } ?: ""}: $userMessage",
        cause = cause
    )

    /**
     * Resource not found error.
     *
     * Requested Firestore document doesn't exist.
     *
     * @property operation Name of the operation that failed
     * @property resourceType Type of resource being searched (e.g., "Transaction", "Wallet")
     * @property resourceId ID of the resource that wasn't found
     * @property userMessage User-friendly error message
     */
    data class ResourceNotFound(
        val operation: String,
        val resourceType: String,
        val resourceId: String,
        val userMessage: String
    ) : RepositoryError(
        message = "Resource not found in $operation: $resourceType with ID $resourceId"
    )

    /**
     * Unknown or unexpected error.
     *
     * Catch-all for errors that don't fit other categories.
     * Should be rare if error handling is comprehensive.
     *
     * @property operation Name of the operation that failed
     * @property userMessage User-friendly error message (or technical message if none available)
     * @property cause Original exception
     */
    data class UnknownError(
        val operation: String,
        val userMessage: String,
        override val cause: Throwable?
    ) : RepositoryError(
        message = "Unknown error in $operation: $userMessage",
        cause = cause
    )

    /**
     * Composite error for operations with multiple parallel queries.
     *
     * Used by `getWalletTransactions()` which runs 4 parallel Firestore
     * queries. Allows aggregating multiple errors without spamming user
     * with 4 separate error messages.
     *
     * **Pattern**: Collect errors from all queries, then emit single
     * composite error if any failed.
     *
     * @property operation Name of the composite operation
     * @property errors List of individual errors from parallel queries
     * @property userMessage Aggregated user-friendly error message
     */
    data class CompositeError(
        val operation: String,
        val errors: List<RepositoryError>,
        val userMessage: String
    ) : RepositoryError(
        message = "Composite error in $operation: ${errors.size} errors occurred"
    ) {
        /**
         * Returns true if any sub-error requires re-authentication.
         */
        val requiresReauth: Boolean
            get() = errors.any {
                when (it) {
                    is FirestoreListener -> it.requiresReauth
                    is FirestoreCrud -> it.requiresReauth
                    is AuthenticationError -> it.requiresReauth
                    else -> false
                }
            }

        /**
         * Returns true if any sub-error should be retried.
         */
        val shouldRetry: Boolean
            get() = errors.any {
                when (it) {
                    is FirestoreListener -> it.shouldRetry
                    is FirestoreCrud -> it.shouldRetry
                    is NetworkError -> !it.isTimeout
                    else -> false
                }
            }

        /**
         * Returns true if any sub-error is due to offline status.
         */
        val isOffline: Boolean
            get() = errors.any {
                when (it) {
                    is FirestoreListener -> it.isOffline
                    is NetworkError -> true
                    else -> false
                }
            }
    }
}

/**
 * Extension function to convert FirestoreErrorHandler.ErrorInfo to RepositoryError.
 *
 * Simplifies repository error handling by providing a consistent conversion
 * from FirestoreErrorHandler output to domain error types.
 *
 * Usage in repositories:
 * ```kotlin
 * val errorInfo = FirestoreErrorHandler.handleError(error, logger)
 * val isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
 * close(errorInfo.toRepositoryError(
 *     operation = "getUserWallets",
 *     isOffline = isOffline,
 *     cause = error
 * ))
 * ```
 */
fun com.axeven.profiteerapp.utils.FirestoreErrorHandler.ErrorInfo.toRepositoryError(
    operation: String,
    isOffline: Boolean,
    cause: Throwable?
): RepositoryError {
    return when {
        // If offline, create NetworkError
        isOffline -> RepositoryError.NetworkError(
            operation = operation,
            userMessage = this.userMessage,
            isTimeout = false,
            cause = cause
        )

        // If requires reauth, create AuthenticationError
        this.requiresReauth -> RepositoryError.AuthenticationError(
            operation = operation,
            userMessage = this.userMessage,
            requiresReauth = true,
            cause = cause
        )

        // Otherwise, create FirestoreListener (most common case)
        else -> RepositoryError.FirestoreListener(
            operation = operation,
            userMessage = this.userMessage,
            shouldRetry = this.shouldRetry,
            requiresReauth = this.requiresReauth,
            isOffline = isOffline,
            cause = cause
        )
    }
}

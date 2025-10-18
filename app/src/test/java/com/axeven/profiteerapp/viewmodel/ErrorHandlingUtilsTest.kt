package com.axeven.profiteerapp.viewmodel

import com.axeven.profiteerapp.data.model.RepositoryError
import org.junit.Assert.*
import org.junit.Test

class ErrorHandlingUtilsTest {

    @Test
    fun `toUserMessage should extract message from RepositoryError`() {
        val error = RepositoryError.NetworkError(
            operation = "getUserTransactions",
            userMessage = "No internet connection",
            isTimeout = false,
            cause = null
        )

        assertEquals("No internet connection", error.toUserMessage())
    }

    @Test
    fun `toUserMessage should return exception message for non-RepositoryError`() {
        val error = RuntimeException("Something went wrong")

        assertEquals("Something went wrong", error.toUserMessage())
    }

    @Test
    fun `toUserMessage should return generic message for exception without message`() {
        val error = RuntimeException()

        assertEquals("An unexpected error occurred", error.toUserMessage())
    }

    @Test
    fun `isCriticalError should return true for AuthenticationError`() {
        val error = RepositoryError.AuthenticationError(
            operation = "getUserData",
            userMessage = "Authentication failed",
            requiresReauth = true,
            permissionType = null,
            cause = null
        )

        assertTrue(error.isCriticalError())
    }

    @Test
    fun `isCriticalError should return true when requiresReauth is true`() {
        val error = RepositoryError.FirestoreListener(
            operation = "getUserTransactions",
            userMessage = "Permission denied",
            shouldRetry = false,
            requiresReauth = true,
            isOffline = false,
            cause = null
        )

        assertTrue(error.isCriticalError())
    }

    @Test
    fun `isCriticalError should return false for network errors`() {
        val error = RepositoryError.NetworkError(
            operation = "getUserTransactions",
            userMessage = "No internet connection",
            isTimeout = false,
            cause = null
        )

        assertFalse(error.isCriticalError())
    }

    @Test
    fun `isCriticalError should return false for non-RepositoryError`() {
        val error = RuntimeException("Something went wrong")

        assertFalse(error.isCriticalError())
    }

    @Test
    fun `isRetryable should return true for NetworkError`() {
        val error = RepositoryError.NetworkError(
            operation = "getUserTransactions",
            userMessage = "No internet connection",
            isTimeout = false,
            cause = null
        )

        assertTrue(error.isRetryable())
    }

    @Test
    fun `isRetryable should return true when shouldRetry flag is set`() {
        val error = RepositoryError.FirestoreListener(
            operation = "getUserTransactions",
            userMessage = "Temporary error",
            shouldRetry = true,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        assertTrue(error.isRetryable())
    }

    @Test
    fun `isRetryable should return false when shouldRetry is false`() {
        val error = RepositoryError.FirestoreListener(
            operation = "getUserTransactions",
            userMessage = "Permission denied",
            shouldRetry = false,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        assertFalse(error.isRetryable())
    }

    @Test
    fun `isRetryable should return false for non-RepositoryError`() {
        val error = RuntimeException("Something went wrong")

        assertFalse(error.isRetryable())
    }

    @Test
    fun `isOfflineError should return true for NetworkError`() {
        val error = RepositoryError.NetworkError(
            operation = "getUserTransactions",
            userMessage = "No internet connection",
            isTimeout = false,
            cause = null
        )

        assertTrue(error.isOfflineError())
    }

    @Test
    fun `isOfflineError should return true when isOffline flag is set`() {
        val error = RepositoryError.FirestoreListener(
            operation = "getUserTransactions",
            userMessage = "Connection lost",
            shouldRetry = true,
            requiresReauth = false,
            isOffline = true,
            cause = null
        )

        assertTrue(error.isOfflineError())
    }

    @Test
    fun `isOfflineError should return false when isOffline is false`() {
        val error = RepositoryError.AuthenticationError(
            operation = "getUserData",
            userMessage = "Authentication failed",
            requiresReauth = true,
            permissionType = null,
            cause = null
        )

        assertFalse(error.isOfflineError())
    }

    @Test
    fun `isOfflineError should return false for non-RepositoryError`() {
        val error = RuntimeException("Something went wrong")

        assertFalse(error.isOfflineError())
    }

    @Test
    fun `getFailedOperation should return operation from RepositoryError`() {
        val error = RepositoryError.FirestoreListener(
            operation = "getUserTransactions",
            userMessage = "Error occurred",
            shouldRetry = true,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        assertEquals("getUserTransactions", error.getFailedOperation())
    }

    @Test
    fun `getFailedOperation should return null for non-RepositoryError`() {
        val error = RuntimeException("Something went wrong")

        assertNull(error.getFailedOperation())
    }

    @Test
    fun `toErrorInfo should extract all properties from RepositoryError`() {
        val error = RepositoryError.NetworkError(
            operation = "getUserTransactions",
            userMessage = "No internet connection",
            isTimeout = false,
            cause = null
        )

        val errorInfo = error.toErrorInfo()

        assertEquals("No internet connection", errorInfo.message)
        assertTrue(errorInfo.isOffline)
        assertTrue(errorInfo.isRetryable)
        assertFalse(errorInfo.isCritical)
        assertEquals("getUserTransactions", errorInfo.operation)
    }

    @Test
    fun `toErrorInfo should work with AuthenticationError`() {
        val error = RepositoryError.AuthenticationError(
            operation = "getUserData",
            userMessage = "Authentication failed",
            requiresReauth = true,
            permissionType = null,
            cause = null
        )

        val errorInfo = error.toErrorInfo()

        assertEquals("Authentication failed", errorInfo.message)
        assertFalse(errorInfo.isOffline)
        assertFalse(errorInfo.isRetryable)
        assertTrue(errorInfo.isCritical)
        assertEquals("getUserData", errorInfo.operation)
    }

    @Test
    fun `toErrorInfo should work with non-RepositoryError`() {
        val error = RuntimeException("Something went wrong")

        val errorInfo = error.toErrorInfo()

        assertEquals("Something went wrong", errorInfo.message)
        assertFalse(errorInfo.isOffline)
        assertFalse(errorInfo.isRetryable)
        assertFalse(errorInfo.isCritical)
        assertNull(errorInfo.operation)
    }

    @Test
    fun `toErrorInfo should handle CompositeError properties`() {
        val childError1 = RepositoryError.NetworkError(
            operation = "query1",
            userMessage = "Network error 1",
            isTimeout = false,
            cause = null
        )

        val childError2 = RepositoryError.AuthenticationError(
            operation = "query2",
            userMessage = "Auth error",
            requiresReauth = true,
            permissionType = null,
            cause = null
        )

        val compositeError = RepositoryError.CompositeError(
            operation = "getWalletTransactions",
            errors = listOf(childError1, childError2),
            userMessage = "Multiple errors occurred"
        )

        val errorInfo = compositeError.toErrorInfo()

        assertEquals("Multiple errors occurred", errorInfo.message)
        assertTrue(errorInfo.isOffline) // Has offline child
        assertTrue(errorInfo.isRetryable) // Has retryable child
        assertTrue(errorInfo.isCritical) // Has auth error requiring reauth
        assertEquals("getWalletTransactions", errorInfo.operation)
    }
}

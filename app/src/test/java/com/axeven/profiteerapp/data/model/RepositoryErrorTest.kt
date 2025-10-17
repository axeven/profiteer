package com.axeven.profiteerapp.data.model

import com.axeven.profiteerapp.utils.FirestoreErrorHandler
import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for RepositoryError sealed class hierarchy.
 *
 * Tests ensure proper error type creation, message formatting,
 * and conversion from FirestoreErrorHandler.ErrorInfo.
 *
 * Test coverage:
 * - All RepositoryError types
 * - Error message formatting
 * - Composite error aggregation
 * - ErrorInfo to RepositoryError conversion
 */
class RepositoryErrorTest {

    // region FirestoreListener Error Tests

    @Test
    fun `FirestoreListener error should contain all required fields`() {
        val operation = "getUserWallets"
        val userMessage = "Permission denied"
        val shouldRetry = false
        val requiresReauth = true
        val isOffline = false
        val cause = Exception("Original error")

        val error = RepositoryError.FirestoreListener(
            operation = operation,
            userMessage = userMessage,
            shouldRetry = shouldRetry,
            requiresReauth = requiresReauth,
            isOffline = isOffline,
            cause = cause
        )

        assertEquals(operation, error.operation)
        assertEquals(userMessage, error.userMessage)
        assertEquals(shouldRetry, error.shouldRetry)
        assertEquals(requiresReauth, error.requiresReauth)
        assertEquals(isOffline, error.isOffline)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `FirestoreListener error message should be formatted correctly`() {
        val error = RepositoryError.FirestoreListener(
            operation = "getUserTransactions",
            userMessage = "Network error occurred",
            shouldRetry = true,
            requiresReauth = false,
            isOffline = true,
            cause = null
        )

        val expectedMessage = "Firestore listener error in getUserTransactions: Network error occurred"
        assertEquals(expectedMessage, error.message)
    }

    @Test
    fun `FirestoreListener error should preserve cause chain`() {
        val rootCause = IllegalStateException("Root cause")
        val intermediateCause = Exception("Intermediate", rootCause)

        val error = RepositoryError.FirestoreListener(
            operation = "getUserWallets",
            userMessage = "Error",
            shouldRetry = false,
            requiresReauth = false,
            isOffline = false,
            cause = intermediateCause
        )

        assertEquals(intermediateCause, error.cause)
        assertEquals(rootCause, error.cause?.cause)
    }

    // endregion

    // region FirestoreCrud Error Tests

    @Test
    fun `FirestoreCrud error should contain all required fields`() {
        val operation = "createWallet"
        val resourceType = "Wallet"
        val resourceId = "wallet123"
        val userMessage = "Failed to create wallet"

        val error = RepositoryError.FirestoreCrud(
            operation = operation,
            resourceType = resourceType,
            resourceId = resourceId,
            userMessage = userMessage,
            shouldRetry = true,
            requiresReauth = false,
            cause = null
        )

        assertEquals(operation, error.operation)
        assertEquals(resourceType, error.resourceType)
        assertEquals(resourceId, error.resourceId)
        assertEquals(userMessage, error.userMessage)
        assertTrue(error.shouldRetry)
        assertFalse(error.requiresReauth)
    }

    @Test
    fun `FirestoreCrud error message should include resource ID when present`() {
        val error = RepositoryError.FirestoreCrud(
            operation = "updateTransaction",
            resourceType = "Transaction",
            resourceId = "txn789",
            userMessage = "Update failed",
            shouldRetry = false,
            requiresReauth = false,
            cause = null
        )

        val expectedMessage = "Firestore CRUD error in updateTransaction for Transaction (ID: txn789): Update failed"
        assertEquals(expectedMessage, error.message)
    }

    @Test
    fun `FirestoreCrud error message should work without resource ID`() {
        val error = RepositoryError.FirestoreCrud(
            operation = "deleteWallet",
            resourceType = "Wallet",
            resourceId = null,
            userMessage = "Delete failed",
            shouldRetry = false,
            requiresReauth = false,
            cause = null
        )

        val expectedMessage = "Firestore CRUD error in deleteWallet for Wallet: Delete failed"
        assertEquals(expectedMessage, error.message)
    }

    // endregion

    // region NetworkError Tests

    @Test
    fun `NetworkError should contain all required fields`() {
        val operation = "fetchData"
        val userMessage = "No internet connection"
        val isTimeout = false
        val cause = Exception("Network unreachable")

        val error = RepositoryError.NetworkError(
            operation = operation,
            userMessage = userMessage,
            isTimeout = isTimeout,
            cause = cause
        )

        assertEquals(operation, error.operation)
        assertEquals(userMessage, error.userMessage)
        assertEquals(isTimeout, error.isTimeout)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `NetworkError should handle timeout scenario`() {
        val error = RepositoryError.NetworkError(
            operation = "loadTransactions",
            userMessage = "Request timed out",
            isTimeout = true,
            cause = null
        )

        assertTrue(error.isTimeout)
        val expectedMessage = "Network error in loadTransactions: Request timed out"
        assertEquals(expectedMessage, error.message)
    }

    // endregion

    // region AuthenticationError Tests

    @Test
    fun `AuthenticationError should contain all required fields`() {
        val operation = "getUserData"
        val userMessage = "Authentication required"
        val requiresReauth = true
        val permissionType = "read"

        val error = RepositoryError.AuthenticationError(
            operation = operation,
            userMessage = userMessage,
            requiresReauth = requiresReauth,
            permissionType = permissionType,
            cause = null
        )

        assertEquals(operation, error.operation)
        assertEquals(userMessage, error.userMessage)
        assertEquals(requiresReauth, error.requiresReauth)
        assertEquals(permissionType, error.permissionType)
    }

    @Test
    fun `AuthenticationError message should be formatted correctly`() {
        val error = RepositoryError.AuthenticationError(
            operation = "updateProfile",
            userMessage = "Token expired",
            requiresReauth = true,
            permissionType = "write",
            cause = null
        )

        val expectedMessage = "Authentication error in updateProfile: Token expired"
        assertEquals(expectedMessage, error.message)
    }

    // endregion

    // region DataValidationError Tests

    @Test
    fun `DataValidationError should contain all required fields`() {
        val operation = "parseTransaction"
        val resourceType = "Transaction"
        val documentId = "doc123"
        val userMessage = "Invalid data format"

        val error = RepositoryError.DataValidationError(
            operation = operation,
            resourceType = resourceType,
            documentId = documentId,
            userMessage = userMessage,
            cause = null
        )

        assertEquals(operation, error.operation)
        assertEquals(resourceType, error.resourceType)
        assertEquals(documentId, error.documentId)
        assertEquals(userMessage, error.userMessage)
    }

    @Test
    fun `DataValidationError message should include document ID when present`() {
        val error = RepositoryError.DataValidationError(
            operation = "parseWallet",
            resourceType = "Wallet",
            documentId = "wallet456",
            userMessage = "Missing required field",
            cause = null
        )

        val expectedMessage = "Data validation error in parseWallet for Wallet (ID: wallet456): Missing required field"
        assertEquals(expectedMessage, error.message)
    }

    // endregion

    // region ResourceNotFound Tests

    @Test
    fun `ResourceNotFound should contain all required fields`() {
        val operation = "getWalletById"
        val resourceType = "Wallet"
        val resourceId = "nonexistent123"
        val userMessage = "Wallet not found"

        val error = RepositoryError.ResourceNotFound(
            operation = operation,
            resourceType = resourceType,
            resourceId = resourceId,
            userMessage = userMessage
        )

        assertEquals(operation, error.operation)
        assertEquals(resourceType, error.resourceType)
        assertEquals(resourceId, error.resourceId)
        assertEquals(userMessage, error.userMessage)
    }

    @Test
    fun `ResourceNotFound message should include resource details`() {
        val error = RepositoryError.ResourceNotFound(
            operation = "getTransactionById",
            resourceType = "Transaction",
            resourceId = "txn999",
            userMessage = "Transaction does not exist"
        )

        val expectedMessage = "Resource not found in getTransactionById: Transaction with ID txn999"
        assertEquals(expectedMessage, error.message)
    }

    // endregion

    // region UnknownError Tests

    @Test
    fun `UnknownError should contain all required fields`() {
        val operation = "performOperation"
        val userMessage = "An unexpected error occurred"
        val cause = Exception("Unknown exception")

        val error = RepositoryError.UnknownError(
            operation = operation,
            userMessage = userMessage,
            cause = cause
        )

        assertEquals(operation, error.operation)
        assertEquals(userMessage, error.userMessage)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `UnknownError message should be formatted correctly`() {
        val error = RepositoryError.UnknownError(
            operation = "unknownOp",
            userMessage = "Something went wrong",
            cause = null
        )

        val expectedMessage = "Unknown error in unknownOp: Something went wrong"
        assertEquals(expectedMessage, error.message)
    }

    // endregion

    // region CompositeError Tests

    @Test
    fun `CompositeError should aggregate multiple errors`() {
        val error1 = RepositoryError.FirestoreListener(
            operation = "query1",
            userMessage = "Error 1",
            shouldRetry = true,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        val error2 = RepositoryError.FirestoreListener(
            operation = "query2",
            userMessage = "Error 2",
            shouldRetry = false,
            requiresReauth = true,
            isOffline = true,
            cause = null
        )

        val composite = RepositoryError.CompositeError(
            operation = "getWalletTransactions",
            errors = listOf(error1, error2),
            userMessage = "Multiple queries failed"
        )

        assertEquals(2, composite.errors.size)
        assertEquals("getWalletTransactions", composite.operation)
        assertEquals("Multiple queries failed", composite.userMessage)
    }

    @Test
    fun `CompositeError requiresReauth should be true if any sub-error requires reauth`() {
        val error1 = RepositoryError.FirestoreListener(
            operation = "query1",
            userMessage = "Error 1",
            shouldRetry = false,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        val error2 = RepositoryError.AuthenticationError(
            operation = "query2",
            userMessage = "Auth error",
            requiresReauth = true,
            cause = null
        )

        val composite = RepositoryError.CompositeError(
            operation = "operation",
            errors = listOf(error1, error2),
            userMessage = "Errors occurred"
        )

        assertTrue(composite.requiresReauth)
    }

    @Test
    fun `CompositeError shouldRetry should be true if any sub-error should retry`() {
        val error1 = RepositoryError.FirestoreListener(
            operation = "query1",
            userMessage = "Error 1",
            shouldRetry = true,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        val error2 = RepositoryError.FirestoreListener(
            operation = "query2",
            userMessage = "Error 2",
            shouldRetry = false,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        val composite = RepositoryError.CompositeError(
            operation = "operation",
            errors = listOf(error1, error2),
            userMessage = "Errors occurred"
        )

        assertTrue(composite.shouldRetry)
    }

    @Test
    fun `CompositeError isOffline should be true if any sub-error is offline`() {
        val error1 = RepositoryError.NetworkError(
            operation = "query1",
            userMessage = "Network error",
            isTimeout = false,
            cause = null
        )

        val error2 = RepositoryError.FirestoreListener(
            operation = "query2",
            userMessage = "Error 2",
            shouldRetry = false,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        val composite = RepositoryError.CompositeError(
            operation = "operation",
            errors = listOf(error1, error2),
            userMessage = "Errors occurred"
        )

        assertTrue(composite.isOffline)
    }

    @Test
    fun `CompositeError message should indicate number of errors`() {
        val errors = listOf(
            RepositoryError.UnknownError("op1", "Error 1", null),
            RepositoryError.UnknownError("op2", "Error 2", null),
            RepositoryError.UnknownError("op3", "Error 3", null)
        )

        val composite = RepositoryError.CompositeError(
            operation = "multiQuery",
            errors = errors,
            userMessage = "Multiple failures"
        )

        val expectedMessage = "Composite error in multiQuery: 3 errors occurred"
        assertEquals(expectedMessage, composite.message)
    }

    // endregion

    // region ErrorInfo to RepositoryError Conversion Tests

    @Test
    fun `toRepositoryError should create NetworkError when isOffline is true`() {
        val errorInfo = FirestoreErrorHandler.ErrorInfo(
            userMessage = "You are offline",
            shouldRetry = true,
            requiresReauth = false
        )

        val cause = Exception("Network unavailable")
        val repositoryError = errorInfo.toRepositoryError(
            operation = "loadData",
            isOffline = true,
            cause = cause
        )

        assertTrue(repositoryError is RepositoryError.NetworkError)
        val networkError = repositoryError as RepositoryError.NetworkError
        assertEquals("loadData", networkError.operation)
        assertEquals("You are offline", networkError.userMessage)
        assertEquals(cause, networkError.cause)
    }

    @Test
    fun `toRepositoryError should create AuthenticationError when requiresReauth is true`() {
        val errorInfo = FirestoreErrorHandler.ErrorInfo(
            userMessage = "Authentication required",
            shouldRetry = false,
            requiresReauth = true
        )

        val cause = Exception("Token expired")
        val repositoryError = errorInfo.toRepositoryError(
            operation = "getUserData",
            isOffline = false,
            cause = cause
        )

        assertTrue(repositoryError is RepositoryError.AuthenticationError)
        val authError = repositoryError as RepositoryError.AuthenticationError
        assertEquals("getUserData", authError.operation)
        assertEquals("Authentication required", authError.userMessage)
        assertTrue(authError.requiresReauth)
        assertEquals(cause, authError.cause)
    }

    @Test
    fun `toRepositoryError should create FirestoreListener for standard errors`() {
        val errorInfo = FirestoreErrorHandler.ErrorInfo(
            userMessage = "Operation failed",
            shouldRetry = true,
            requiresReauth = false
        )

        val cause = Exception("Firestore error")
        val repositoryError = errorInfo.toRepositoryError(
            operation = "getTransactions",
            isOffline = false,
            cause = cause
        )

        assertTrue(repositoryError is RepositoryError.FirestoreListener)
        val listenerError = repositoryError as RepositoryError.FirestoreListener
        assertEquals("getTransactions", listenerError.operation)
        assertEquals("Operation failed", listenerError.userMessage)
        assertTrue(listenerError.shouldRetry)
        assertFalse(listenerError.requiresReauth)
        assertFalse(listenerError.isOffline)
        assertEquals(cause, listenerError.cause)
    }

    @Test
    fun `toRepositoryError should preserve all ErrorInfo fields and isOffline parameter`() {
        val errorInfo = FirestoreErrorHandler.ErrorInfo(
            userMessage = "Complex error",
            shouldRetry = true,
            requiresReauth = false
        )

        val repositoryError = errorInfo.toRepositoryError(
            operation = "complexOperation",
            isOffline = false,
            cause = null
        )

        val listenerError = repositoryError as RepositoryError.FirestoreListener
        assertEquals(errorInfo.userMessage, listenerError.userMessage)
        assertEquals(errorInfo.shouldRetry, listenerError.shouldRetry)
        assertEquals(errorInfo.requiresReauth, listenerError.requiresReauth)
        assertFalse(listenerError.isOffline)
    }

    // endregion

    // region Exception Behavior Tests

    @Test
    fun `RepositoryError should be throwable as Exception`() {
        val error = RepositoryError.FirestoreListener(
            operation = "test",
            userMessage = "Test error",
            shouldRetry = false,
            requiresReauth = false,
            isOffline = false,
            cause = null
        )

        assertTrue(error is Exception)
        assertTrue(error is Throwable)
    }

    @Test
    fun `RepositoryError should allow try-catch as Exception`() {
        val error = RepositoryError.UnknownError(
            operation = "test",
            userMessage = "Test",
            cause = null
        )

        var caught = false
        try {
            throw error
        } catch (e: Exception) {
            caught = true
            assertTrue(e is RepositoryError)
        }

        assertTrue(caught)
    }

    // endregion
}

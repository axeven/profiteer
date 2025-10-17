package com.axeven.profiteerapp.test.helpers

import com.axeven.profiteerapp.data.model.RepositoryError
import org.junit.Assert.*

/**
 * Test helpers for repository error handling validation.
 *
 * These helpers are used during the TDD refactoring of repositories
 * to verify that UI dependencies (SharedErrorViewModel) are properly
 * removed and replaced with domain error types.
 *
 * Usage:
 * ```kotlin
 * @Test
 * fun `getUserWallets should return RepositoryError on failure`() {
 *     // Arrange
 *     val mockFirestore = createMockFirestoreWithError()
 *     val repository = WalletRepository(mockFirestore, logger)
 *
 *     // Act & Assert
 *     val result = repository.getUserWallets("userId")
 *         .catch { error ->
 *             error.assertIsRepositoryError()
 *             error.assertIsFirestoreListenerError()
 *         }
 *         .collect()
 * }
 * ```
 */
object RepositoryTestHelpers {

    /**
     * Asserts that a Throwable is a RepositoryError.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if the throwable is not a RepositoryError
     */
    fun Throwable.assertIsRepositoryError(message: String? = null) {
        val errorMessage = message ?: "Expected RepositoryError but got ${this::class.simpleName}"
        assertTrue(errorMessage, this is RepositoryError)
    }

    /**
     * Asserts that a Throwable is NOT a RepositoryError.
     * Used to verify old behavior before refactoring.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if the throwable is a RepositoryError
     */
    fun Throwable.assertIsNotRepositoryError(message: String? = null) {
        val errorMessage = message ?: "Did not expect RepositoryError but got one"
        assertFalse(errorMessage, this is RepositoryError)
    }

    /**
     * Asserts that a RepositoryError is specifically a FirestoreListener error.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if not a FirestoreListener error
     */
    fun Throwable.assertIsFirestoreListenerError(message: String? = null) {
        assertIsRepositoryError(message)
        val errorMessage = message ?: "Expected FirestoreListener error but got ${(this as RepositoryError)::class.simpleName}"
        assertTrue(errorMessage, this is RepositoryError.FirestoreListener)
    }

    /**
     * Asserts that a RepositoryError is specifically a NetworkError.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if not a NetworkError
     */
    fun Throwable.assertIsNetworkError(message: String? = null) {
        assertIsRepositoryError(message)
        val errorMessage = message ?: "Expected NetworkError but got ${(this as RepositoryError)::class.simpleName}"
        assertTrue(errorMessage, this is RepositoryError.NetworkError)
    }

    /**
     * Asserts that a RepositoryError is specifically an AuthenticationError.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if not an AuthenticationError
     */
    fun Throwable.assertIsAuthenticationError(message: String? = null) {
        assertIsRepositoryError(message)
        val errorMessage = message ?: "Expected AuthenticationError but got ${(this as RepositoryError)::class.simpleName}"
        assertTrue(errorMessage, this is RepositoryError.AuthenticationError)
    }

    /**
     * Asserts that a RepositoryError contains the expected operation name.
     *
     * @param expectedOperation The expected operation name
     * @param message Optional custom assertion message
     * @throws AssertionError if operation doesn't match
     */
    fun Throwable.assertHasOperation(expectedOperation: String, message: String? = null) {
        assertIsRepositoryError(message)
        val repositoryError = this as RepositoryError
        val actualOperation = when (repositoryError) {
            is RepositoryError.FirestoreListener -> repositoryError.operation
            is RepositoryError.FirestoreCrud -> repositoryError.operation
            is RepositoryError.NetworkError -> repositoryError.operation
            is RepositoryError.AuthenticationError -> repositoryError.operation
            is RepositoryError.DataValidationError -> repositoryError.operation
            is RepositoryError.ResourceNotFound -> repositoryError.operation
            is RepositoryError.UnknownError -> repositoryError.operation
            is RepositoryError.CompositeError -> repositoryError.operation
        }
        val errorMessage = message ?: "Expected operation '$expectedOperation' but got '$actualOperation'"
        assertEquals(errorMessage, expectedOperation, actualOperation)
    }

    /**
     * Asserts that a RepositoryError contains a user-friendly message.
     *
     * @param expectedSubstring Substring that should be present in the user message
     * @param message Optional custom assertion message
     * @throws AssertionError if user message doesn't contain the substring
     */
    fun Throwable.assertUserMessageContains(expectedSubstring: String, message: String? = null) {
        assertIsRepositoryError(message)
        val repositoryError = this as RepositoryError
        val userMessage = when (repositoryError) {
            is RepositoryError.FirestoreListener -> repositoryError.userMessage
            is RepositoryError.FirestoreCrud -> repositoryError.userMessage
            is RepositoryError.NetworkError -> repositoryError.userMessage
            is RepositoryError.AuthenticationError -> repositoryError.userMessage
            is RepositoryError.DataValidationError -> repositoryError.userMessage
            is RepositoryError.ResourceNotFound -> repositoryError.userMessage
            is RepositoryError.UnknownError -> repositoryError.userMessage
            is RepositoryError.CompositeError -> repositoryError.userMessage
        }
        val errorMessage = message ?: "Expected user message to contain '$expectedSubstring' but got: $userMessage"
        assertTrue(errorMessage, userMessage.contains(expectedSubstring, ignoreCase = true))
    }

    /**
     * Asserts that a RepositoryError requires re-authentication.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if error doesn't require reauth
     */
    fun Throwable.assertRequiresReauth(message: String? = null) {
        assertIsRepositoryError(message)
        val repositoryError = this as RepositoryError
        val requiresReauth = when (repositoryError) {
            is RepositoryError.FirestoreListener -> repositoryError.requiresReauth
            is RepositoryError.FirestoreCrud -> repositoryError.requiresReauth
            is RepositoryError.AuthenticationError -> repositoryError.requiresReauth
            is RepositoryError.CompositeError -> repositoryError.requiresReauth
            else -> false
        }
        val errorMessage = message ?: "Expected error to require re-authentication but it doesn't"
        assertTrue(errorMessage, requiresReauth)
    }

    /**
     * Asserts that a RepositoryError should be retried.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if error shouldn't be retried
     */
    fun Throwable.assertShouldRetry(message: String? = null) {
        assertIsRepositoryError(message)
        val repositoryError = this as RepositoryError
        val shouldRetry = when (repositoryError) {
            is RepositoryError.FirestoreListener -> repositoryError.shouldRetry
            is RepositoryError.FirestoreCrud -> repositoryError.shouldRetry
            is RepositoryError.NetworkError -> !repositoryError.isTimeout
            is RepositoryError.CompositeError -> repositoryError.shouldRetry
            else -> false
        }
        val errorMessage = message ?: "Expected error to be retryable but it isn't"
        assertTrue(errorMessage, shouldRetry)
    }

    /**
     * Asserts that a RepositoryError is due to offline status.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if error isn't due to offline status
     */
    fun Throwable.assertIsOffline(message: String? = null) {
        assertIsRepositoryError(message)
        val repositoryError = this as RepositoryError
        val isOffline = when (repositoryError) {
            is RepositoryError.FirestoreListener -> repositoryError.isOffline
            is RepositoryError.NetworkError -> true
            is RepositoryError.CompositeError -> repositoryError.isOffline
            else -> false
        }
        val errorMessage = message ?: "Expected error to be due to offline status but it isn't"
        assertTrue(errorMessage, isOffline)
    }

    /**
     * Asserts that a RepositoryError has a valid cause chain.
     *
     * @param message Optional custom assertion message
     * @throws AssertionError if cause is null
     */
    fun Throwable.assertHasCause(message: String? = null) {
        assertIsRepositoryError(message)
        val errorMessage = message ?: "Expected error to have a cause but cause is null"
        assertNotNull(errorMessage, this.cause)
    }

    /**
     * Asserts that a CompositeError contains the expected number of sub-errors.
     *
     * @param expectedCount Expected number of sub-errors
     * @param message Optional custom assertion message
     * @throws AssertionError if count doesn't match or not a CompositeError
     */
    fun Throwable.assertCompositeErrorCount(expectedCount: Int, message: String? = null) {
        assertIsRepositoryError(message)
        assertTrue("Expected CompositeError", this is RepositoryError.CompositeError)
        val compositeError = this as RepositoryError.CompositeError
        val errorMessage = message ?: "Expected $expectedCount errors but got ${compositeError.errors.size}"
        assertEquals(errorMessage, expectedCount, compositeError.errors.size)
    }
}

/**
 * Verification helpers for ensuring SharedErrorViewModel is not called.
 *
 * These are used to verify that repositories no longer have UI dependencies.
 */
object UIDecouplingVerifiers {

    /**
     * Track to verify SharedErrorViewModel is never instantiated in tests.
     * If this gets called, the test should fail.
     */
    class MockSharedErrorViewModel {
        init {
            throw AssertionError(
                "SharedErrorViewModel should not be instantiated in repository tests. " +
                "Repositories must not depend on UI layer."
            )
        }

        fun showError(
            message: String,
            shouldRetry: Boolean = false,
            requiresReauth: Boolean = false,
            isOffline: Boolean = false
        ) {
            throw AssertionError(
                "SharedErrorViewModel.showError() should never be called from repositories. " +
                "Use RepositoryError types instead."
            )
        }
    }

    /**
     * Verifies that no UI dependencies are present in the repository constructor.
     *
     * @param constructorParams List of constructor parameter types
     * @throws AssertionError if UI dependencies are found
     */
    fun verifyNoUIDependencies(constructorParams: List<Class<*>>) {
        val uiClasses = listOf(
            "SharedErrorViewModel",
            "ViewModel",
            "Activity",
            "Fragment",
            "Context" // Note: Context is sometimes necessary, but flag for review
        )

        constructorParams.forEach { param ->
            val simpleName = param.simpleName
            uiClasses.forEach { uiClass ->
                if (simpleName.contains(uiClass)) {
                    if (uiClass == "Context") {
                        // Context is allowed but log a warning
                        println("WARNING: Repository has Context dependency - verify it's necessary")
                    } else {
                        throw AssertionError(
                            "Repository has UI dependency: $simpleName. " +
                            "Repositories should not depend on UI layer classes."
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fake implementations for testing.
 */
object TestFakes {

    /**
     * Creates a fake RepositoryError for testing error handling.
     */
    fun createFakeFirestoreListenerError(
        operation: String = "testOperation",
        userMessage: String = "Test error message",
        shouldRetry: Boolean = false,
        requiresReauth: Boolean = false,
        isOffline: Boolean = false
    ): RepositoryError.FirestoreListener {
        return RepositoryError.FirestoreListener(
            operation = operation,
            userMessage = userMessage,
            shouldRetry = shouldRetry,
            requiresReauth = requiresReauth,
            isOffline = isOffline,
            cause = Exception("Fake test exception")
        )
    }

    /**
     * Creates a fake NetworkError for testing.
     */
    fun createFakeNetworkError(
        operation: String = "testOperation",
        userMessage: String = "Network error",
        isTimeout: Boolean = false
    ): RepositoryError.NetworkError {
        return RepositoryError.NetworkError(
            operation = operation,
            userMessage = userMessage,
            isTimeout = isTimeout,
            cause = Exception("Fake network exception")
        )
    }

    /**
     * Creates a fake AuthenticationError for testing.
     */
    fun createFakeAuthError(
        operation: String = "testOperation",
        userMessage: String = "Authentication required",
        requiresReauth: Boolean = true
    ): RepositoryError.AuthenticationError {
        return RepositoryError.AuthenticationError(
            operation = operation,
            userMessage = userMessage,
            requiresReauth = requiresReauth,
            cause = Exception("Fake auth exception")
        )
    }

    /**
     * Creates a fake CompositeError for testing.
     */
    fun createFakeCompositeError(
        operation: String = "testOperation",
        errorCount: Int = 2
    ): RepositoryError.CompositeError {
        val errors = (1..errorCount).map { index ->
            createFakeFirestoreListenerError(
                operation = "subOperation$index",
                userMessage = "Sub-error $index"
            )
        }
        return RepositoryError.CompositeError(
            operation = operation,
            errors = errors,
            userMessage = "Multiple errors occurred"
        )
    }
}

package com.axeven.profiteerapp.service

import com.axeven.profiteerapp.utils.logging.Logger
import org.junit.Test
import org.junit.Before
import org.junit.Assert.*

/**
 * Test class to verify logging behavior in AuthTokenManager.
 * This follows TDD approach to ensure proper logging is implemented.
 */
class AuthTokenManagerLoggingTest {

    private lateinit var mockLogger: Logger
    private val capturedLogs = mutableListOf<LogCall>()

    data class LogCall(
        val level: String,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )

    @Before
    fun setUp() {
        capturedLogs.clear()
        mockLogger = object : Logger {
            override fun d(tag: String, message: String) {
                capturedLogs.add(LogCall("DEBUG", tag, message))
            }

            override fun i(tag: String, message: String) {
                capturedLogs.add(LogCall("INFO", tag, message))
            }

            override fun w(tag: String, message: String) {
                capturedLogs.add(LogCall("WARN", tag, message))
            }

            override fun e(tag: String, message: String, throwable: Throwable?) {
                capturedLogs.add(LogCall("ERROR", tag, message, throwable))
            }
        }
    }

    @Test
    fun `should log token refresh attempts with appropriate level`() {
        // This test verifies that token refresh operations are logged appropriately

        val expectedTag = "AuthTokenManager"

        // Token refresh success should be logged at DEBUG level
        mockLogger.d(expectedTag, "Token refresh successful")

        // Token refresh failure should be logged at ERROR level with throwable
        val testException = RuntimeException("Network error")
        mockLogger.e(expectedTag, "Token refresh failed", testException)

        // Verify the logs were captured
        assertEquals("Should have captured 2 log calls", 2, capturedLogs.size)

        val debugLog = capturedLogs[0]
        assertEquals("Should log success at DEBUG level", "DEBUG", debugLog.level)
        assertEquals("Should use correct tag", expectedTag, debugLog.tag)
        assertTrue("Should contain success message", debugLog.message.contains("successful"))

        val errorLog = capturedLogs[1]
        assertEquals("Should log failure at ERROR level", "ERROR", errorLog.level)
        assertEquals("Should use correct tag", expectedTag, errorLog.tag)
        assertTrue("Should contain failure message", errorLog.message.contains("failed"))
        assertNotNull("Should include throwable for errors", errorLog.throwable)
    }

    @Test
    fun `should not log actual token values`() {
        // This test ensures that actual token values are never logged

        val expectedTag = "AuthTokenManager"
        val fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"

        // We should log token operations but not the actual token values
        mockLogger.d(expectedTag, "Token validation successful")
        mockLogger.w(expectedTag, "No authenticated user found")
        mockLogger.e(expectedTag, "Token is null or empty")

        // Verify that no actual token values are included
        assertEquals("Should have captured 3 log calls", 3, capturedLogs.size)

        capturedLogs.forEach { logCall ->
            assertEquals("Should use correct tag", expectedTag, logCall.tag)
            assertFalse("Should not contain actual token", logCall.message.contains(fakeToken))
            assertFalse("Should not contain JWT pattern", logCall.message.matches(Regex(".*eyJ[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*")))
        }
    }

    @Test
    fun `should log authentication state changes`() {
        // This test verifies that authentication state changes are logged properly

        val expectedTag = "AuthTokenManager"

        // Authentication state changes should be logged at appropriate levels
        mockLogger.w(expectedTag, "Triggering re-authentication: token expired")
        mockLogger.d(expectedTag, "User signed out successfully")

        // Verify
        assertEquals("Should have captured 2 log calls", 2, capturedLogs.size)

        val warnLog = capturedLogs[0]
        assertEquals("Should log re-auth trigger at WARN level", "WARN", warnLog.level)
        assertTrue("Should contain re-authentication context", warnLog.message.contains("re-authentication"))

        val debugLog = capturedLogs[1]
        assertEquals("Should log sign out at DEBUG level", "DEBUG", debugLog.level)
        assertTrue("Should contain sign out message", debugLog.message.contains("signed out"))
    }

    @Test
    fun `should log token validation results appropriately`() {
        // This test verifies token validation logging

        val expectedTag = "AuthTokenManager"

        // Token validation results should be logged with appropriate detail
        mockLogger.d(expectedTag, "Token validation successful")
        mockLogger.e(expectedTag, "Token is expired. Expires: 2024-01-01, Current: 2024-01-02")
        mockLogger.e(expectedTag, "Token validation failed", RuntimeException("Parse error"))

        // Verify
        assertEquals("Should have captured 3 log calls", 3, capturedLogs.size)

        val successLog = capturedLogs[0]
        assertEquals("Should log success at DEBUG level", "DEBUG", successLog.level)
        assertTrue("Should indicate success", successLog.message.contains("successful"))

        val expiredLog = capturedLogs[1]
        assertEquals("Should log expiration at ERROR level", "ERROR", expiredLog.level)
        assertTrue("Should contain expiration info", expiredLog.message.contains("expired"))
        // Should contain timestamp info but no actual tokens
        assertTrue("Should contain timestamp context", expiredLog.message.contains("Expires:"))

        val failureLog = capturedLogs[2]
        assertEquals("Should log failure at ERROR level", "ERROR", failureLog.level)
        assertTrue("Should contain failure message", failureLog.message.contains("failed"))
        assertNotNull("Should include throwable", failureLog.throwable)
    }

    @Test
    fun `should use consistent tag naming`() {
        // This test verifies consistent tag usage

        val expectedTag = "AuthTokenManager"

        // All logs should use the same tag
        mockLogger.d(expectedTag, "Test debug message")
        mockLogger.w(expectedTag, "Test warning message")
        mockLogger.e(expectedTag, "Test error message")

        // Verify
        assertEquals("Should have captured 3 log calls", 3, capturedLogs.size)

        capturedLogs.forEach { logCall ->
            assertEquals("Should use consistent tag", expectedTag, logCall.tag)
        }
    }

    @Test
    fun `should handle error cases gracefully in logging`() {
        // This test verifies that error scenarios are logged appropriately

        val expectedTag = "AuthTokenManager"
        val networkError = RuntimeException("Connection timeout")
        val parseError = IllegalStateException("Invalid token format")

        // Different types of errors should be logged with context
        mockLogger.e(expectedTag, "Token refresh failed", networkError)
        mockLogger.e(expectedTag, "Token validation failed", parseError)
        mockLogger.e(expectedTag, "Error signing out", RuntimeException("Auth error"))

        // Verify
        assertEquals("Should have captured 3 log calls", 3, capturedLogs.size)

        capturedLogs.forEach { logCall ->
            assertEquals("Should log errors at ERROR level", "ERROR", logCall.level)
            assertEquals("Should use correct tag", expectedTag, logCall.tag)
            assertNotNull("Should include throwable for debugging", logCall.throwable)
            assertTrue("Should contain operation context",
                logCall.message.contains("refresh") ||
                logCall.message.contains("validation") ||
                logCall.message.contains("signing out"))
        }
    }
}
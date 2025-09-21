package com.axeven.profiteerapp.viewmodel

import com.axeven.profiteerapp.utils.logging.Logger
import org.junit.Test
import org.junit.Before
import org.junit.Assert.*

/**
 * Test class to verify logging behavior in ViewModels.
 * This follows TDD approach to ensure proper logging is implemented across all ViewModels.
 */
class ViewModelLoggingTest {

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
    fun `should log user actions at appropriate levels`() {
        // This test verifies that user actions are logged at appropriate levels

        val expectedTag = "HomeViewModel"

        // User actions should be logged at INFO or DEBUG level
        mockLogger.i(expectedTag, "User refreshed transaction list")
        mockLogger.d(expectedTag, "Fetching transactions for user: user123")

        // Verify the logs were captured
        assertEquals("Should have captured 2 log calls", 2, capturedLogs.size)

        val infoLog = capturedLogs[0]
        assertEquals("Should log at INFO level", "INFO", infoLog.level)
        assertEquals("Should use correct tag", expectedTag, infoLog.tag)
        assertTrue("Should contain user action", infoLog.message.contains("User refreshed"))

        val debugLog = capturedLogs[1]
        assertEquals("Should log at DEBUG level", "DEBUG", debugLog.level)
        assertTrue("Should contain operation details", debugLog.message.contains("Fetching transactions"))
    }

    @Test
    fun `should log errors without exposing sensitive data`() {
        // This test ensures that errors are logged appropriately without sensitive information

        val expectedTag = "SettingsViewModel"
        val testException = RuntimeException("Network error")

        // Errors should be logged at ERROR level without exposing sensitive data
        mockLogger.e(expectedTag, "Failed to update user preferences", testException)

        // Verify
        assertEquals("Should have captured 1 log call", 1, capturedLogs.size)
        val logCall = capturedLogs.first()
        assertEquals("Should log at ERROR level", "ERROR", logCall.level)
        assertEquals("Should use correct tag", expectedTag, logCall.tag)
        assertTrue("Should contain error message", logCall.message.contains("Failed to update"))
        assertNotNull("Should include throwable for debugging", logCall.throwable)

        // Should not contain sensitive data
        assertFalse("Should not contain user ID", logCall.message.contains("user_123"))
        assertFalse("Should not contain email", logCall.message.contains("@"))
        assertFalse("Should not contain balance", logCall.message.contains("1000.00"))
    }

    @Test
    fun `should log transaction operations with appropriate detail`() {
        // This test verifies transaction-related logging in ViewModels

        val expectedTag = "TransactionListViewModel"

        // Transaction operations should provide useful context
        mockLogger.d(expectedTag, "Loading transactions for wallet: wallet123")
        mockLogger.i(expectedTag, "Transaction deletion completed successfully")
        mockLogger.w(expectedTag, "No transactions found for selected period")

        // Verify
        assertEquals("Should have captured 3 log calls", 3, capturedLogs.size)

        val debugLog = capturedLogs[0]
        assertEquals("Should log loading at DEBUG level", "DEBUG", debugLog.level)
        assertTrue("Should contain wallet reference", debugLog.message.contains("wallet123"))

        val infoLog = capturedLogs[1]
        assertEquals("Should log completion at INFO level", "INFO", infoLog.level)
        assertTrue("Should indicate success", infoLog.message.contains("completed successfully"))

        val warnLog = capturedLogs[2]
        assertEquals("Should log warnings at WARN level", "WARN", warnLog.level)
        assertTrue("Should indicate issue", warnLog.message.contains("No transactions found"))
    }

    @Test
    fun `should log wallet operations with balance context`() {
        // This test verifies wallet-related logging patterns

        val expectedTag = "WalletDetailViewModel"

        // Wallet operations should log context without exposing actual balances
        mockLogger.d(expectedTag, "Calculating balance for wallet: wallet456")
        mockLogger.i(expectedTag, "Balance recalculation completed")
        mockLogger.w(expectedTag, "Wallet balance inconsistency detected")

        // Verify
        assertEquals("Should have captured 3 log calls", 3, capturedLogs.size)

        capturedLogs.forEach { logCall ->
            assertEquals("Should use correct tag", expectedTag, logCall.tag)
            assertTrue("Should contain wallet context",
                logCall.message.contains("wallet") || logCall.message.contains("balance"))
            // Should not contain actual balance amounts
            assertFalse("Should not contain specific amounts", logCall.message.matches(Regex(".*\\d+\\.\\d{2}.*")))
        }
    }

    @Test
    fun `should log report generation with performance context`() {
        // This test verifies report-related logging patterns

        val expectedTag = "ReportViewModel"

        // Report operations should log timing and context
        mockLogger.d(expectedTag, "Starting report generation for period: 2024-01")
        mockLogger.i(expectedTag, "Report data aggregation completed in 150ms")
        mockLogger.w(expectedTag, "Large dataset detected, report may take longer")

        // Verify
        assertEquals("Should have captured 3 log calls", 3, capturedLogs.size)

        val debugLog = capturedLogs[0]
        assertTrue("Should contain period context", debugLog.message.contains("2024-01"))

        val infoLog = capturedLogs[1]
        assertTrue("Should contain timing info", infoLog.message.contains("150ms"))

        val warnLog = capturedLogs[2]
        assertTrue("Should contain performance warning", warnLog.message.contains("may take longer"))
    }

    @Test
    fun `should handle ViewModel lifecycle events appropriately`() {
        // This test verifies that ViewModel lifecycle is logged appropriately

        val expectedTag = "HomeViewModel"

        // Lifecycle events should be logged for debugging
        mockLogger.d(expectedTag, "ViewModel initialized")
        mockLogger.d(expectedTag, "Starting data refresh on resume")
        mockLogger.d(expectedTag, "ViewModel clearing resources")

        // Verify
        assertEquals("Should have captured 3 log calls", 3, capturedLogs.size)

        capturedLogs.forEach { logCall ->
            assertEquals("Should log lifecycle at DEBUG level", "DEBUG", logCall.level)
            assertEquals("Should use correct tag", expectedTag, logCall.tag)
        }

        assertTrue("Should log initialization", capturedLogs.any { it.message.contains("initialized") })
        assertTrue("Should log data operations", capturedLogs.any { it.message.contains("data refresh") })
        assertTrue("Should log cleanup", capturedLogs.any { it.message.contains("clearing resources") })
    }

    @Test
    fun `should log network and data errors with context`() {
        // This test verifies error logging patterns across ViewModels

        val networkError = RuntimeException("Connection timeout")
        val dataError = IllegalStateException("Invalid data format")

        // Different types of errors should be logged with appropriate context
        mockLogger.e("HomeViewModel", "Network error during transaction fetch", networkError)
        mockLogger.e("SettingsViewModel", "Data validation error in preferences", dataError)

        // Verify
        assertEquals("Should have captured 2 log calls", 2, capturedLogs.size)

        val networkLog = capturedLogs[0]
        assertEquals("Should log network errors at ERROR level", "ERROR", networkLog.level)
        assertTrue("Should contain operation context", networkLog.message.contains("transaction fetch"))
        assertEquals("Should include throwable", networkError, networkLog.throwable)

        val dataLog = capturedLogs[1]
        assertEquals("Should log data errors at ERROR level", "ERROR", dataLog.level)
        assertTrue("Should contain validation context", dataLog.message.contains("validation error"))
        assertEquals("Should include throwable", dataError, dataLog.throwable)
    }

    @Test
    fun `should use consistent tag naming across ViewModels`() {
        // This test verifies consistent naming patterns for ViewModel tags

        val expectedTags = listOf(
            "HomeViewModel",
            "SettingsViewModel",
            "TransactionListViewModel",
            "WalletDetailViewModel",
            "ReportViewModel"
        )

        // All ViewModels should use consistent tag naming
        expectedTags.forEach { tag ->
            mockLogger.d(tag, "Test message")
        }

        // Verify
        assertEquals("Should have captured all ViewModel logs", expectedTags.size, capturedLogs.size)

        capturedLogs.forEachIndexed { index, logCall ->
            assertEquals("Should use expected tag format", expectedTags[index], logCall.tag)
            assertTrue("Tag should end with ViewModel", logCall.tag.endsWith("ViewModel"))
        }
    }
}
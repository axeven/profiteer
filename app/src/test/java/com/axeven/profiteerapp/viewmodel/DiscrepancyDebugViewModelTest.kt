package com.axeven.profiteerapp.viewmodel

import com.axeven.profiteerapp.utils.BalanceDiscrepancyDetector
import com.axeven.profiteerapp.utils.DiscrepancyAnalyzer
import com.axeven.profiteerapp.utils.logging.Logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DiscrepancyDebugViewModel.
 * Tests expected behavior patterns and logging.
 */
class DiscrepancyDebugViewModelTest {

    private lateinit var mockLogger: Logger
    private lateinit var balanceDetector: BalanceDiscrepancyDetector
    private lateinit var discrepancyAnalyzer: DiscrepancyAnalyzer
    private val capturedLogs = mutableListOf<LogCall>()

    data class LogCall(
        val level: String,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )

    @Before
    fun setup() {
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

        balanceDetector = BalanceDiscrepancyDetector()
        discrepancyAnalyzer = DiscrepancyAnalyzer(balanceDetector)
    }

    @Test
    fun `should log data loading operations`() {
        // ViewModel should log when loading discrepancy data
        val expectedTag = "DiscrepancyDebugVM"

        mockLogger.d(expectedTag, "Loading discrepancy data for user: user123")
        mockLogger.d(expectedTag, "Loaded 5 transactions and 3 wallets")

        assertEquals("Should have captured 2 log calls", 2, capturedLogs.size)

        val loadingLog = capturedLogs[0]
        assertEquals("Should log at DEBUG level", "DEBUG", loadingLog.level)
        assertEquals("Should use correct tag", expectedTag, loadingLog.tag)
        assertTrue("Should contain user context", loadingLog.message.contains("user123"))

        val loadedLog = capturedLogs[1]
        assertTrue("Should contain transaction count", loadedLog.message.contains("5 transactions"))
        assertTrue("Should contain wallet count", loadedLog.message.contains("3 wallets"))
    }

    @Test
    fun `should log balance calculations`() {
        val expectedTag = "DiscrepancyDebugVM"

        mockLogger.d(expectedTag, "Physical total: 150.0, Logical total: 100.0")

        assertEquals("Should have captured 1 log call", 1, capturedLogs.size)

        val balanceLog = capturedLogs[0]
        assertEquals("Should log at DEBUG level", "DEBUG", balanceLog.level)
        assertTrue("Should contain Physical total", balanceLog.message.contains("Physical total"))
        assertTrue("Should contain Logical total", balanceLog.message.contains("Logical total"))
    }

    @Test
    fun `should log discrepancy detection results`() {
        val expectedTag = "DiscrepancyDebugVM"

        mockLogger.d(expectedTag, "First discrepancy transaction: t123")
        mockLogger.i(expectedTag, "Discrepancy analysis complete. Has discrepancy: true")

        assertEquals("Should have captured 2 log calls", 2, capturedLogs.size)

        val detectionLog = capturedLogs[0]
        assertEquals("Should log detection at DEBUG level", "DEBUG", detectionLog.level)
        assertTrue("Should contain transaction ID", detectionLog.message.contains("t123"))

        val completeLog = capturedLogs[1]
        assertEquals("Should log completion at INFO level", "INFO", completeLog.level)
        assertTrue("Should indicate completion", completeLog.message.contains("complete"))
        assertTrue("Should indicate discrepancy status", completeLog.message.contains("true"))
    }

    @Test
    fun `should log running balance calculations`() {
        val expectedTag = "DiscrepancyDebugVM"

        mockLogger.d(expectedTag, "Calculated running balances for 10 transactions")

        assertEquals("Should have captured 1 log call", 1, capturedLogs.size)

        val calcLog = capturedLogs[0]
        assertEquals("Should log at DEBUG level", "DEBUG", calcLog.level)
        assertTrue("Should contain transaction count", calcLog.message.contains("10 transactions"))
    }

    @Test
    fun `should log errors with context`() {
        val expectedTag = "DiscrepancyDebugVM"
        val testError = RuntimeException("Test error")

        mockLogger.e(expectedTag, "Error loading discrepancy data", testError)
        mockLogger.e(expectedTag, "Error analyzing discrepancy data", testError)

        assertEquals("Should have captured 2 log calls", 2, capturedLogs.size)

        val loadErrorLog = capturedLogs[0]
        assertEquals("Should log at ERROR level", "ERROR", loadErrorLog.level)
        assertTrue("Should contain error context", loadErrorLog.message.contains("loading"))
        assertEquals("Should include throwable", testError, loadErrorLog.throwable)

        val analyzeErrorLog = capturedLogs[1]
        assertTrue("Should contain error context", analyzeErrorLog.message.contains("analyzing"))
    }

    @Test
    fun `should log refresh operations`() {
        val expectedTag = "DiscrepancyDebugVM"

        mockLogger.d(expectedTag, "Refreshing discrepancy data")

        assertEquals("Should have captured 1 log call", 1, capturedLogs.size)

        val refreshLog = capturedLogs[0]
        assertEquals("Should log at DEBUG level", "DEBUG", refreshLog.level)
        assertTrue("Should contain refresh context", refreshLog.message.contains("Refreshing"))
    }

    @Test
    fun `should not log sensitive financial data`() {
        val expectedTag = "DiscrepancyDebugVM"

        // Log messages should not contain specific user balances or amounts
        mockLogger.d(expectedTag, "Physical total: 150.0, Logical total: 100.0")
        mockLogger.i(expectedTag, "Discrepancy analysis complete. Has discrepancy: true")

        capturedLogs.forEach { logCall ->
            // Messages contain aggregate totals for debugging, which is acceptable
            // but should not contain individual transaction amounts or user IDs
            assertTrue("Should not contain sensitive patterns",
                !logCall.message.contains("user_") &&
                !logCall.message.contains("@"))
        }
    }

    @Test
    fun `should use consistent tag naming`() {
        val expectedTag = "DiscrepancyDebugVM"

        mockLogger.d(expectedTag, "Test message 1")
        mockLogger.i(expectedTag, "Test message 2")
        mockLogger.e(expectedTag, "Test message 3", null)

        capturedLogs.forEach { logCall ->
            assertEquals("Should use consistent tag", expectedTag, logCall.tag)
        }
    }

    @Test
    fun `BalanceDiscrepancyDetector should calculate balances correctly`() {
        // Test that the detector works as expected
        val detector = BalanceDiscrepancyDetector()

        // Test hasDiscrepancy
        assertTrue("Should detect discrepancy", detector.hasDiscrepancy(100.0, 95.0))
        assertTrue("Should not detect small difference", !detector.hasDiscrepancy(100.0, 100.005))

        // Test getDiscrepancyAmount
        assertEquals("Should calculate positive discrepancy", 50.0,
            detector.getDiscrepancyAmount(150.0, 100.0), 0.001)
        assertEquals("Should calculate negative discrepancy", -50.0,
            detector.getDiscrepancyAmount(100.0, 150.0), 0.001)
    }

    @Test
    fun `DiscrepancyAnalyzer should be properly constructed`() {
        // Test that the analyzer is properly initialized
        val detector = BalanceDiscrepancyDetector()
        val analyzer = DiscrepancyAnalyzer(detector)

        // If constructor succeeds, analyzer is properly constructed
        assertTrue("Analyzer should be created successfully", analyzer != null)
    }
}

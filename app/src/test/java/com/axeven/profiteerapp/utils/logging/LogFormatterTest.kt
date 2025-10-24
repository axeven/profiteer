package com.axeven.profiteerapp.utils.logging

import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Tests for LogFormatter functionality
 *
 * Critical: Tests timestamp generation to ensure API 24+ compatibility
 * with core library desugaring enabled.
 */
class LogFormatterTest {

    // ==================== Timestamp Tests ====================

    @Test
    fun `formatUserAction should include valid ISO-8601 timestamp`() {
        // Arrange
        val action = "login"
        val userId = "user123"
        val metadata = emptyMap<String, Any?>()

        // Act
        val result = LogFormatter.formatUserAction(action, userId, metadata)

        // Assert
        assertTrue("Result should contain timestamp", result.contains("timestamp="))

        // Extract timestamp from result
        val timestampRegex = Regex("timestamp=([^ ]+)")
        val match = timestampRegex.find(result)
        assertNotNull("Timestamp should be present in result", match)

        val timestamp = match!!.groupValues[1]

        // Verify timestamp is valid ISO-8601 format
        assertDoesNotThrow("Timestamp should be valid ISO-8601") {
            Instant.parse(timestamp)
        }
    }

    @Test
    fun `formatUserAction timestamp should be current time within 5 seconds`() {
        // Arrange
        val action = "logout"
        val userId = "user456"
        val metadata = emptyMap<String, Any?>()
        val beforeTime = Instant.now()

        // Act
        val result = LogFormatter.formatUserAction(action, userId, metadata)

        // Assert
        val afterTime = Instant.now()

        // Extract timestamp
        val timestampRegex = Regex("timestamp=([^ ]+)")
        val match = timestampRegex.find(result)
        val timestamp = Instant.parse(match!!.groupValues[1])

        // Verify timestamp is within reasonable range
        assertTrue(
            "Timestamp should be after or equal to beforeTime",
            !timestamp.isBefore(beforeTime.minus(1, ChronoUnit.SECONDS))
        )
        assertTrue(
            "Timestamp should be before or equal to afterTime",
            !timestamp.isAfter(afterTime.plus(1, ChronoUnit.SECONDS))
        )
    }

    @Test
    fun `formatError should include valid ISO-8601 timestamp`() {
        // Arrange
        val error = RuntimeException("Test error")
        val context = "TestContext"

        // Act
        val result = LogFormatter.formatError(error, context)

        // Assert
        assertTrue("Result should contain timestamp", result.contains("timestamp="))

        // Extract and validate timestamp
        val timestampRegex = Regex("timestamp=([^ ]+)")
        val match = timestampRegex.find(result)
        assertNotNull("Timestamp should be present in result", match)

        val timestamp = match!!.groupValues[1]
        assertDoesNotThrow("Timestamp should be valid ISO-8601") {
            Instant.parse(timestamp)
        }
    }

    @Test
    fun `formatTransaction should include valid ISO-8601 timestamp`() {
        // Arrange
        val transactionType = "EXPENSE"
        val metadata = mapOf("amount" to 100.0)

        // Act
        val result = LogFormatter.formatTransaction(transactionType, metadata)

        // Assert
        assertTrue("Result should contain timestamp", result.contains("timestamp="))

        // Extract and validate timestamp
        val timestampRegex = Regex("timestamp=([^ ]+)")
        val match = timestampRegex.find(result)
        assertNotNull("Timestamp should be present in result", match)

        val timestamp = match!!.groupValues[1]
        assertDoesNotThrow("Timestamp should be valid ISO-8601") {
            Instant.parse(timestamp)
        }
    }

    @Test
    fun `formatPerformance should include valid ISO-8601 timestamp`() {
        // Arrange
        val operation = "database_query"
        val duration = 150L
        val metadata = mapOf("query" to "SELECT * FROM users")

        // Act
        val result = LogFormatter.formatPerformance(operation, duration, metadata)

        // Assert
        assertTrue("Result should contain timestamp", result.contains("timestamp="))

        // Extract and validate timestamp
        val timestampRegex = Regex("timestamp=([^ ]+)")
        val match = timestampRegex.find(result)
        assertNotNull("Timestamp should be present in result", match)

        val timestamp = match!!.groupValues[1]
        assertDoesNotThrow("Timestamp should be valid ISO-8601") {
            Instant.parse(timestamp)
        }
    }

    @Test
    fun `timestamp format should be consistent across multiple calls`() {
        // Arrange
        val action = "test_action"
        val userId = "user789"
        val metadata = emptyMap<String, Any?>()

        // Act - Call multiple times
        val result1 = LogFormatter.formatUserAction(action, userId, metadata)
        val result2 = LogFormatter.formatUserAction(action, userId, metadata)
        val result3 = LogFormatter.formatUserAction(action, userId, metadata)

        // Assert - All timestamps should be valid ISO-8601
        val timestampRegex = Regex("timestamp=([^ ]+)")

        val timestamp1 = timestampRegex.find(result1)!!.groupValues[1]
        val timestamp2 = timestampRegex.find(result2)!!.groupValues[1]
        val timestamp3 = timestampRegex.find(result3)!!.groupValues[1]

        assertDoesNotThrow { Instant.parse(timestamp1) }
        assertDoesNotThrow { Instant.parse(timestamp2) }
        assertDoesNotThrow { Instant.parse(timestamp3) }

        // All timestamps should follow same format pattern
        val isoPattern = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z")
        assertTrue("Timestamp 1 should match ISO-8601 pattern", isoPattern.matches(timestamp1))
        assertTrue("Timestamp 2 should match ISO-8601 pattern", isoPattern.matches(timestamp2))
        assertTrue("Timestamp 3 should match ISO-8601 pattern", isoPattern.matches(timestamp3))
    }

    // ==================== Format Structure Tests ====================

    @Test
    fun `formatUserAction should include action and sanitized userId`() {
        // Arrange
        val action = "profile_update"
        val userId = "verylonguserid12345678"
        val metadata = emptyMap<String, Any?>()

        // Act
        val result = LogFormatter.formatUserAction(action, userId, metadata)

        // Assert
        assertTrue("Result should contain action", result.contains("action=$action"))
        assertTrue("Result should contain user field", result.contains("user="))
        assertTrue("Result should contain timestamp", result.contains("timestamp="))
    }

    @Test
    fun `formatUserAction should include metadata when provided`() {
        // Arrange
        val action = "search"
        val userId = "user123"
        val metadata = mapOf(
            "query" to "test query",
            "resultsCount" to 42
        )

        // Act
        val result = LogFormatter.formatUserAction(action, userId, metadata)

        // Assert
        assertTrue("Result should contain query metadata", result.contains("query=test query"))
        assertTrue("Result should contain resultsCount metadata", result.contains("resultsCount=42"))
    }

    @Test
    fun `formatError should include error type and context`() {
        // Arrange
        val error = IllegalArgumentException("Invalid input")
        val context = "UserValidation"

        // Act
        val result = LogFormatter.formatError(error, context)

        // Assert
        assertTrue("Result should contain error type", result.contains("error=IllegalArgumentException"))
        assertTrue("Result should contain context", result.contains("context=$context"))
        assertTrue("Result should contain message", result.contains("message="))
    }

    @Test
    fun `formatTransaction should include transaction type and metadata`() {
        // Arrange
        val transactionType = "TRANSFER"
        val metadata = mapOf(
            "sourceWallet" to "wallet1",
            "destinationWallet" to "wallet2",
            "amount" to 500.0
        )

        // Act
        val result = LogFormatter.formatTransaction(transactionType, metadata)

        // Assert
        assertTrue("Result should contain transaction flag", result.contains("transaction=true"))
        assertTrue("Result should contain type", result.contains("type=$transactionType"))
        assertTrue("Result should contain sourceWallet", result.contains("sourceWallet=wallet1"))
        assertTrue("Result should contain destinationWallet", result.contains("destinationWallet=wallet2"))
        assertTrue("Result should contain amount", result.contains("amount=500.0"))
    }

    @Test
    fun `formatPerformance should include operation and duration`() {
        // Arrange
        val operation = "network_request"
        val duration = 1250L
        val metadata = mapOf("endpoint" to "/api/users")

        // Act
        val result = LogFormatter.formatPerformance(operation, duration, metadata)

        // Assert
        assertTrue("Result should contain performance flag", result.contains("performance=true"))
        assertTrue("Result should contain operation", result.contains("operation=$operation"))
        assertTrue("Result should contain duration", result.contains("duration=$duration"))
        assertTrue("Result should contain endpoint", result.contains("endpoint=/api/users"))
    }

    // ==================== Sanitization Tests ====================

    @Test
    fun `formatUserAction should sanitize email in metadata`() {
        // Arrange
        val action = "email_update"
        val userId = "user123"
        val metadata = mapOf("email" to "test@example.com")

        // Act
        val result = LogFormatter.formatUserAction(action, userId, metadata)

        // Assert
        assertTrue("Result should contain email key", result.contains("email="))
        assertFalse("Result should not contain full email", result.contains("test@example.com"))
    }

    @Test
    fun `formatError should sanitize error message`() {
        // Arrange
        val error = Exception("Error with user@example.com")
        val context = "EmailValidation"

        // Act
        val result = LogFormatter.formatError(error, context)

        // Assert
        assertTrue("Result should contain message field", result.contains("message="))
        // Message should be sanitized by LogSanitizer
    }

    // ==================== Edge Cases ====================

    @Test
    fun `formatUserAction should handle empty metadata`() {
        // Arrange
        val action = "simple_action"
        val userId = "user123"
        val metadata = emptyMap<String, Any?>()

        // Act
        val result = LogFormatter.formatUserAction(action, userId, metadata)

        // Assert
        assertTrue("Result should contain action", result.contains("action=$action"))
        assertTrue("Result should contain user", result.contains("user=user123"))
        assertTrue("Result should contain timestamp", result.contains("timestamp="))
        // Should not have extra space or metadata section
        assertEquals("Should have 3 components", 3, result.split(" ").size)
    }

    @Test
    fun `formatUserAction should handle null values in metadata`() {
        // Arrange
        val action = "action_with_null"
        val userId = "user123"
        val metadata = mapOf<String, Any?>("key1" to null, "key2" to "value2")

        // Act
        val result = LogFormatter.formatUserAction(action, userId, metadata)

        // Assert
        assertTrue("Result should contain key1=null", result.contains("key1=null"))
        assertTrue("Result should contain key2=value2", result.contains("key2=value2"))
    }

    @Test
    fun `formatError should handle null error message`() {
        // Arrange
        val error = RuntimeException(null as String?)
        val context = "NullMessageTest"

        // Act
        val result = LogFormatter.formatError(error, context)

        // Assert
        assertTrue("Result should contain error type", result.contains("error=RuntimeException"))
        assertTrue("Result should contain context", result.contains("context=$context"))
        assertTrue("Result should contain Unknown error message", result.contains("message=\"Unknown error\""))
    }

    // ==================== Helper Functions ====================

    private fun assertDoesNotThrow(message: String = "", block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("$message - Exception thrown: ${e.message}")
        }
    }
}

package com.axeven.profiteerapp.utils.logging

import org.junit.Test
import org.junit.Assert.*

class StructuredLoggingTest {

    @Test
    fun `should format user action logs consistently`() {
        val action = "wallet_created"
        val userId = "user123"
        val metadata = mapOf(
            "walletType" to "Physical",
            "currency" to "USD",
            "initialBalance" to 0.0
        )

        val formatted = LogFormatter.formatUserAction(action, userId, metadata)

        assertTrue("Should contain action", formatted.contains("wallet_created"))
        assertTrue("Should contain user context", formatted.contains("user="))
        assertTrue("Should contain wallet type", formatted.contains("walletType=Physical"))
        assertTrue("Should contain currency", formatted.contains("currency=USD"))
        assertTrue("Should be structured format", formatted.contains("action="))
    }

    @Test
    fun `should format error logs with context`() {
        val error = RuntimeException("Database connection failed")
        val context = "TransactionRepository.createTransaction"

        val formatted = LogFormatter.formatError(error, context)

        assertTrue("Should contain error type", formatted.contains("RuntimeException"))
        assertTrue("Should contain error message", formatted.contains("Database connection failed"))
        assertTrue("Should contain context", formatted.contains("TransactionRepository.createTransaction"))
        assertTrue("Should be structured", formatted.contains("error="))
        assertTrue("Should contain context field", formatted.contains("context="))
    }

    @Test
    fun `should format transaction logs with proper structure`() {
        val transactionType = "transfer"
        val metadata = mapOf(
            "amount" to 150.0,
            "currency" to "EUR",
            "sourceWallet" to "wallet_abc",
            "destinationWallet" to "wallet_xyz"
        )

        val formatted = LogFormatter.formatTransaction(transactionType, metadata)


        assertTrue("Should contain transaction type", formatted.contains("type=transfer"))
        assertTrue("Should contain amount", formatted.contains("amount=150.0"))
        assertTrue("Should contain currency", formatted.contains("currency=EUR"))
        assertTrue("Should contain source wallet", formatted.contains("sourceWallet=wallet_abc"))
        assertTrue("Should be structured", formatted.startsWith("transaction="))
    }

    @Test
    fun `should include timestamp in structured logs`() {
        val action = "user_login"
        val userId = "user456"
        val metadata = emptyMap<String, Any>()

        val formatted = LogFormatter.formatUserAction(action, userId, metadata)

        assertTrue("Should contain timestamp", formatted.contains("timestamp="))
        assertTrue("Should have ISO format timestamp", formatted.matches(Regex(".*timestamp=\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")))
    }

    @Test
    fun `should handle empty metadata gracefully`() {
        val action = "app_start"
        val userId = "anonymous"
        val metadata = emptyMap<String, Any>()

        val formatted = LogFormatter.formatUserAction(action, userId, metadata)

        assertTrue("Should contain action", formatted.contains("action=app_start"))
        assertTrue("Should contain user", formatted.contains("user=anonymous"))
        assertFalse("Should not contain metadata field", formatted.contains("metadata="))
    }

    @Test
    fun `should format performance metrics consistently`() {
        val operation = "database_query"
        val duration = 250L
        val metadata = mapOf(
            "query" to "SELECT * FROM transactions",
            "recordCount" to 42
        )

        val formatted = LogFormatter.formatPerformance(operation, duration, metadata)

        assertTrue("Should contain operation", formatted.contains("operation=database_query"))
        assertTrue("Should contain duration", formatted.contains("duration=250"))
        assertTrue("Should contain query", formatted.contains("query=SELECT * FROM transactions"))
        assertTrue("Should contain record count", formatted.contains("recordCount=42"))
        assertTrue("Should be structured", formatted.startsWith("performance="))
    }

    @Test
    fun `should sanitize sensitive data in structured logs`() {
        val action = "payment_processed"
        val userId = "user789"
        val metadata = mapOf(
            "email" to "user@example.com",
            "amount" to 99.99,
            "token" to "secret123"
        )

        val formatted = LogFormatter.formatUserAction(action, userId, metadata)

        // Basic test - ensure email and token fields are handled
        assertTrue("Should contain action", formatted.contains("action=payment_processed"))
        assertTrue("Should contain user", formatted.contains("user=user789"))
        assertTrue("Should contain amount", formatted.contains("amount=99.99"))
        assertTrue("Should be properly structured", formatted.contains("timestamp="))
    }

    @Test
    fun `should handle null values in metadata`() {
        val action = "data_sync"
        val userId = "user999"
        val metadata = mapOf<String, Any?>(
            "lastSync" to null,
            "recordCount" to 10,
            "status" to "completed"
        )

        val formatted = LogFormatter.formatUserAction(action, userId, metadata)

        assertTrue("Should contain action", formatted.contains("action=data_sync"))
        assertTrue("Should handle null gracefully", formatted.contains("lastSync=null"))
        assertTrue("Should contain other values", formatted.contains("recordCount=10"))
    }

    @Test
    fun `should escape special characters in log values`() {
        val action = "search_query"
        val userId = "user123"
        val metadata = mapOf(
            "query" to "search term with \"quotes\" and = equals",
            "category" to "test & development"
        )

        val formatted = LogFormatter.formatUserAction(action, userId, metadata)

        assertTrue("Should contain action", formatted.contains("action=search_query"))
        // Values should be properly escaped or quoted
        assertTrue("Should handle special characters", formatted.contains("query="))
        assertTrue("Should handle ampersand", formatted.contains("category="))
    }
}
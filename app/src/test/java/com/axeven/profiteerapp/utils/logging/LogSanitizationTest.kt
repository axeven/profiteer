package com.axeven.profiteerapp.utils.logging

import org.junit.Test
import org.junit.Assert.*

class LogSanitizationTest {

    @Test
    fun `should redact user email from logs`() {
        val message = "User authentication successful for user: john.doe@example.com"
        val sanitized = LogSanitizer.sanitizeUserData(message)

        assertFalse("Should not contain email", sanitized.contains("john.doe@example.com"))
        assertTrue("Should contain redaction marker", sanitized.contains("[EMAIL_REDACTED]"))
        assertTrue("Should preserve context", sanitized.contains("User authentication successful"))
    }

    @Test
    fun `should redact user ID from logs`() {
        val message = "Transaction created for userId: abc123-def456-ghi789"
        val sanitized = LogSanitizer.sanitizeUserData(message)

        assertFalse("Should not contain user ID", sanitized.contains("abc123-def456-ghi789"))
        assertTrue("Should contain redaction marker", sanitized.contains("[USER_ID_REDACTED]"))
        assertTrue("Should preserve context", sanitized.contains("Transaction created for userId"))
    }

    @Test
    fun `should redact wallet balances from logs`() {
        val message = "Wallet balance updated: 150000.50 IDR, new balance: 1850000.75"
        val sanitized = LogSanitizer.sanitizeFinancialData(message)

        assertFalse("Should not contain first amount", sanitized.contains("150000.50"))
        assertFalse("Should not contain second amount", sanitized.contains("1850000.75"))
        assertTrue("Should contain redaction marker", sanitized.contains("[AMOUNT_REDACTED]"))
        assertTrue("Should preserve context", sanitized.contains("Wallet balance updated"))
    }

    @Test
    fun `should redact authentication tokens from logs`() {
        val message = "Token refresh successful: eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2NzAy"
        val sanitized = LogSanitizer.sanitizeAuthData(message)


        assertFalse("Should not contain token", sanitized.contains("eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2NzAy"))
        assertTrue("Should contain redaction marker", sanitized.contains("[TOKEN_REDACTED]"))
        assertTrue("Should preserve context", sanitized.contains("Token refresh successful"))
    }

    @Test
    fun `should provide convenience method for sanitizing all data types`() {
        val message = "User admin@company.com processed payment of 999.99 EUR with token xyz789"
        val sanitized = LogSanitizer.sanitizeAll(message)


        assertFalse("Should not contain email", sanitized.contains("admin@company.com"))
        assertFalse("Should not contain amount", sanitized.contains("999.99"))
        assertFalse("Should not contain token", sanitized.contains("xyz789"))
        assertTrue("Should contain email redaction", sanitized.contains("[EMAIL_REDACTED]"))
        assertTrue("Should contain amount redaction", sanitized.contains("[AMOUNT_REDACTED]"))
        assertTrue("Should contain token redaction", sanitized.contains("[TOKEN_REDACTED]"))
    }

    @Test
    fun `should handle empty messages gracefully`() {
        assertEquals("Empty string should remain empty", "", LogSanitizer.sanitizeAll(""))
    }

    @Test
    fun `should handle messages with no sensitive data`() {
        val message = "Application started successfully"
        val sanitized = LogSanitizer.sanitizeAll(message)

        assertEquals("Should return original message", message, sanitized)
    }
}
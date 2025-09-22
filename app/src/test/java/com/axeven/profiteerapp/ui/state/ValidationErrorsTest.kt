package com.axeven.profiteerapp.ui.state

import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive tests for ValidationErrors data class.
 * Validates error management and user-friendly error reporting.
 */
class ValidationErrorsTest {

    @Test
    fun `should start with no errors`() {
        val errors = ValidationErrors()

        assertFalse("Should have no errors initially", errors.hasErrors)
        assertTrue("Should have empty error list", errors.allErrors.isEmpty())
        assertNull("Should have no first error", errors.firstError)
        assertEquals("Should have zero error count", 0, errors.errorCount)
    }

    @Test
    fun `should detect errors correctly`() {
        val withErrors = ValidationErrors(
            titleError = "Title is required",
            amountError = "Amount must be positive"
        )

        assertTrue("Should detect errors", withErrors.hasErrors)
        assertEquals("Should count errors correctly", 2, withErrors.errorCount)
        assertEquals("Should get first error", "Title is required", withErrors.firstError)
    }

    @Test
    fun `should get all error messages`() {
        val errors = ValidationErrors(
            titleError = "Title is required",
            amountError = "Amount must be positive",
            walletError = "Wallet must be selected"
        )

        val allErrors = errors.allErrors
        assertEquals("Should have 3 errors", 3, allErrors.size)
        assertTrue("Should contain title error", allErrors.contains("Title is required"))
        assertTrue("Should contain amount error", allErrors.contains("Amount must be positive"))
        assertTrue("Should contain wallet error", allErrors.contains("Wallet must be selected"))
    }

    @Test
    fun `should check field errors`() {
        val errors = ValidationErrors(titleError = "Title is required")

        assertTrue("Should detect title error", errors.hasFieldError(ValidationField.TITLE))
        assertFalse("Should not detect amount error", errors.hasFieldError(ValidationField.AMOUNT))

        assertEquals("Should get title error message", "Title is required",
                    errors.getFieldError(ValidationField.TITLE))
        assertNull("Should get null for amount error", errors.getFieldError(ValidationField.AMOUNT))
    }

    @Test
    fun `should update field errors immutably`() {
        val original = ValidationErrors()
        val updated = original.updateField(ValidationField.TITLE, "New title error")

        assertNull("Original should remain unchanged", original.titleError)
        assertEquals("Updated should have new error", "New title error", updated.titleError)
    }

    @Test
    fun `should clear all errors`() {
        val withErrors = ValidationErrors(
            titleError = "Title error",
            amountError = "Amount error"
        )

        val cleared = withErrors.clearAll()

        assertNull("Title error should be cleared", cleared.titleError)
        assertNull("Amount error should be cleared", cleared.amountError)
        assertFalse("Should have no errors", cleared.hasErrors)
    }

    @Test
    fun `should clear specific fields`() {
        val withErrors = ValidationErrors(
            titleError = "Title error",
            amountError = "Amount error",
            walletError = "Wallet error"
        )

        val cleared = withErrors.clearFields(ValidationField.TITLE, ValidationField.AMOUNT)

        assertNull("Title error should be cleared", cleared.titleError)
        assertNull("Amount error should be cleared", cleared.amountError)
        assertNotNull("Wallet error should remain", cleared.walletError)
    }

    @Test
    fun `should clear transfer errors`() {
        val withErrors = ValidationErrors(
            titleError = "Title error",
            transferError = "Transfer error"
        )

        val cleared = withErrors.clearTransferErrors()

        assertNotNull("Title error should remain", cleared.titleError)
        assertNull("Transfer error should be cleared", cleared.transferError)
    }

    @Test
    fun `should clear wallet errors`() {
        val withErrors = ValidationErrors(
            titleError = "Title error",
            walletError = "Wallet error",
            transferError = "Transfer error"
        )

        val cleared = withErrors.clearWalletErrors()

        assertNotNull("Title error should remain", cleared.titleError)
        assertNull("Wallet error should be cleared", cleared.walletError)
        assertNull("Transfer error should be cleared", cleared.transferError)
    }

    @Test
    fun `should group errors by severity`() {
        val errors = ValidationErrors(
            titleError = "Title is required",       // Critical
            amountError = "Amount format invalid",  // Warning
            tagsError = "Too many tags",           // Info
            walletError = "Wallet required"        // Critical
        )

        val bySeverity = errors.errorsBySeverity

        val critical = bySeverity[ErrorSeverity.CRITICAL] ?: emptyList()
        val warning = bySeverity[ErrorSeverity.WARNING] ?: emptyList()
        val info = bySeverity[ErrorSeverity.INFO] ?: emptyList()

        assertEquals("Should have 2 critical errors", 2, critical.size)
        assertEquals("Should have 1 warning error", 1, warning.size)
        assertEquals("Should have 1 info error", 1, info.size)
    }

    @Test
    fun `should create summary message`() {
        val noErrors = ValidationErrors()
        assertNull("No errors should have no summary", noErrors.summaryMessage)

        val oneError = ValidationErrors(titleError = "Title is required")
        assertEquals("One error should return first error", "Title is required", oneError.summaryMessage)

        val multipleErrors = ValidationErrors(
            titleError = "Title is required",
            amountError = "Amount invalid"
        )
        val summary = multipleErrors.summaryMessage
        assertNotNull("Multiple errors should have summary", summary)
        assertTrue("Summary should mention error count", summary!!.contains("2 validation errors"))
    }

    @Test
    fun `should create errors from validation`() {
        val errors = ValidationErrors.fromValidation(
            title = "Test Title",
            amount = "100.0",
            titleError = "Title too long",
            amountError = null,
            walletError = "No wallet selected"
        )

        assertEquals("Should set title error", "Title too long", errors.titleError)
        assertNull("Should have no amount error", errors.amountError)
        assertEquals("Should set wallet error", "No wallet selected", errors.walletError)
    }

    @Test
    fun `should build errors using builder pattern`() {
        val errors = buildValidationErrors {
            title("Title is required")
            amount("Amount must be positive")
            wallet("Wallet must be selected")
        }

        assertEquals("Should have title error", "Title is required", errors.titleError)
        assertEquals("Should have amount error", "Amount must be positive", errors.amountError)
        assertEquals("Should have wallet error", "Wallet must be selected", errors.walletError)
        assertTrue("Should have errors", errors.hasErrors)
    }

    @Test
    fun `should provide field display names`() {
        assertEquals("Title", ValidationField.TITLE.displayName())
        assertEquals("Amount", ValidationField.AMOUNT.displayName())
        assertEquals("Wallet Selection", ValidationField.WALLET.displayName())
        assertEquals("Transfer Settings", ValidationField.TRANSFER.displayName())
        assertEquals("Tags", ValidationField.TAGS.displayName())
        assertEquals("Date", ValidationField.DATE.displayName())
    }

    @Test
    fun `should provide severity color names`() {
        assertEquals("error", ErrorSeverity.CRITICAL.colorName())
        assertEquals("warning", ErrorSeverity.WARNING.colorName())
        assertEquals("info", ErrorSeverity.INFO.colorName())
    }
}
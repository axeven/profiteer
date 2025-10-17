package com.axeven.profiteerapp.data.constants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for validation-related constants.
 * Verifies configuration values for form validation rules.
 */
class ValidationConstantsTest {

    @Test
    fun `WALLET_NAME_MIN_LENGTH should equal 2`() {
        assertEquals(2, ValidationConstants.WALLET_NAME_MIN_LENGTH)
    }

    @Test
    fun `WALLET_NAME_MAX_LENGTH should equal 50`() {
        assertEquals(50, ValidationConstants.WALLET_NAME_MAX_LENGTH)
    }

    @Test
    fun `WALLET_NAME_MAX_LENGTH should be greater than MIN_LENGTH`() {
        assertTrue(
            "Max length must be greater than min length",
            ValidationConstants.WALLET_NAME_MAX_LENGTH > ValidationConstants.WALLET_NAME_MIN_LENGTH
        )
    }

    @Test
    fun `TRANSACTION_TITLE_MAX_LENGTH should equal 100`() {
        assertEquals(100, ValidationConstants.TRANSACTION_TITLE_MAX_LENGTH)
    }

    @Test
    fun `TRANSACTION_TITLE_MAX_LENGTH should be positive`() {
        assertTrue(
            "Title max length must be positive",
            ValidationConstants.TRANSACTION_TITLE_MAX_LENGTH > 0
        )
    }

    @Test
    fun `MAX_TAGS_PER_TRANSACTION should equal 15`() {
        assertEquals(15, ValidationConstants.MAX_TAGS_PER_TRANSACTION)
    }

    @Test
    fun `MAX_TAGS_PER_TRANSACTION should be reasonable`() {
        assertTrue(
            "Max tags should be between 5 and 20 for good UX",
            ValidationConstants.MAX_TAGS_PER_TRANSACTION in 5..20
        )
    }

    @Test
    fun `BALANCE_DIFFERENCE_THRESHOLD should equal 10`() {
        assertEquals(10.0, ValidationConstants.BALANCE_DIFFERENCE_THRESHOLD, 0.001)
    }

    @Test
    fun `BALANCE_DIFFERENCE_THRESHOLD should be positive`() {
        assertTrue(
            "Balance difference threshold must be positive",
            ValidationConstants.BALANCE_DIFFERENCE_THRESHOLD > 0
        )
    }
}

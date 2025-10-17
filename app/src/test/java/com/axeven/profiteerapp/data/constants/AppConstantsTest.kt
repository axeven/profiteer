package com.axeven.profiteerapp.data.constants

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for RepositoryConstants.
 * These tests verify that centralized constants have expected values
 * and help prevent accidental changes to critical configuration.
 */
class AppConstantsTest {

    @Test
    fun `TRANSACTION_PAGE_SIZE should equal 20`() {
        assertEquals(20, RepositoryConstants.TRANSACTION_PAGE_SIZE)
    }

    @Test
    fun `SINGLE_RESULT_LIMIT should equal 1`() {
        assertEquals(1, RepositoryConstants.SINGLE_RESULT_LIMIT)
    }

    @Test
    fun `DEFAULT_CURRENCY should equal USD`() {
        assertEquals("USD", RepositoryConstants.DEFAULT_CURRENCY)
    }

    @Test
    fun `MAX_TAG_SUGGESTIONS should equal 10`() {
        assertEquals(10, RepositoryConstants.MAX_TAG_SUGGESTIONS)
    }

    @Test
    fun `all repository constants should be positive`() {
        assert(RepositoryConstants.TRANSACTION_PAGE_SIZE > 0) {
            "TRANSACTION_PAGE_SIZE must be positive"
        }
        assert(RepositoryConstants.SINGLE_RESULT_LIMIT > 0) {
            "SINGLE_RESULT_LIMIT must be positive"
        }
        assert(RepositoryConstants.MAX_TAG_SUGGESTIONS > 0) {
            "MAX_TAG_SUGGESTIONS must be positive"
        }
    }

    @Test
    fun `DEFAULT_CURRENCY should be valid currency code format`() {
        // Currency codes are 3 uppercase letters (ISO 4217)
        val currencyCodePattern = Regex("^[A-Z]{3}$")
        assert(RepositoryConstants.DEFAULT_CURRENCY.matches(currencyCodePattern)) {
            "DEFAULT_CURRENCY must be a valid 3-letter currency code"
        }
    }
}

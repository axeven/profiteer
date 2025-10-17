package com.axeven.profiteerapp.data.constants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for performance and timeout-related constants.
 * Verifies configuration values for retry delays and thresholds.
 */
class PerformanceConstantsTest {

    @Test
    fun `RETRY_DELAY_MS should equal 100`() {
        assertEquals(100L, PerformanceConstants.RETRY_DELAY_MS)
    }

    @Test
    fun `RETRY_DELAY_MS should be positive`() {
        assertTrue(
            "Retry delay must be positive",
            PerformanceConstants.RETRY_DELAY_MS > 0
        )
    }

    @Test
    fun `RETRY_DELAY_MS should be reasonable`() {
        assertTrue(
            "Retry delay should be between 50ms and 500ms",
            PerformanceConstants.RETRY_DELAY_MS in 50..500
        )
    }

    @Test
    fun `SLOW_OPERATION_THRESHOLD_MS should equal 5000`() {
        assertEquals(5000L, PerformanceConstants.SLOW_OPERATION_THRESHOLD_MS)
    }

    @Test
    fun `SLOW_OPERATION_THRESHOLD_MS should be positive`() {
        assertTrue(
            "Slow operation threshold must be positive",
            PerformanceConstants.SLOW_OPERATION_THRESHOLD_MS > 0
        )
    }

    @Test
    fun `SLOW_OPERATION_THRESHOLD_MS should be in seconds`() {
        assertTrue(
            "Slow operation threshold should be at least 1 second",
            PerformanceConstants.SLOW_OPERATION_THRESHOLD_MS >= 1000
        )
    }
}

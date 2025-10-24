package com.axeven.profiteerapp.utils

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Tests for NumberFormatter locale handling.
 *
 * These tests verify that number formatting is consistent across different locales.
 * Critical: Ensures decimal separator is always period (.) regardless of system locale.
 */
class NumberFormatterLocaleTest {

    private var originalLocale: Locale? = null

    @Before
    fun setUp() {
        // Save original locale
        originalLocale = Locale.getDefault()
    }

    @After
    fun tearDown() {
        // Restore original locale
        originalLocale?.let { Locale.setDefault(it) }
    }

    // ==================== Compact Currency Formatting Tests ====================

    @Test
    fun `formatCompactCurrency should use period decimal separator in US locale`() {
        Locale.setDefault(Locale.US)

        val result = NumberFormatter.formatCompactCurrency(1_500_000.0, "USD")

        assertTrue("Should contain period decimal separator", result.contains("."))
        assertFalse("Should not contain comma decimal separator", result.contains(","))
        assertTrue("Should format to 1.5M", result.contains("1.5M"))
    }

    @Test
    fun `formatCompactCurrency should use period decimal separator in German locale`() {
        Locale.setDefault(Locale.GERMANY)

        val result = NumberFormatter.formatCompactCurrency(1_500_000.0, "USD")

        // Must use period, not comma, regardless of locale
        assertTrue("Should contain period decimal separator", result.contains("."))
        assertTrue("Should format to 1.5M with period", result.contains("1.5M"))
    }

    @Test
    fun `formatCompactCurrency should use period decimal separator in French locale`() {
        Locale.setDefault(Locale.FRANCE)

        val result = NumberFormatter.formatCompactCurrency(2_300_000.0, "EUR")

        // Must use period, not comma
        assertTrue("Should contain period decimal separator", result.contains("."))
        assertTrue("Should format to 2.3M with period", result.contains("2.3M"))
    }

    @Test
    fun `formatCompactCurrency should use period decimal separator in Turkish locale`() {
        Locale.setDefault(Locale("tr", "TR"))

        val result = NumberFormatter.formatCompactCurrency(3_700_000.0, "USD")

        // Turkish uses comma as decimal separator, but we need period
        assertTrue("Should contain period decimal separator", result.contains("."))
        assertTrue("Should format to 3.7M with period", result.contains("3.7M"))
    }

    @Test
    fun `formatCompactCurrency thousands should use period in all locales`() {
        val testCases = listOf(
            Locale.US to 1_500.0,
            Locale.GERMANY to 2_300.0,
            Locale.FRANCE to 3_700.0,
            Locale("tr", "TR") to 4_900.0
        )

        testCases.forEach { (locale, amount) ->
            Locale.setDefault(locale)
            val result = NumberFormatter.formatCompactCurrency(amount, "USD")

            assertTrue(
                "Locale ${locale.displayName}: Should contain period for thousands",
                result.contains(".")
            )
            assertFalse(
                "Locale ${locale.displayName}: Should not contain comma as decimal",
                result.matches(Regex(".*\\d,\\d.*"))
            )
        }
    }

    @Test
    fun `formatCompactCurrency millions should use period in all locales`() {
        val testCases = listOf(
            Locale.US to 1_500_000.0,
            Locale.GERMANY to 2_300_000.0,
            Locale.FRANCE to 3_700_000.0,
            Locale("tr", "TR") to 4_900_000.0
        )

        testCases.forEach { (locale, amount) ->
            Locale.setDefault(locale)
            val result = NumberFormatter.formatCompactCurrency(amount, "USD")

            assertTrue(
                "Locale ${locale.displayName}: Should contain period for millions",
                result.contains(".")
            )
            assertFalse(
                "Locale ${locale.displayName}: Should not contain comma as decimal",
                result.matches(Regex(".*\\d,\\d.*"))
            )
        }
    }

    // ==================== Edge Cases ====================

    @Test
    fun `formatCompactCurrency should handle whole millions consistently across locales`() {
        val locales = listOf(
            Locale.US,
            Locale.GERMANY,
            Locale.FRANCE,
            Locale("tr", "TR")
        )

        locales.forEach { locale ->
            Locale.setDefault(locale)
            val result = NumberFormatter.formatCompactCurrency(2_000_000.0, "USD")

            // Should be "2M" without decimal, consistent across all locales
            assertTrue(
                "Locale ${locale.displayName}: Should format as 2M",
                result.contains("2M")
            )
        }
    }

    @Test
    fun `formatCompactCurrency should handle whole thousands consistently across locales`() {
        val locales = listOf(
            Locale.US,
            Locale.GERMANY,
            Locale.FRANCE,
            Locale("tr", "TR")
        )

        locales.forEach { locale ->
            Locale.setDefault(locale)
            val result = NumberFormatter.formatCompactCurrency(5_000.0, "USD")

            // Should be "5K" without decimal
            assertTrue(
                "Locale ${locale.displayName}: Should format as 5K",
                result.contains("5K")
            )
        }
    }

    @Test
    fun `formatCompactCurrency should handle negative amounts consistently`() {
        val locales = listOf(Locale.US, Locale.GERMANY, Locale.FRANCE)

        locales.forEach { locale ->
            Locale.setDefault(locale)
            val result = NumberFormatter.formatCompactCurrency(-1_500_000.0, "USD")

            // Should use period for decimal (the critical locale-independent requirement)
            assertTrue(
                "Locale ${locale.displayName}: Should contain period for decimal",
                result.contains(".")
            )
            // Note: Negative sign formatting may vary by currency symbol placement
            // The important thing is decimal separator consistency
        }
    }

    // ==================== Consistency Tests ====================

    @Test
    fun `formatCompactCurrency should produce identical output across locales`() {
        val amount = 1_234_567.89
        val currency = "USD"

        // Capture results from different locales
        Locale.setDefault(Locale.US)
        val usResult = NumberFormatter.formatCompactCurrency(amount, currency)

        Locale.setDefault(Locale.GERMANY)
        val deResult = NumberFormatter.formatCompactCurrency(amount, currency)

        Locale.setDefault(Locale.FRANCE)
        val frResult = NumberFormatter.formatCompactCurrency(amount, currency)

        Locale.setDefault(Locale("tr", "TR"))
        val trResult = NumberFormatter.formatCompactCurrency(amount, currency)

        // All should produce identical decimal formatting
        assertEquals("US and German locales should produce same format", usResult, deResult)
        assertEquals("US and French locales should produce same format", usResult, frResult)
        assertEquals("US and Turkish locales should produce same format", usResult, trResult)
    }

    @Test
    fun `formatCompactCurrency decimal parts should be parseable as Double`() {
        val locales = listOf(
            Locale.US,
            Locale.GERMANY,
            Locale.FRANCE,
            Locale("tr", "TR")
        )

        locales.forEach { locale ->
            Locale.setDefault(locale)
            val result = NumberFormatter.formatCompactCurrency(1_500_000.0, "USD")

            // Extract the numeric part (e.g., "1.5" from "$1.5M")
            val numericPart = result.replace(Regex("[^0-9.]"), "")

            // Should be parseable as Double using standard US format
            val parsed = numericPart.toDoubleOrNull()
            assertNotNull(
                "Locale ${locale.displayName}: Numeric part '$numericPart' should be parseable",
                parsed
            )
        }
    }
}

package com.axeven.profiteerapp.ui.home

import com.axeven.profiteerapp.utils.TagFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for HomeScreen tag display formatting.
 *
 * These tests verify that transaction tags are properly formatted using
 * TagFormatter before being displayed to users.
 */
class HomeScreenTest {

    // ========================================
    // Tag Display Formatting Tests
    // ========================================

    @Test
    fun `transaction item should display tags in camel case`() {
        // Simulates how tags are displayed in TransactionItem composable
        val storedTags = listOf("food", "grocery shopping", "essentials")
        val formattedTags = TagFormatter.formatTags(storedTags)
        val displayText = formattedTags.joinToString(", ")

        assertEquals("Food, Grocery Shopping, Essentials", displayText)
    }

    @Test
    fun `transaction item should display Untagged when tags are empty`() {
        // Simulates subtitle text building logic in TransactionItem
        val storedTags = emptyList<String>()
        val formattedTags = TagFormatter.formatTags(storedTags)
        val displayText = if (formattedTags.isEmpty()) "Untagged" else formattedTags.joinToString(", ")

        assertEquals("Untagged", displayText)
    }

    @Test
    fun `transaction item should format single tag correctly`() {
        val storedTags = listOf("travel")
        val formattedTags = TagFormatter.formatTags(storedTags)
        val displayText = formattedTags.joinToString(", ")

        assertEquals("Travel", displayText)
    }

    @Test
    fun `transaction item should format multi-word tags correctly`() {
        val storedTags = listOf("home expense", "utility bills")
        val formattedTags = TagFormatter.formatTags(storedTags)
        val displayText = formattedTags.joinToString(", ")

        assertEquals("Home Expense, Utility Bills", displayText)
    }

    @Test
    fun `transaction item should handle mixed case stored tags`() {
        // Even if storage has inconsistent casing (shouldn't happen with normalization)
        val storedTags = listOf("food", "TRAVEL", "Shopping")
        val formattedTags = TagFormatter.formatTags(storedTags)
        val displayText = formattedTags.joinToString(", ")

        assertEquals("Food, Travel, Shopping", displayText)
    }

    @Test
    fun `transaction subtitle should combine formatted tags with other elements`() {
        // Simulates the full subtitle building logic
        val storedTags = listOf("food", "dining out")
        val displayDate = "Jan 15"
        val isTransfer = false

        val formattedTags = TagFormatter.formatTags(storedTags)
        val subtitleText = buildString {
            if (formattedTags.isNotEmpty()) {
                append(formattedTags.joinToString(", "))
            } else {
                append("Untagged")
            }
            append(" • ")
            append(displayDate)
            if (isTransfer) {
                append(" • Transfer")
            }
        }

        assertEquals("Food, Dining Out • Jan 15", subtitleText)
    }

    @Test
    fun `transaction subtitle should show Untagged with date when no tags`() {
        val storedTags = emptyList<String>()
        val displayDate = "Jan 15"
        val isTransfer = false

        val formattedTags = TagFormatter.formatTags(storedTags)
        val subtitleText = buildString {
            if (formattedTags.isNotEmpty()) {
                append(formattedTags.joinToString(", "))
            } else {
                append("Untagged")
            }
            append(" • ")
            append(displayDate)
            if (isTransfer) {
                append(" • Transfer")
            }
        }

        assertEquals("Untagged • Jan 15", subtitleText)
    }

    @Test
    fun `transaction subtitle should include Transfer label when applicable`() {
        val storedTags = listOf("budget")
        val displayDate = "Jan 15"
        val isTransfer = true

        val formattedTags = TagFormatter.formatTags(storedTags)
        val subtitleText = buildString {
            if (formattedTags.isNotEmpty()) {
                append(formattedTags.joinToString(", "))
            } else {
                append("Untagged")
            }
            append(" • ")
            append(displayDate)
            if (isTransfer) {
                append(" • Transfer")
            }
        }

        assertEquals("Budget • Jan 15 • Transfer", subtitleText)
    }

    // ========================================
    // Tag Formatting Preservation Tests
    // ========================================

    @Test
    fun `should not modify original tags list when formatting`() {
        val originalTags = listOf("food", "travel")
        val originalCopy = originalTags.toList()

        // Format tags
        TagFormatter.formatTags(originalTags)

        // Original list should be unchanged
        assertEquals(originalCopy, originalTags)
        assertEquals("food", originalTags[0])
        assertEquals("travel", originalTags[1])
    }

    @Test
    fun `formatted tags should be different from stored tags`() {
        val storedTags = listOf("food", "grocery shopping")
        val formattedTags = TagFormatter.formatTags(storedTags)

        // Stored tags remain lowercase
        assertEquals("food", storedTags[0])
        assertEquals("grocery shopping", storedTags[1])

        // Formatted tags are in camel case
        assertEquals("Food", formattedTags[0])
        assertEquals("Grocery Shopping", formattedTags[1])
    }

    // ========================================
    // Real-World Scenario Tests
    // ========================================

    @Test
    fun `realistic expense transaction tag display`() {
        // Restaurant expense with typical tags
        val tags = listOf("food", "dining out", "entertainment")
        val formattedTags = TagFormatter.formatTags(tags)

        assertEquals(listOf("Food", "Dining Out", "Entertainment"), formattedTags)
        assertEquals("Food, Dining Out, Entertainment", formattedTags.joinToString(", "))
    }

    @Test
    fun `realistic home expense transaction tag display`() {
        // Home expense with multi-word tags
        val tags = listOf("home expense", "utility bills", "electricity")
        val formattedTags = TagFormatter.formatTags(tags)

        assertEquals(listOf("Home Expense", "Utility Bills", "Electricity"), formattedTags)
        assertEquals("Home Expense, Utility Bills, Electricity", formattedTags.joinToString(", "))
    }

    @Test
    fun `realistic shopping transaction tag display`() {
        // Shopping with various tag formats
        val tags = listOf("shopping", "grocery shopping", "essentials")
        val formattedTags = TagFormatter.formatTags(tags)

        assertEquals(listOf("Shopping", "Grocery Shopping", "Essentials"), formattedTags)
        assertEquals("Shopping, Grocery Shopping, Essentials", formattedTags.joinToString(", "))
    }
}

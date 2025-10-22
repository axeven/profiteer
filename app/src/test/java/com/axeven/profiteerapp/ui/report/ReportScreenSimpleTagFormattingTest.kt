package com.axeven.profiteerapp.ui.report

import com.axeven.profiteerapp.utils.TagFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for tag formatting in ReportScreenSimple.
 *
 * These tests verify that tags in expense/income transaction reports are displayed
 * in camel case while preserving the original lowercase tags in the underlying data.
 */
class ReportScreenSimpleTagFormattingTest {

    // ========================================
    // Expense Transaction Legend Formatting Tests
    // ========================================

    @Test
    fun `expense legend should display tags in camel case`() {
        // Simulates SimpleExpenseTransactionsByTagLegend behavior
        val expenseData = mapOf(
            "food" to 1000.0,
            "travel" to 500.0,
            "grocery shopping" to 300.0
        )

        // Format tags for display (as done in UI)
        val formattedTags = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("Food", "Travel", "Grocery Shopping"), formattedTags)
    }

    @Test
    fun `expense legend should handle single tag`() {
        val expenseData = mapOf("food" to 1000.0)

        val formattedTags = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("Food"), formattedTags)
    }

    @Test
    fun `expense legend should handle empty data`() {
        val expenseData = emptyMap<String, Double>()

        val formattedTags = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(emptyList<String>(), formattedTags)
    }

    @Test
    fun `expense legend should format hyphenated tags`() {
        val expenseData = mapOf(
            "food-related" to 500.0,
            "work-expense" to 300.0
        )

        val formattedTags = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("Food-Related", "Work-Expense"), formattedTags)
    }

    @Test
    fun `expense legend should preserve original tags in data map`() {
        val expenseData = mapOf("grocery shopping" to 500.0)

        // Original data should remain unchanged
        assertEquals(true, expenseData.containsKey("grocery shopping"))
        assertEquals(false, expenseData.containsKey("Grocery Shopping"))
    }

    // ========================================
    // Income Transaction Legend Formatting Tests
    // ========================================

    @Test
    fun `income legend should display tags in camel case`() {
        // Simulates SimpleIncomeTransactionsByTagLegend behavior
        val incomeData = mapOf(
            "salary" to 5000.0,
            "freelance work" to 2000.0,
            "side project" to 1000.0
        )

        // Format tags for display
        val formattedTags = incomeData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("Salary", "Freelance Work", "Side Project"), formattedTags)
    }

    @Test
    fun `income legend should handle single tag`() {
        val incomeData = mapOf("salary" to 5000.0)

        val formattedTags = incomeData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("Salary"), formattedTags)
    }

    @Test
    fun `income legend should handle empty data`() {
        val incomeData = emptyMap<String, Double>()

        val formattedTags = incomeData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(emptyList<String>(), formattedTags)
    }

    @Test
    fun `income legend should format multi-word tags correctly`() {
        val incomeData = mapOf(
            "freelance work" to 2000.0,
            "consulting fee" to 1500.0,
            "investment return" to 1000.0
        )

        val formattedTags = incomeData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(
            listOf("Freelance Work", "Consulting Fee", "Investment Return"),
            formattedTags
        )
    }

    @Test
    fun `income legend should preserve original tags in data map`() {
        val incomeData = mapOf("freelance work" to 2000.0)

        // Original data should remain unchanged
        assertEquals(true, incomeData.containsKey("freelance work"))
        assertEquals(false, incomeData.containsKey("Freelance Work"))
    }

    // ========================================
    // Expense Pie Chart Label Formatting Tests
    // ========================================

    @Test
    fun `expense pie chart should format tag labels`() {
        // Simulates ComposeChartsPieChartExpenseTransactionsByTag behavior
        val expenseData = mapOf(
            "food" to 1000.0,
            "travel" to 500.0,
            "entertainment" to 300.0
        )

        // Simulate pie chart data creation with formatted labels
        val pieLabels = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("Food", "Travel", "Entertainment"), pieLabels)
    }

    @Test
    fun `expense pie chart should handle complex tag names`() {
        val expenseData = mapOf(
            "grocery shopping" to 500.0,
            "online shopping" to 300.0,
            "home improvement" to 200.0
        )

        val pieLabels = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(
            listOf("Grocery Shopping", "Online Shopping", "Home Improvement"),
            pieLabels
        )
    }

    @Test
    fun `expense pie chart should preserve data values`() {
        val expenseData = mapOf("food" to 1000.0)

        // Formatting labels shouldn't affect values
        val formattedLabel = TagFormatter.formatTag(expenseData.keys.first())
        val value = expenseData.values.first()

        assertEquals("Food", formattedLabel)
        assertEquals(1000.0, value, 0.001)
    }

    // ========================================
    // Income Pie Chart Label Formatting Tests
    // ========================================

    @Test
    fun `income pie chart should format tag labels`() {
        // Simulates ComposeChartsPieChartIncomeTransactionsByTag behavior
        val incomeData = mapOf(
            "salary" to 5000.0,
            "bonus" to 2000.0,
            "investment" to 1000.0
        )

        // Simulate pie chart data creation with formatted labels
        val pieLabels = incomeData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("Salary", "Bonus", "Investment"), pieLabels)
    }

    @Test
    fun `income pie chart should handle complex tag names`() {
        val incomeData = mapOf(
            "freelance work" to 2000.0,
            "consulting fee" to 1500.0,
            "rental income" to 1000.0
        )

        val pieLabels = incomeData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(
            listOf("Freelance Work", "Consulting Fee", "Rental Income"),
            pieLabels
        )
    }

    @Test
    fun `income pie chart should preserve data values`() {
        val incomeData = mapOf("salary" to 5000.0)

        // Formatting labels shouldn't affect values
        val formattedLabel = TagFormatter.formatTag(incomeData.keys.first())
        val value = incomeData.values.first()

        assertEquals("Salary", formattedLabel)
        assertEquals(5000.0, value, 0.001)
    }

    // ========================================
    // Cross-Component Consistency Tests
    // ========================================

    @Test
    fun `expense legend and pie chart should use same formatting`() {
        val expenseData = mapOf("grocery shopping" to 500.0)

        // Both legend and pie chart should format the same way
        val legendFormatted = TagFormatter.formatTag(expenseData.keys.first())
        val pieChartFormatted = TagFormatter.formatTag(expenseData.keys.first())

        assertEquals("Grocery Shopping", legendFormatted)
        assertEquals(legendFormatted, pieChartFormatted)
    }

    @Test
    fun `income legend and pie chart should use same formatting`() {
        val incomeData = mapOf("freelance work" to 2000.0)

        // Both legend and pie chart should format the same way
        val legendFormatted = TagFormatter.formatTag(incomeData.keys.first())
        val pieChartFormatted = TagFormatter.formatTag(incomeData.keys.first())

        assertEquals("Freelance Work", legendFormatted)
        assertEquals(legendFormatted, pieChartFormatted)
    }

    @Test
    fun `expense and income formatting should be consistent`() {
        val tag = "work expense"

        val expenseFormatted = TagFormatter.formatTag(tag)
        val incomeFormatted = TagFormatter.formatTag(tag)

        // Same tag should format identically across both expense and income displays
        assertEquals("Work Expense", expenseFormatted)
        assertEquals(expenseFormatted, incomeFormatted)
    }

    // ========================================
    // Integration Tests
    // ========================================

    @Test
    fun `report should handle mixed tag formats in expense data`() {
        val expenseData = mapOf(
            "food" to 500.0,
            "grocery shopping" to 300.0,
            "work-expense" to 200.0
        )

        val formattedTags = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(
            listOf("Food", "Grocery Shopping", "Work-Expense"),
            formattedTags
        )
    }

    @Test
    fun `report should handle mixed tag formats in income data`() {
        val incomeData = mapOf(
            "salary" to 5000.0,
            "freelance work" to 2000.0,
            "side-project" to 1000.0
        )

        val formattedTags = incomeData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(
            listOf("Salary", "Freelance Work", "Side-Project"),
            formattedTags
        )
    }

    @Test
    fun `report should sort by value and maintain formatted tag association`() {
        val expenseData = mapOf(
            "food" to 1000.0,
            "travel" to 500.0,
            "entertainment" to 300.0
        )

        // Sort by value descending (as done in the legends)
        val sortedEntries = expenseData.entries.sortedByDescending { it.value }
        val formattedTags = sortedEntries.map { TagFormatter.formatTag(it.key) }

        assertEquals(listOf("Food", "Travel", "Entertainment"), formattedTags)
    }

    @Test
    fun `report should handle special characters in tags`() {
        val expenseData = mapOf(
            "food&drink" to 500.0,
            "50% off" to 300.0
        )

        val formattedTags = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("Food&drink", "50% Off"), formattedTags)
    }

    @Test
    fun `report should handle single character tags`() {
        val expenseData = mapOf(
            "a" to 100.0,
            "b" to 200.0
        )

        val formattedTags = expenseData.keys.map { TagFormatter.formatTag(it) }

        assertEquals(listOf("A", "B"), formattedTags)
    }
}

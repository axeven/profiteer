package com.axeven.profiteerapp.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for TagFormatter utility functions.
 *
 * Following TDD approach - tests written BEFORE implementation.
 * These tests define the expected behavior of tag display formatting.
 *
 * TagFormatter is responsible for DISPLAY formatting only (camel case).
 * Storage and normalization remain lowercase (handled by TagNormalizer).
 */
class TagFormatterTest {

    // ========================================
    // formatTag() - Single Tag Tests
    // ========================================

    @Test
    fun `formatTag - single word lowercase to camel case`() {
        val result = TagFormatter.formatTag("food")
        assertEquals("Food", result)
    }

    @Test
    fun `formatTag - single word uppercase to camel case`() {
        val result = TagFormatter.formatTag("FOOD")
        assertEquals("Food", result)
    }

    @Test
    fun `formatTag - single word mixed case to camel case`() {
        val result = TagFormatter.formatTag("fOoD")
        assertEquals("Food", result)
    }

    @Test
    fun `formatTag - empty string returns empty string`() {
        val result = TagFormatter.formatTag("")
        assertEquals("", result)
    }

    @Test
    fun `formatTag - whitespace only returns empty string`() {
        val result = TagFormatter.formatTag("   ")
        assertEquals("", result)
    }

    // ========================================
    // formatTag() - Multi-Word Tag Tests
    // ========================================

    @Test
    fun `formatTag - two words lowercase to camel case each word`() {
        val result = TagFormatter.formatTag("grocery shopping")
        assertEquals("Grocery Shopping", result)
    }

    @Test
    fun `formatTag - two words uppercase to camel case each word`() {
        val result = TagFormatter.formatTag("GROCERY SHOPPING")
        assertEquals("Grocery Shopping", result)
    }

    @Test
    fun `formatTag - multiple spaces between words to single space`() {
        val result = TagFormatter.formatTag("grocery  shopping")
        assertEquals("Grocery Shopping", result)
    }

    @Test
    fun `formatTag - leading and trailing whitespace trimmed and formatted`() {
        val result = TagFormatter.formatTag("  food  ")
        assertEquals("Food", result)
    }

    @Test
    fun `formatTag - three words formatted correctly`() {
        val result = TagFormatter.formatTag("home expense budget")
        assertEquals("Home Expense Budget", result)
    }

    @Test
    fun `formatTag - mixed case multi-word normalized`() {
        val result = TagFormatter.formatTag("GrOcErY sHoPpInG")
        assertEquals("Grocery Shopping", result)
    }

    // ========================================
    // formatTag() - Edge Cases
    // ========================================

    @Test
    fun `formatTag - single character to uppercase`() {
        val result = TagFormatter.formatTag("a")
        assertEquals("A", result)
    }

    @Test
    fun `formatTag - numbers in tag preserved`() {
        val result = TagFormatter.formatTag("groceries2024")
        assertEquals("Groceries2024", result)
    }

    @Test
    fun `formatTag - numbers at start preserved`() {
        val result = TagFormatter.formatTag("2024budget")
        assertEquals("2024budget", result)
    }

    @Test
    fun `formatTag - tag with numbers in middle`() {
        val result = TagFormatter.formatTag("q1budget2024")
        assertEquals("Q1budget2024", result)
    }

    @Test
    fun `formatTag - special characters preserved`() {
        val result = TagFormatter.formatTag("food&drink")
        assertEquals("Food&drink", result)
    }

    @Test
    fun `formatTag - hyphenated words each part capitalized`() {
        val result = TagFormatter.formatTag("food-related")
        assertEquals("Food-Related", result)
    }

    @Test
    fun `formatTag - multiple hyphens handled`() {
        val result = TagFormatter.formatTag("food-and-drink-related")
        assertEquals("Food-And-Drink-Related", result)
    }

    @Test
    fun `formatTag - underscore preserved as word separator`() {
        val result = TagFormatter.formatTag("home_expense")
        assertEquals("Home_expense", result)
    }

    @Test
    fun `formatTag - apostrophe in word preserved`() {
        val result = TagFormatter.formatTag("children's toys")
        assertEquals("Children's Toys", result)
    }

    @Test
    fun `formatTag - parentheses preserved`() {
        val result = TagFormatter.formatTag("food (groceries)")
        assertEquals("Food (groceries)", result)
    }

    @Test
    fun `formatTag - tab characters treated as whitespace`() {
        val result = TagFormatter.formatTag("\tfood\t")
        assertEquals("Food", result)
    }

    @Test
    fun `formatTag - newline characters treated as whitespace`() {
        val result = TagFormatter.formatTag("\ntravel\n")
        assertEquals("Travel", result)
    }

    @Test
    fun `formatTag - already formatted tag unchanged`() {
        val result = TagFormatter.formatTag("Food")
        assertEquals("Food", result)
    }

    @Test
    fun `formatTag - unicode characters preserved`() {
        val result = TagFormatter.formatTag("食費")
        assertEquals("食費", result)
    }

    @Test
    fun `formatTag - emoji preserved`() {
        val result = TagFormatter.formatTag("🍔 food")
        assertEquals("🍔 Food", result)
    }

    // ========================================
    // formatTags() - List Formatting Tests
    // ========================================

    @Test
    fun `formatTags - empty list returns empty list`() {
        val tags = emptyList<String>()
        val result = TagFormatter.formatTags(tags)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `formatTags - single tag list formatted`() {
        val tags = listOf("food")
        val result = TagFormatter.formatTags(tags)
        assertEquals(listOf("Food"), result)
    }

    @Test
    fun `formatTags - multiple tags all formatted`() {
        val tags = listOf("food", "travel")
        val result = TagFormatter.formatTags(tags)
        assertEquals(listOf("Food", "Travel"), result)
    }

    @Test
    fun `formatTags - mixed case input normalized`() {
        val tags = listOf("food", "TRAVEL", "shopping")
        val result = TagFormatter.formatTags(tags)
        assertEquals(listOf("Food", "Travel", "Shopping"), result)
    }

    @Test
    fun `formatTags - multi-word tags formatted correctly`() {
        val tags = listOf("grocery shopping", "home expense", "travel cost")
        val result = TagFormatter.formatTags(tags)
        assertEquals(listOf("Grocery Shopping", "Home Expense", "Travel Cost"), result)
    }

    @Test
    fun `formatTags - blank tags filtered out`() {
        val tags = listOf("food", "", "travel", "   ")
        val result = TagFormatter.formatTags(tags)
        assertEquals(listOf("Food", "Travel"), result)
    }

    @Test
    fun `formatTags - whitespace-only tags filtered out`() {
        val tags = listOf("food", "\t", "travel", "\n")
        val result = TagFormatter.formatTags(tags)
        assertEquals(listOf("Food", "Travel"), result)
    }

    @Test
    fun `formatTags - preserves order`() {
        val tags = listOf("zebra", "apple", "monkey")
        val result = TagFormatter.formatTags(tags)
        assertEquals(listOf("Zebra", "Apple", "Monkey"), result)
    }

    @Test
    fun `formatTags - handles large list efficiently`() {
        val tags = (1..1000).map { "tag$it" }
        val result = TagFormatter.formatTags(tags)
        assertEquals(1000, result.size)
        assertEquals("Tag1", result[0])
        assertEquals("Tag1000", result[999])
    }

    // ========================================
    // Integration with TagNormalizer
    // ========================================

    @Test
    fun `integration - format after normalization pipeline`() {
        // Simulate user input → normalize → format workflow
        val userInput = "  Food , TRAVEL  "
        val normalized = TagNormalizer.parseTagInput(userInput)
        val formatted = TagFormatter.formatTags(normalized)

        assertEquals(listOf("Food", "Travel"), formatted)
    }

    @Test
    fun `integration - duplicate handling with normalization`() {
        // TagNormalizer deduplicates, then TagFormatter formats
        val tags = listOf("food", "Food", "FOOD")
        val normalized = TagNormalizer.normalizeTags(tags)
        val formatted = TagFormatter.formatTags(normalized)

        assertEquals(listOf("Food"), formatted)
    }

    @Test
    fun `integration - multi-word tags through full pipeline`() {
        val userInput = "grocery shopping, GROCERY SHOPPING, Grocery Shopping"
        val normalized = TagNormalizer.parseTagInput(userInput)
        val formatted = TagFormatter.formatTags(normalized)

        assertEquals(listOf("Grocery Shopping"), formatted)
    }

    @Test
    fun `integration - complex real-world scenario`() {
        // User types various formats, system normalizes to lowercase, then formats for display
        val userInput = " Home Expense , travel-related , FOOD&DRINK , groceries2024 "
        val normalized = TagNormalizer.parseTagInput(userInput)
        val formatted = TagFormatter.formatTags(normalized)

        assertEquals(
            listOf("Home Expense", "Travel-Related", "Food&drink", "Groceries2024"),
            formatted
        )
    }

    @Test
    fun `integration - Untagged filtered by normalizer before formatting`() {
        val userInput = "food, Untagged, travel"
        val normalized = TagNormalizer.parseTagInput(userInput)
        val formatted = TagFormatter.formatTags(normalized)

        // "Untagged" should be removed by normalizer, so formatter never sees it
        assertEquals(listOf("Food", "Travel"), formatted)
    }

    @Test
    fun `integration - blank tags filtered by normalizer`() {
        val userInput = "food, , travel, "
        val normalized = TagNormalizer.parseTagInput(userInput)
        val formatted = TagFormatter.formatTags(normalized)

        assertEquals(listOf("Food", "Travel"), formatted)
    }

    // ========================================
    // Real-World Display Scenarios
    // ========================================

    @Test
    fun `display - home screen transaction subtitle`() {
        // Simulates HomeScreen displaying transaction tags
        val storedTags = listOf("food", "grocery shopping", "essentials")
        val displayTags = TagFormatter.formatTags(storedTags)
        val subtitle = displayTags.joinToString(", ")

        assertEquals("Food, Grocery Shopping, Essentials", subtitle)
    }

    @Test
    fun `display - autocomplete suggestion formatting`() {
        // Simulates CreateTransactionScreen autocomplete dropdown
        val availableTags = listOf("food", "travel", "home expense", "grocery shopping")
        val suggestions = availableTags.filter { it.contains("food", ignoreCase = true) }
        val formattedSuggestions = TagFormatter.formatTags(suggestions)

        assertEquals(listOf("Food"), formattedSuggestions)
    }

    @Test
    fun `display - tag filter dropdown formatting`() {
        // Simulates TransactionListScreen filter options
        val allTags = listOf("food", "travel", "shopping", "home expense")
        val formattedTags = TagFormatter.formatTags(allTags)

        assertEquals(listOf("Food", "Travel", "Shopping", "Home Expense"), formattedTags)
    }

    @Test
    fun `display - empty tag list shows Untagged label`() {
        // Simulates displaying transaction with no tags
        val storedTags = emptyList<String>()
        val displayTags = TagFormatter.formatTags(storedTags)
        val subtitle = if (displayTags.isEmpty()) "Untagged" else displayTags.joinToString(", ")

        assertEquals("Untagged", subtitle)
    }

    // ========================================
    // Consistency Tests
    // ========================================

    @Test
    fun `consistency - same input always produces same output`() {
        val tag = "food"
        val result1 = TagFormatter.formatTag(tag)
        val result2 = TagFormatter.formatTag(tag)

        assertEquals(result1, result2)
    }

    @Test
    fun `consistency - formatting is idempotent`() {
        val tag = "food"
        val formatted1 = TagFormatter.formatTag(tag)
        val formatted2 = TagFormatter.formatTag(formatted1)

        assertEquals(formatted1, formatted2)
        assertEquals("Food", formatted2)
    }

    @Test
    fun `consistency - list formatting maintains order`() {
        val tags = listOf("zebra", "apple", "monkey", "banana")
        val result = TagFormatter.formatTags(tags)

        assertEquals(listOf("Zebra", "Apple", "Monkey", "Banana"), result)
    }

    // ========================================
    // Performance & Robustness
    // ========================================

    @Test
    fun `performance - handles very long tag name`() {
        val longTag = "a".repeat(1000)
        val result = TagFormatter.formatTag(longTag)

        assertEquals("A" + "a".repeat(999), result)
    }

    @Test
    fun `robustness - handles tag with only special characters`() {
        val result = TagFormatter.formatTag("@#$%")
        assertEquals("@#$%", result)
    }

    @Test
    fun `robustness - handles tag with mixed special chars and words`() {
        val result = TagFormatter.formatTag("@food #travel $shopping")
        assertEquals("@food #travel $shopping", result)
    }

    @Test
    fun `robustness - handles tag with dots as separators`() {
        val result = TagFormatter.formatTag("home.expense.budget")
        assertEquals("Home.expense.budget", result)
    }
}

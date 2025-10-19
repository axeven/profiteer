package com.axeven.profiteerapp.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for TagNormalizer utility functions.
 *
 * Following TDD approach - tests written BEFORE implementation.
 * These tests define the expected behavior of tag normalization.
 */
class TagNormalizerTest {

    // ========================================
    // normalizeTag() - Single Tag Tests
    // ========================================

    @Test
    fun `normalizeTag - trims leading whitespace`() {
        val result = TagNormalizer.normalizeTag("  food")
        assertEquals("food", result)
    }

    @Test
    fun `normalizeTag - trims trailing whitespace`() {
        val result = TagNormalizer.normalizeTag("food  ")
        assertEquals("food", result)
    }

    @Test
    fun `normalizeTag - trims leading and trailing whitespace`() {
        val result = TagNormalizer.normalizeTag("  food  ")
        assertEquals("food", result)
    }

    @Test
    fun `normalizeTag - converts to lowercase`() {
        val result = TagNormalizer.normalizeTag("FOOD")
        assertEquals("food", result)
    }

    @Test
    fun `normalizeTag - converts mixed case to lowercase`() {
        val result = TagNormalizer.normalizeTag("FoOd")
        assertEquals("food", result)
    }

    @Test
    fun `normalizeTag - handles empty string`() {
        val result = TagNormalizer.normalizeTag("")
        assertEquals("", result)
    }

    @Test
    fun `normalizeTag - handles whitespace-only string`() {
        val result = TagNormalizer.normalizeTag("   ")
        assertEquals("", result)
    }

    @Test
    fun `normalizeTag - handles tab characters`() {
        val result = TagNormalizer.normalizeTag("\ttravel\t")
        assertEquals("travel", result)
    }

    @Test
    fun `normalizeTag - handles newline characters`() {
        val result = TagNormalizer.normalizeTag("\ntravel\n")
        assertEquals("travel", result)
    }

    @Test
    fun `normalizeTag - preserves internal spaces`() {
        val result = TagNormalizer.normalizeTag("  home expense  ")
        assertEquals("home expense", result)
    }

    @Test
    fun `normalizeTag - handles already normalized tag`() {
        val result = TagNormalizer.normalizeTag("food")
        assertEquals("food", result)
    }

    // ========================================
    // normalizeTags() - Tag List Tests
    // ========================================

    @Test
    fun `normalizeTags - removes duplicates case-insensitively`() {
        val tags = listOf("food", "Food", "FOOD")
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(listOf("food"), result)
    }

    @Test
    fun `normalizeTags - preserves order of first occurrence`() {
        val tags = listOf("travel", "food", "TRAVEL", "shopping", "Food")
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(listOf("travel", "food", "shopping"), result)
    }

    @Test
    fun `normalizeTags - filters out blank tags`() {
        val tags = listOf("food", "", "travel", "   ", "shopping")
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `normalizeTags - handles mixed case duplicates`() {
        val tags = listOf("Food", "food", "FOOD", "FoOd")
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(listOf("food"), result)
    }

    @Test
    fun `normalizeTags - trims whitespace from each tag`() {
        val tags = listOf("  food  ", " travel ", "shopping")
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `normalizeTags - handles empty list`() {
        val tags = emptyList<String>()
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `normalizeTags - handles list with only blank tags`() {
        val tags = listOf("", "   ", "\t", "\n")
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `normalizeTags - handles single tag`() {
        val tags = listOf("Food")
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(listOf("food"), result)
    }

    @Test
    fun `normalizeTags - removes Untagged case-insensitively`() {
        val tags = listOf("food", "Untagged", "travel", "untagged", "UNTAGGED")
        val result = TagNormalizer.normalizeTags(tags)
        assertEquals(listOf("food", "travel"), result)
    }

    // ========================================
    // parseTagInput() - String Parsing Tests
    // ========================================

    @Test
    fun `parseTagInput - splits on comma`() {
        val input = "food,travel,shopping"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `parseTagInput - trims each tag`() {
        val input = " food , travel , shopping "
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `parseTagInput - removes duplicates case-insensitively`() {
        val input = "food, Food, FOOD"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food"), result)
    }

    @Test
    fun `parseTagInput - handles spaces around commas`() {
        val input = "food , travel , shopping"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `parseTagInput - handles multiple spaces`() {
        val input = "food  ,  travel  ,  shopping"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `parseTagInput - filters out empty tags`() {
        val input = "food, , travel, , shopping"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `parseTagInput - handles empty string`() {
        val input = ""
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `parseTagInput - handles whitespace-only string`() {
        val input = "   "
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `parseTagInput - handles single tag without comma`() {
        val input = "food"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food"), result)
    }

    @Test
    fun `parseTagInput - handles trailing comma`() {
        val input = "food, travel,"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel"), result)
    }

    @Test
    fun `parseTagInput - handles leading comma`() {
        val input = ", food, travel"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel"), result)
    }

    @Test
    fun `parseTagInput - handles multiple consecutive commas`() {
        val input = "food,,,travel,,shopping"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `parseTagInput - complex real-world example`() {
        val input = " Food, food, FOOD ,  travel , Shopping, shopping , "
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel", "shopping"), result)
    }

    @Test
    fun `parseTagInput - removes Untagged case-insensitively`() {
        val input = "food, Untagged, travel, untagged"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("food", "travel"), result)
    }

    @Test
    fun `parseTagInput - handles tags with internal spaces`() {
        val input = "home expense, travel cost, food budget"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("home expense", "travel cost", "food budget"), result)
    }

    // ========================================
    // Edge Cases & Performance
    // ========================================

    @Test
    fun `normalizeTags - handles large list efficiently`() {
        val tags = (1..1000).map { "tag$it" } + (1..1000).map { "TAG$it" }
        val result = TagNormalizer.normalizeTags(tags)
        // Should deduplicate to 1000 unique tags
        assertEquals(1000, result.size)
    }

    @Test
    fun `parseTagInput - handles very long input`() {
        val input = (1..100).joinToString(",") { "tag$it" }
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(100, result.size)
    }

    @Test
    fun `normalizeTag - handles unicode characters`() {
        val result = TagNormalizer.normalizeTag("  食費  ")
        assertEquals("食費", result)
    }

    @Test
    fun `normalizeTag - handles emojis`() {
        val result = TagNormalizer.normalizeTag("  🍔 food  ")
        assertEquals("🍔 food", result)
    }

    @Test
    fun `parseTagInput - handles unicode tags`() {
        val input = "食費, 旅行, 食費"
        val result = TagNormalizer.parseTagInput(input)
        assertEquals(listOf("食費", "旅行"), result)
    }
}

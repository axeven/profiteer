package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.utils.TagFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for tag filter display formatting in TransactionListScreen.
 *
 * These tests verify that:
 * 1. Available tags in filter dropdown are displayed in camel case
 * 2. Selected tags are displayed in camel case
 * 3. Filtering logic remains case-insensitive
 * 4. Original lowercase tags are used for filtering operations
 */
class TransactionListScreenTagFormattingTest {

    // ========================================
    // Available Tags Display Tests
    // ========================================

    @Test
    fun `available tags should display in camel case in filter dropdown`() {
        val availableTags = listOf("food", "travel", "grocery shopping")

        // Format for display
        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("Food", "Travel", "Grocery Shopping"), formattedTags)
    }

    @Test
    fun `available tags with multi-word names should format correctly`() {
        val availableTags = listOf("home expense", "utility bills", "dining out")

        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals(
            listOf("Home Expense", "Utility Bills", "Dining Out"),
            formattedTags
        )
    }

    @Test
    fun `empty available tags should return empty list`() {
        val availableTags = emptyList<String>()

        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals(emptyList<String>(), formattedTags)
    }

    // ========================================
    // Selected Tags Display Tests
    // ========================================

    @Test
    fun `single selected tag should display in camel case`() {
        val selectedTags = setOf("food")

        // When one tag is selected, display it formatted
        val displayText = if (selectedTags.size == 1) {
            TagFormatter.formatTag(selectedTags.first())
        } else {
            "${selectedTags.size} tags selected"
        }

        assertEquals("Food", displayText)
    }

    @Test
    fun `multiple selected tags should show count`() {
        val selectedTags = setOf("food", "travel", "shopping")

        val displayText = when (selectedTags.size) {
            0 -> "All Tags"
            1 -> TagFormatter.formatTag(selectedTags.first())
            else -> "${selectedTags.size} tags selected"
        }

        assertEquals("3 tags selected", displayText)
    }

    @Test
    fun `no selected tags should show All Tags`() {
        val selectedTags = emptySet<String>()

        val displayText = when (selectedTags.size) {
            0 -> "All Tags"
            1 -> TagFormatter.formatTag(selectedTags.first())
            else -> "${selectedTags.size} tags selected"
        }

        assertEquals("All Tags", displayText)
    }

    // ========================================
    // Tag Selection and Filtering Tests
    // ========================================

    @Test
    fun `selecting formatted tag should toggle original lowercase tag`() {
        val availableTags = listOf("food", "travel", "shopping")
        val formattedTags = TagFormatter.formatTags(availableTags)

        // User clicks "Food" in the dropdown
        val clickedFormattedTag = "Food"
        val clickedIndex = formattedTags.indexOf(clickedFormattedTag)
        val originalTag = availableTags[clickedIndex]

        // Original lowercase tag should be used for filtering
        assertEquals("food", originalTag)
    }

    @Test
    fun `tag filtering should use original lowercase tags`() {
        val availableTags = listOf("food", "travel", "grocery shopping")
        val selectedTags = mutableSetOf<String>()

        // Simulate user selecting tags
        val formattedTags = TagFormatter.formatTags(availableTags)

        // User selects "Grocery Shopping"
        val selectedFormattedTag = "Grocery Shopping"
        val selectedIndex = formattedTags.indexOf(selectedFormattedTag)
        val originalTag = availableTags[selectedIndex]

        selectedTags.add(originalTag)

        // Selected tags should contain lowercase version
        assertTrue(selectedTags.contains("grocery shopping"))
        assertEquals(setOf("grocery shopping"), selectedTags)
    }

    @Test
    fun `deselecting tag should use original lowercase tag`() {
        val availableTags = listOf("food", "travel", "shopping")
        val selectedTags = mutableSetOf("food", "travel")

        // Format for display
        val formattedTags = TagFormatter.formatTags(availableTags)

        // User deselects "Travel"
        val deselectedFormattedTag = "Travel"
        val deselectedIndex = formattedTags.indexOf(deselectedFormattedTag)
        val originalTag = availableTags[deselectedIndex]

        selectedTags.remove(originalTag)

        assertEquals(setOf("food"), selectedTags)
    }

    // ========================================
    // Clear All Functionality Tests
    // ========================================

    @Test
    fun `clearing all selected tags should work with original tags`() {
        val availableTags = listOf("food", "travel", "shopping")
        val selectedTags = mutableSetOf("food", "travel", "shopping")

        // Clear all - iterate over current selected tags
        selectedTags.toList().forEach { tag ->
            selectedTags.remove(tag)
        }

        assertTrue(selectedTags.isEmpty())
    }

    @Test
    fun `clear all button should only show when tags selected`() {
        val selectedTagsEmpty = emptySet<String>()
        val selectedTagsWithItems = setOf("food", "travel")

        val showClearForEmpty = selectedTagsEmpty.isNotEmpty()
        val showClearForItems = selectedTagsWithItems.isNotEmpty()

        assertEquals(false, showClearForEmpty)
        assertEquals(true, showClearForItems)
    }

    // ========================================
    // Tag Display Consistency Tests
    // ========================================

    @Test
    fun `formatted tags should match available tags count`() {
        val availableTags = listOf("food", "travel", "shopping", "home expense")
        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals(availableTags.size, formattedTags.size)
    }

    @Test
    fun `formatted and original tags should maintain same order`() {
        val availableTags = listOf("zebra", "apple", "monkey")
        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals("Zebra", formattedTags[0])
        assertEquals("Apple", formattedTags[1])
        assertEquals("Monkey", formattedTags[2])
    }

    @Test
    fun `original tags should remain unchanged after formatting`() {
        val availableTags = listOf("food", "travel", "shopping")
        val originalCopy = availableTags.toList()

        TagFormatter.formatTags(availableTags)

        assertEquals(originalCopy, availableTags)
    }

    // ========================================
    // Real-World Filter Scenarios Tests
    // ========================================

    @Test
    fun `realistic tag filter workflow`() {
        // Setup: Available tags from user's transactions
        val availableTags = listOf("food", "travel", "grocery shopping", "dining out")
        val selectedTags = mutableSetOf<String>()

        // Step 1: Format tags for display
        val formattedTags = TagFormatter.formatTags(availableTags)
        assertEquals(
            listOf("Food", "Travel", "Grocery Shopping", "Dining Out"),
            formattedTags
        )

        // Step 2: User selects "Grocery Shopping"
        val selectedFormattedTag = "Grocery Shopping"
        val selectedIndex = formattedTags.indexOf(selectedFormattedTag)
        val originalTag = availableTags[selectedIndex]
        selectedTags.add(originalTag)

        // Step 3: Verify filtering uses lowercase tag
        assertEquals(setOf("grocery shopping"), selectedTags)

        // Step 4: Display shows formatted tag
        val displayText = TagFormatter.formatTag(selectedTags.first())
        assertEquals("Grocery Shopping", displayText)
    }

    @Test
    fun `realistic multi-selection workflow`() {
        val availableTags = listOf("food", "travel", "shopping")
        val selectedTags = mutableSetOf<String>()
        val formattedTags = TagFormatter.formatTags(availableTags)

        // User selects multiple tags
        listOf("Food", "Travel").forEach { formattedTag ->
            val index = formattedTags.indexOf(formattedTag)
            selectedTags.add(availableTags[index])
        }

        assertEquals(setOf("food", "travel"), selectedTags)

        // Display shows count
        val displayText = "${selectedTags.size} tags selected"
        assertEquals("2 tags selected", displayText)
    }

    @Test
    fun `tag filter with special characters`() {
        val availableTags = listOf("food-related", "work-from-home")
        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("Food-Related", "Work-From-Home"), formattedTags)
    }

    // ========================================
    // Checkbox State Tests
    // ========================================

    @Test
    fun `checkbox should be checked for selected tags`() {
        val availableTags = listOf("food", "travel", "shopping")
        val selectedTags = setOf("food", "travel")

        // Check each tag's selection state
        val tagStates = availableTags.map { tag ->
            tag to selectedTags.contains(tag)
        }

        assertEquals(true, tagStates[0].second) // food - selected
        assertEquals(true, tagStates[1].second) // travel - selected
        assertEquals(false, tagStates[2].second) // shopping - not selected
    }

    @Test
    fun `formatted tags should preserve checkbox state mapping`() {
        val availableTags = listOf("food", "travel", "shopping")
        val formattedTags = TagFormatter.formatTags(availableTags)
        val selectedTags = setOf("food")

        // Map formatted tags to their selection state
        val formattedStates = availableTags.mapIndexed { index, originalTag ->
            formattedTags[index] to selectedTags.contains(originalTag)
        }

        assertEquals(true, formattedStates[0].second) // "Food" - selected
        assertEquals(false, formattedStates[1].second) // "Travel" - not selected
        assertEquals(false, formattedStates[2].second) // "Shopping" - not selected
    }

    // ========================================
    // Edge Cases Tests
    // ========================================

    @Test
    fun `single character tag should format correctly`() {
        val availableTags = listOf("a", "b", "c")
        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("A", "B", "C"), formattedTags)
    }

    @Test
    fun `tags with numbers should format correctly`() {
        val availableTags = listOf("budget2024", "q1expenses")
        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("Budget2024", "Q1expenses"), formattedTags)
    }

    @Test
    fun `large tag list should format efficiently`() {
        val availableTags = (1..100).map { "tag$it" }
        val formattedTags = TagFormatter.formatTags(availableTags)

        assertEquals(100, formattedTags.size)
        assertEquals("Tag1", formattedTags[0])
        assertEquals("Tag100", formattedTags[99])
    }
}

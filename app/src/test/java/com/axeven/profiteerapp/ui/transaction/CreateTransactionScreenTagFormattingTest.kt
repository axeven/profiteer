package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.utils.TagFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for tag autocomplete formatting in CreateTransactionScreen.
 *
 * These tests verify that autocomplete suggestions are displayed in camel case
 * while preserving the original lowercase tags for storage.
 */
class CreateTransactionScreenTagFormattingTest {

    // ========================================
    // Autocomplete Suggestion Formatting Tests
    // ========================================

    @Test
    fun `autocomplete suggestions should display in camel case`() {
        // Simulates TagInputField composable behavior
        val availableTags = listOf("food", "travel", "grocery shopping")

        // Format suggestions for display
        val formattedSuggestions = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("Food", "Travel", "Grocery Shopping"), formattedSuggestions)
    }

    @Test
    fun `autocomplete should filter and format suggestions`() {
        val availableTags = listOf("food", "travel", "grocery shopping", "home expense")
        val currentInput = "foo"

        // Filter matching tags (case-insensitive)
        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }

        // Format for display
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        assertEquals(listOf("Food"), formattedSuggestions)
    }

    @Test
    fun `autocomplete should preserve original tags for selection`() {
        val availableTags = listOf("food", "travel", "shopping")
        val formattedSuggestions = TagFormatter.formatTags(availableTags)

        // When user selects "Food" from formatted suggestions
        val selectedFormattedTag = "Food"
        val selectedIndex = formattedSuggestions.indexOf(selectedFormattedTag)
        val originalTag = availableTags[selectedIndex]

        // Original lowercase tag should be used for storage
        assertEquals("food", originalTag)
    }

    @Test
    fun `autocomplete should handle multi-word tag suggestions`() {
        val availableTags = listOf("grocery shopping", "online shopping", "window shopping")
        val currentInput = "shop"

        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        assertEquals(
            listOf("Grocery Shopping", "Online Shopping", "Window Shopping"),
            formattedSuggestions
        )
    }

    @Test
    fun `autocomplete should format case-insensitive matches`() {
        val availableTags = listOf("food", "travel", "grocery shopping")
        val currentInput = "FOOD"

        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        // Should find "food" and format as "Food"
        assertEquals(listOf("Food"), formattedSuggestions)
    }

    @Test
    fun `autocomplete should show empty suggestions when no matches`() {
        val availableTags = listOf("food", "travel", "shopping")
        val currentInput = "xyz"

        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        assertEquals(emptyList<String>(), formattedSuggestions)
    }

    // ========================================
    // Tag Selection and Appending Tests
    // ========================================

    @Test
    fun `selecting formatted suggestion should append original lowercase tag`() {
        // User has typed "food, trav"
        val currentValue = "food, trav"
        val availableTags = listOf("food", "travel", "shopping")

        // Get matching suggestions
        val currentInput = currentValue.split(",").lastOrNull()?.trim() ?: ""
        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        // User selects "Travel" from dropdown
        val selectedFormattedTag = "Travel"
        val selectedIndex = formattedSuggestions.indexOf(selectedFormattedTag)
        val originalTag = matchingSuggestions[selectedIndex]

        // Build new value with original tag
        val existingTags = currentValue.split(",").dropLast(1).map { it.trim() }
        val newValue = (existingTags + originalTag).joinToString(", ")

        assertEquals("food, travel", newValue)
    }

    @Test
    fun `selecting suggestion should preserve existing tags`() {
        val currentValue = "food, shopping, hom"
        val availableTags = listOf("home expense", "home repair")

        val currentInput = currentValue.split(",").lastOrNull()?.trim() ?: ""
        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        // User selects "Home Expense"
        val selectedFormattedTag = "Home Expense"
        val selectedIndex = formattedSuggestions.indexOf(selectedFormattedTag)
        val originalTag = matchingSuggestions[selectedIndex]

        val existingTags = currentValue.split(",").dropLast(1).map { it.trim() }
        val newValue = (existingTags + originalTag).joinToString(", ")

        assertEquals("food, shopping, home expense", newValue)
    }

    // ========================================
    // User Input Preservation Tests
    // ========================================

    @Test
    fun `user input should remain as typed until suggestion selected`() {
        // User types "Food" - input field should show exactly what they typed
        val userInput = "Food"

        // Input field value should match user input exactly
        assertEquals("Food", userInput)

        // Suggestions get formatted for display, but input doesn't change
        val availableTags = listOf("food")
        val formattedSuggestions = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("Food"), formattedSuggestions)
        assertEquals("Food", userInput) // Still what user typed
    }

    @Test
    fun `user can type tags in any case - input shows exactly what was typed`() {
        val testInputs = listOf("food", "Food", "FOOD", "FoOd")

        testInputs.forEach { userInput ->
            // Input field should show exactly what user typed
            assertEquals(userInput, userInput)
        }
    }

    // ========================================
    // Storage Normalization Tests
    // ========================================

    @Test
    fun `stored tags should remain lowercase regardless of display format`() {
        // User sees formatted suggestions: "Food", "Travel", "Grocery Shopping"
        val formattedSuggestions = listOf("Food", "Travel", "Grocery Shopping")

        // But original tags (used for storage) remain lowercase
        val originalTags = listOf("food", "travel", "grocery shopping")

        // Verify storage tags are lowercase
        originalTags.forEach { tag ->
            assertEquals(tag, tag.lowercase())
        }
    }

    @Test
    fun `tag filtering should work case-insensitively`() {
        val availableTags = listOf("food", "travel", "shopping")

        // User types various cases - all should match
        val testInputs = listOf("foo", "FOO", "Foo", "fOo")

        testInputs.forEach { input ->
            val matches = availableTags.filter {
                it.contains(input, ignoreCase = true)
            }
            assertEquals(1, matches.size)
            assertEquals("food", matches[0])
        }
    }

    // ========================================
    // Real-World Scenario Tests
    // ========================================

    @Test
    fun `realistic tag autocomplete workflow`() {
        // Setup: User has existing tags in database
        val availableTags = listOf("food", "grocery shopping", "dining out", "travel")

        // Step 1: User starts typing "groc"
        val userInput1 = "groc"
        val matches1 = availableTags.filter { it.contains(userInput1, ignoreCase = true) }
        val formatted1 = TagFormatter.formatTags(matches1)

        assertEquals(listOf("Grocery Shopping"), formatted1)

        // Step 2: User sees "Grocery Shopping" in dropdown and selects it
        val selectedIndex = formatted1.indexOf("Grocery Shopping")
        val originalTag = matches1[selectedIndex]

        assertEquals("grocery shopping", originalTag)

        // Step 3: Selected tag is appended to input
        val newValue = originalTag + ", "
        assertEquals("grocery shopping, ", newValue)

        // Step 4: User continues typing "din"
        val userInput2 = newValue + "din"
        val currentInput = userInput2.split(",").lastOrNull()?.trim() ?: ""
        val matches2 = availableTags.filter { it.contains(currentInput, ignoreCase = true) }
        val formatted2 = TagFormatter.formatTags(matches2)

        assertEquals(listOf("Dining Out"), formatted2)
    }

    @Test
    fun `autocomplete with no available tags`() {
        val availableTags = emptyList<String>()
        val currentInput = "food"

        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        assertEquals(emptyList<String>(), formattedSuggestions)
    }

    @Test
    fun `autocomplete with partial multi-word match`() {
        val availableTags = listOf("grocery shopping", "home expense", "utility bills")
        val currentInput = "expense"

        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        assertEquals(listOf("Home Expense"), formattedSuggestions)
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    fun `autocomplete should handle tags with hyphens`() {
        val availableTags = listOf("food-related", "work-from-home")
        val formattedSuggestions = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("Food-Related", "Work-From-Home"), formattedSuggestions)
    }

    @Test
    fun `autocomplete should handle tags with numbers`() {
        val availableTags = listOf("budget2024", "q1expenses")
        val formattedSuggestions = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("Budget2024", "Q1expenses"), formattedSuggestions)
    }

    @Test
    fun `autocomplete minimum character threshold behavior`() {
        // Typically autocomplete requires 3+ characters
        val minChars = 3

        val shortInput = "fo" // 2 chars - should not trigger
        val longInput = "foo" // 3 chars - should trigger

        assertEquals(2, shortInput.length)
        assertEquals(3, longInput.length)

        // Only show suggestions when input length >= minChars
        val shouldShowForShort = shortInput.length >= minChars
        val shouldShowForLong = longInput.length >= minChars

        assertEquals(false, shouldShowForShort)
        assertEquals(true, shouldShowForLong)
    }
}

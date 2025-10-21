package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.ui.EditTransactionUiState
import com.axeven.profiteerapp.data.ui.updateTags
import com.axeven.profiteerapp.utils.TagFormatter
import com.axeven.profiteerapp.utils.TagNormalizer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Tests for tag autocomplete formatting in EditTransactionScreen.
 *
 * These tests verify that:
 * 1. Autocomplete suggestions are displayed in camel case
 * 2. Existing tags load correctly (not pre-formatted in input)
 * 3. Edited tags are saved as lowercase
 * 4. EditTransactionUiState normalization logic unchanged
 */
class EditTransactionScreenTagFormattingTest {

    // ========================================
    // Tag Loading from Transaction Tests
    // ========================================

    @Test
    fun `loading existing transaction should preserve tags as-is in input field`() {
        // Transaction has normalized lowercase tags
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Grocery Shopping",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "grocery shopping", "essentials"),
            transactionDate = Date()
        )

        // EditTransactionUiState loads tags into comma-separated string
        val editState = EditTransactionUiState.fromTransaction(transaction)

        // Tags in input field should be normalized (lowercase, comma-separated)
        assertEquals("food, grocery shopping, essentials", editState.tags)
    }

    @Test
    fun `loading transaction with empty tags should show empty string`() {
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Test",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            tags = emptyList(),
            transactionDate = Date()
        )

        val editState = EditTransactionUiState.fromTransaction(transaction)

        assertEquals("", editState.tags)
    }

    // ========================================
    // Autocomplete Suggestion Tests
    // ========================================

    @Test
    fun `autocomplete suggestions should display in camel case`() {
        // User is editing transaction and typing a tag
        val availableTags = listOf("food", "travel", "grocery shopping")

        // Format suggestions for display (same as CreateTransactionScreen)
        val formattedSuggestions = TagFormatter.formatTags(availableTags)

        assertEquals(listOf("Food", "Travel", "Grocery Shopping"), formattedSuggestions)
    }

    @Test
    fun `selecting autocomplete suggestion should append original lowercase tag`() {
        // User is editing tags: "food, trav"
        val currentValue = "food, trav"
        val availableTags = listOf("food", "travel", "shopping")

        // Filter and format suggestions
        val currentInput = currentValue.split(",").lastOrNull()?.trim() ?: ""
        val matchingSuggestions = availableTags.filter {
            it.contains(currentInput, ignoreCase = true)
        }
        val formattedSuggestions = TagFormatter.formatTags(matchingSuggestions)

        // User sees "Travel" and selects it
        val selectedFormattedTag = "Travel"
        val selectedIndex = formattedSuggestions.indexOf(selectedFormattedTag)
        val originalTag = matchingSuggestions[selectedIndex]

        // Append original tag to input
        val existingTags = currentValue.split(",").dropLast(1).map { it.trim() }
        val newValue = (existingTags + originalTag).joinToString(", ")

        assertEquals("food, travel", newValue)
    }

    // ========================================
    // Tag Editing Workflow Tests
    // ========================================

    @Test
    fun `editing existing tags should maintain lowercase normalization`() {
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Test",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "shopping"),
            transactionDate = Date()
        )

        var editState = EditTransactionUiState.fromTransaction(transaction)

        // User adds another tag via autocomplete: "travel"
        editState = editState.updateTags("food, shopping, travel")

        // Tags in state remain as user typed (will be normalized on save)
        assertEquals("food, shopping, travel", editState.tags)

        // Parse tags for saving (normalization happens here)
        val parsedTags = TagNormalizer.parseTagInput(editState.tags)
        assertEquals(listOf("food", "shopping", "travel"), parsedTags)
    }

    @Test
    fun `removing tags from existing transaction should work correctly`() {
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Test",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "shopping", "travel"),
            transactionDate = Date()
        )

        var editState = EditTransactionUiState.fromTransaction(transaction)
        assertEquals("food, shopping, travel", editState.tags)

        // User removes "shopping"
        editState = editState.updateTags("food, travel")

        assertEquals("food, travel", editState.tags)
    }

    @Test
    fun `clearing all tags should result in empty tags list`() {
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Test",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "shopping"),
            transactionDate = Date()
        )

        var editState = EditTransactionUiState.fromTransaction(transaction)

        // User clears all tags
        editState = editState.updateTags("")

        assertEquals("", editState.tags)

        val parsedTags = TagNormalizer.parseTagInput(editState.tags)
        assertEquals(emptyList<String>(), parsedTags)
    }

    // ========================================
    // EditTransactionUiState Normalization Tests
    // ========================================

    @Test
    fun `EditTransactionUiState should normalize tags on creation from transaction`() {
        // Transaction might have inconsistent casing (edge case)
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Test",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "TRAVEL", "Shopping"),
            transactionDate = Date()
        )

        val editState = EditTransactionUiState.fromTransaction(transaction)

        // Tags should be normalized to lowercase and joined
        val expectedTags = "food, travel, shopping"
        assertEquals(expectedTags, editState.tags)
    }

    @Test
    fun `EditTransactionUiState should filter out Untagged keyword`() {
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Test",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "Untagged", "travel"),
            transactionDate = Date()
        )

        val editState = EditTransactionUiState.fromTransaction(transaction)

        // "Untagged" should be filtered out by normalization
        assertEquals("food, travel", editState.tags)
    }

    @Test
    fun `EditTransactionUiState should deduplicate tags`() {
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Test",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "Food", "FOOD", "travel"),
            transactionDate = Date()
        )

        val editState = EditTransactionUiState.fromTransaction(transaction)

        // Duplicates should be removed by normalization
        assertEquals("food, travel", editState.tags)
    }

    // ========================================
    // Integration with TagInputField Tests
    // ========================================

    @Test
    fun `TagInputField receives normalized tags from EditTransactionUiState`() {
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Test",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            tags = listOf("grocery shopping", "food"),
            transactionDate = Date()
        )

        val editState = EditTransactionUiState.fromTransaction(transaction)

        // TagInputField value prop receives this string
        val tagInputValue = editState.tags

        assertEquals("grocery shopping, food", tagInputValue)

        // User will see "grocery shopping, food" in input field
        // Autocomplete suggestions will be formatted as "Grocery Shopping", "Food"
    }

    @Test
    fun `updating tags through TagInputField maintains state consistency`() {
        var editState = EditTransactionUiState.fromTransaction(
            Transaction(
                id = "test-1",
                userId = "user-1",
                title = "Test",
                amount = 10.0,
                type = TransactionType.EXPENSE,
                tags = listOf("food"),
                transactionDate = Date()
            )
        )

        // User types additional tags
        editState = editState.updateTags("food, travel, shopping")

        assertEquals("food, travel, shopping", editState.tags)
    }

    // ========================================
    // Real-World Edit Scenarios
    // ========================================

    @Test
    fun `realistic edit workflow - modifying existing transaction tags`() {
        // Step 1: Load transaction
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Restaurant",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "dining out"),
            transactionDate = Date()
        )

        var editState = EditTransactionUiState.fromTransaction(transaction)
        assertEquals("food, dining out", editState.tags)

        // Step 2: User adds "entertainment" tag via autocomplete
        val availableTags = listOf("food", "dining out", "entertainment", "travel")
        val currentInput = "enter"
        val matches = availableTags.filter { it.contains(currentInput, ignoreCase = true) }
        val formatted = TagFormatter.formatTags(matches)

        assertEquals(listOf("Entertainment"), formatted)

        // Step 3: User selects "Entertainment"
        val selectedIndex = formatted.indexOf("Entertainment")
        val originalTag = matches[selectedIndex]

        editState = editState.updateTags("food, dining out, $originalTag")

        assertEquals("food, dining out, entertainment", editState.tags)

        // Step 4: Tags are normalized for saving
        val finalTags = TagNormalizer.parseTagInput(editState.tags)
        assertEquals(listOf("food", "dining out", "entertainment"), finalTags)
    }

    @Test
    fun `realistic edit workflow - replacing all tags`() {
        val transaction = Transaction(
            id = "test-1",
            userId = "user-1",
            title = "Purchase",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            tags = listOf("shopping", "clothes"),
            transactionDate = Date()
        )

        var editState = EditTransactionUiState.fromTransaction(transaction)
        assertEquals("shopping, clothes", editState.tags)

        // User decides to change category completely
        editState = editState.updateTags("electronics, gadgets")

        assertEquals("electronics, gadgets", editState.tags)

        val finalTags = TagNormalizer.parseTagInput(editState.tags)
        assertEquals(listOf("electronics", "gadgets"), finalTags)
    }

    // ========================================
    // Validation and Error Handling Tests
    // ========================================

    @Test
    fun `tag validation should work with formatted suggestions`() {
        var editState = EditTransactionUiState.fromTransaction(
            Transaction(
                id = "test-1",
                userId = "user-1",
                title = "Test",
                amount = 10.0,
                type = TransactionType.EXPENSE,
                tags = emptyList(),
                transactionDate = Date()
            )
        )

        // Valid tags (optional field)
        editState = editState.updateTags("food, travel")
        assertEquals(null, editState.validationErrors.tagsError)

        // Empty tags also valid
        editState = editState.updateTags("")
        assertEquals(null, editState.validationErrors.tagsError)
    }

    @Test
    fun `tags field should show formatted suggestions regardless of input case`() {
        val availableTags = listOf("food", "travel", "grocery shopping")

        // User types different cases
        val testInputs = listOf("FOO", "foo", "Foo", "fOo")

        testInputs.forEach { input ->
            val matches = availableTags.filter { it.contains(input, ignoreCase = true) }
            val formatted = TagFormatter.formatTags(matches)

            // All should find and format "food" as "Food"
            assertEquals(listOf("Food"), formatted)
        }
    }
}

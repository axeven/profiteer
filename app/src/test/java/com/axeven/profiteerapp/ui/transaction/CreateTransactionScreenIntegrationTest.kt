package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Integration tests for CreateTransactionScreen with consolidated state management.
 *
 * Following TDD methodology - these tests define the expected behavior for the
 * migrated screen that uses consolidated state instead of scattered mutableStateOf variables.
 *
 * These tests will initially fail (RED phase) and will be made to pass during
 * the screen migration (GREEN phase).
 */
class CreateTransactionScreenIntegrationTest {

    // Mock data for testing
    private val mockPhysicalWallet = Wallet(
        id = "physical-1",
        name = "Main Account",
        walletType = "Physical",
        balance = 1000.0
    )

    private val mockLogicalWallet = Wallet(
        id = "logical-1",
        name = "Savings Goal",
        walletType = "Logical",
        balance = 500.0
    )

    private val mockPhysicalWallet2 = Wallet(
        id = "physical-2",
        name = "Backup Account",
        walletType = "Physical",
        balance = 200.0
    )


    // Tests for state consolidation behavior
    @Test
    fun `screen should use single CreateTransactionUiState instead of multiple mutableStateOf`() {
        // This test verifies that the screen uses consolidated state management
        // The actual implementation will be checked during migration

        val initialState = CreateTransactionUiState()

        // Verify initial state properties
        assertEquals("", initialState.title)
        assertEquals("", initialState.amount)
        assertEquals(TransactionType.EXPENSE, initialState.selectedType)
        assertEquals(SelectedWallets(), initialState.selectedWallets)
        assertEquals(DialogStates(), initialState.dialogStates)
        assertEquals(ValidationErrors(), initialState.validationErrors)
        assertFalse(initialState.isFormValid)
    }

    @Test
    fun `screen should update state immutably through state manager functions`() {
        val initialState = CreateTransactionUiState()

        // Test title update
        val titleUpdatedState = updateTitle(initialState, "Test Transaction")
        assertNotSame(initialState, titleUpdatedState)
        assertEquals("Test Transaction", titleUpdatedState.title)
        assertEquals("", initialState.title) // Original unchanged

        // Test amount update
        val amountUpdatedState = updateAmount(titleUpdatedState, "100.50")
        assertEquals("100.50", amountUpdatedState.amount)
        assertEquals("Test Transaction", amountUpdatedState.title) // Previous change preserved

        // Test wallet update
        val walletUpdatedState = updatePhysicalWallet(amountUpdatedState, mockPhysicalWallet)
        assertEquals(mockPhysicalWallet, walletUpdatedState.selectedWallets.physical)
        assertEquals("Test Transaction", walletUpdatedState.title) // Previous changes preserved
        assertEquals("100.50", walletUpdatedState.amount) // Previous changes preserved
    }

    @Test
    fun `screen should validate state automatically on updates`() {
        var state = CreateTransactionUiState().updateAndValidate()

        // Initially invalid (empty title, amount, no wallets)
        assertFalse(state.isFormValid)
        assertNotNull(state.validationErrors.titleError)
        assertNotNull(state.validationErrors.amountError)
        assertNotNull(state.validationErrors.walletError)

        // Add title - should clear title error
        state = updateTitle(state, "Valid Transaction")
        assertNull(state.validationErrors.titleError)
        assertNotNull(state.validationErrors.amountError) // Still invalid
        assertNotNull(state.validationErrors.walletError) // Still invalid

        // Add amount - should clear amount error
        state = updateAmount(state, "100.00")
        assertNull(state.validationErrors.titleError)
        assertNull(state.validationErrors.amountError)
        assertNotNull(state.validationErrors.walletError) // Still invalid

        // Add wallet - should clear wallet error and make form valid
        state = updatePhysicalWallet(state, mockPhysicalWallet)
        assertNull(state.validationErrors.titleError)
        assertNull(state.validationErrors.amountError)
        assertNull(state.validationErrors.walletError)
        assertTrue(state.isFormValid)
    }

    @Test
    fun `screen should handle transaction type changes correctly`() {
        var state = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets(
                physical = mockPhysicalWallet,
                logical = mockLogicalWallet
            )
        )

        // Switch to transfer - should preserve regular wallets
        state = updateTransactionType(state, TransactionType.TRANSFER)
        assertEquals(TransactionType.TRANSFER, state.selectedType)
        assertEquals(mockPhysicalWallet, state.selectedWallets.physical)
        assertEquals(mockLogicalWallet, state.selectedWallets.logical)
        assertNull(state.selectedWallets.source)
        assertNull(state.selectedWallets.destination)

        // Add transfer wallets
        state = updateSourceWallet(state, mockPhysicalWallet)
        state = updateDestinationWallet(state, mockPhysicalWallet2)
        assertEquals(mockPhysicalWallet, state.selectedWallets.source)
        assertEquals(mockPhysicalWallet2, state.selectedWallets.destination)

        // Switch back to expense - should clear transfer wallets
        state = updateTransactionType(state, TransactionType.EXPENSE)
        assertEquals(TransactionType.EXPENSE, state.selectedType)
        assertNull(state.selectedWallets.source)
        assertNull(state.selectedWallets.destination)
        assertEquals(mockPhysicalWallet, state.selectedWallets.physical) // Regular wallets preserved
        assertEquals(mockLogicalWallet, state.selectedWallets.logical)
    }

    @Test
    fun `screen should manage dialog states correctly`() {
        var state = CreateTransactionUiState()

        // Open date picker - should close all others
        state = openDialog(state, DialogType.DATE_PICKER)
        assertTrue(state.dialogStates.showDatePicker)
        assertFalse(state.dialogStates.showPhysicalWalletPicker)
        assertFalse(state.dialogStates.showLogicalWalletPicker)

        // Open physical wallet picker - should close date picker
        state = openDialog(state, DialogType.PHYSICAL_WALLET)
        assertFalse(state.dialogStates.showDatePicker)
        assertTrue(state.dialogStates.showPhysicalWalletPicker)
        assertFalse(state.dialogStates.showLogicalWalletPicker)

        // Close all dialogs
        state = closeAllDialogs(state)
        assertFalse(state.dialogStates.showDatePicker)
        assertFalse(state.dialogStates.showPhysicalWalletPicker)
        assertFalse(state.dialogStates.showLogicalWalletPicker)
        assertFalse(state.dialogStates.showSourceWalletPicker)
        assertFalse(state.dialogStates.showDestinationWalletPicker)
    }

    @Test
    fun `screen should handle wallet selection with dialog closing`() {
        var state = CreateTransactionUiState(
            dialogStates = DialogStates(showPhysicalWalletPicker = true)
        )

        // Select physical wallet - should close dialog and update wallet
        state = updatePhysicalWallet(state, mockPhysicalWallet)
        assertEquals(mockPhysicalWallet, state.selectedWallets.physical)
        assertFalse(state.dialogStates.showPhysicalWalletPicker)

        // Open logical wallet picker
        state = openDialog(state, DialogType.LOGICAL_WALLET)
        assertTrue(state.dialogStates.showLogicalWalletPicker)

        // Select logical wallet - should close dialog and update wallet
        state = updateLogicalWallet(state, mockLogicalWallet)
        assertEquals(mockLogicalWallet, state.selectedWallets.logical)
        assertFalse(state.dialogStates.showLogicalWalletPicker)
        assertEquals(mockPhysicalWallet, state.selectedWallets.physical) // Previous selection preserved
    }

    @Test
    fun `screen should validate transfer transactions correctly`() {
        var state = CreateTransactionUiState(
            title = "Transfer Test",
            amount = "100.00",
            selectedType = TransactionType.TRANSFER
        ).updateAndValidate()

        // Invalid initially - no source/destination wallets
        assertFalse(state.isFormValid)
        assertNotNull(state.validationErrors.transferError)

        // Add source wallet - still invalid
        state = updateSourceWallet(state, mockPhysicalWallet)
        assertFalse(state.isFormValid)
        assertNotNull(state.validationErrors.transferError)

        // Add destination wallet of same type - should be valid
        state = updateDestinationWallet(state, mockPhysicalWallet2)
        assertTrue(state.isFormValid)
        assertNull(state.validationErrors.transferError)

        // Change destination to different type - should be invalid
        state = updateDestinationWallet(state, mockLogicalWallet)
        assertFalse(state.isFormValid)
        assertNotNull(state.validationErrors.transferError)
        assertTrue(state.validationErrors.transferError!!.contains("same type"))
    }

    @Test
    fun `screen should handle date selection with dialog management`() {
        val testDate = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000) // Tomorrow
        var state = CreateTransactionUiState()

        // Open date picker
        state = openDialog(state, DialogType.DATE_PICKER)
        assertTrue(state.dialogStates.showDatePicker)

        // Select date - should close dialog and update date
        state = updateSelectedDate(state, testDate)
        assertEquals(testDate, state.selectedDate)
        assertFalse(state.dialogStates.showDatePicker)
    }

    @Test
    fun `screen should handle tags input correctly`() {
        var state = CreateTransactionUiState()

        // Add tags
        state = updateTags(state, "food, grocery, household")
        assertEquals("food, grocery, household", state.tags)

        // Tags are normalized: duplicates are automatically removed
        state = updateTags(state, "tag1, tag2, tag1") // Duplicate tag
        assertEquals("tag1, tag2", state.tags) // Duplicates removed by normalization
    }

    @Test
    fun `screen should provide transaction summary for submission`() {
        var state = CreateTransactionUiState(
            title = "Grocery Shopping",
            amount = "85.50",
            selectedType = TransactionType.EXPENSE,
            tags = "food, grocery"
        )

        state = updatePhysicalWallet(state, mockPhysicalWallet)
        state = updateLogicalWallet(state, mockLogicalWallet)

        val summary = getTransactionSummary(state)

        assertEquals("Grocery Shopping", summary.title)
        assertEquals(85.50, summary.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, summary.type)
        assertEquals(2, summary.wallets.size)
        assertTrue(summary.wallets.contains(mockPhysicalWallet))
        assertTrue(summary.wallets.contains(mockLogicalWallet))
        assertEquals(listOf("food", "grocery"), summary.tags)
        assertTrue(summary.isValid)
    }

    @Test
    fun `screen should handle pre-selected wallet initialization`() {
        // Test pre-selection through state factory method
        val stateWithPreSelection = CreateTransactionUiState.withPreSelectedWallet(
            walletId = mockPhysicalWallet.id,
            walletType = mockPhysicalWallet.walletType,
            initialTransactionType = TransactionType.INCOME
        )

        assertEquals(TransactionType.INCOME, stateWithPreSelection.selectedType)
        // Note: Actual wallet pre-selection will be implemented when repository is available
    }

    @Test
    fun `screen should maintain performance with consolidated state`() {
        val initialState = CreateTransactionUiState()

        // Measure performance of state updates
        val startTime = System.currentTimeMillis()

        var state = initialState
        for (i in 1..100) {
            state = updateTitle(state, "Title $i")
            state = updateAmount(state, "$i.00")
            state = updateTransactionType(state, TransactionType.values()[i % 3])
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete 300 state updates in under 50ms
        assertTrue("State updates should be fast, took ${duration}ms", duration < 50)

        // Verify final state
        assertEquals("Title 100", state.title)
        assertEquals("100.00", state.amount)
    }

    @Test
    fun `screen should handle complex transaction creation flow`() {
        var state = CreateTransactionUiState().updateAndValidate()

        // Start with invalid state
        assertFalse(state.isFormValid)

        // Step 1: Set transaction type
        state = updateTransactionType(state, TransactionType.EXPENSE)
        assertEquals(TransactionType.EXPENSE, state.selectedType)

        // Step 2: Enter title
        state = updateTitle(state, "Grocery Shopping")
        assertEquals("Grocery Shopping", state.title)
        assertNull(state.validationErrors.titleError)

        // Step 3: Enter amount
        state = updateAmount(state, "125.75")
        assertEquals("125.75", state.amount)
        assertNull(state.validationErrors.amountError)

        // Step 4: Select physical wallet
        state = updatePhysicalWallet(state, mockPhysicalWallet)
        assertEquals(mockPhysicalWallet, state.selectedWallets.physical)

        // Step 5: Select logical wallet
        state = updateLogicalWallet(state, mockLogicalWallet)
        assertEquals(mockLogicalWallet, state.selectedWallets.logical)
        assertNull(state.validationErrors.walletError)

        // Step 6: Add tags
        state = updateTags(state, "food, grocery, household")
        assertEquals("food, grocery, household", state.tags)

        // Step 7: Set custom date
        val customDate = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000) // Yesterday
        state = updateSelectedDate(state, customDate)
        assertEquals(customDate, state.selectedDate)

        // Final validation - should be completely valid
        assertTrue(state.isFormValid)
        assertFalse(state.validationErrors.hasErrors)

        // Generate summary for submission
        val summary = getTransactionSummary(state)
        assertTrue(summary.isValid)
        assertEquals("Grocery Shopping", summary.title)
        assertEquals(125.75, summary.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, summary.type)
        assertEquals(2, summary.wallets.size)
        assertEquals(listOf("food", "grocery", "household"), summary.tags)
        assertEquals(customDate, summary.date)
    }

    @Test
    fun `screen should handle transfer transaction creation flow`() {
        var state = CreateTransactionUiState().updateAndValidate()

        // Step 1: Set transfer type
        state = updateTransactionType(state, TransactionType.TRANSFER)
        assertEquals(TransactionType.TRANSFER, state.selectedType)

        // Step 2: Enter title and amount
        state = updateTitle(state, "Move to Savings")
        state = updateAmount(state, "500.00")

        // Step 3: Select source wallet
        state = updateSourceWallet(state, mockPhysicalWallet)
        assertEquals(mockPhysicalWallet, state.selectedWallets.source)

        // Step 4: Select destination wallet of same type
        state = updateDestinationWallet(state, mockPhysicalWallet2)
        assertEquals(mockPhysicalWallet2, state.selectedWallets.destination)

        // Should be valid for transfer
        assertTrue(state.isFormValid)
        assertNull(state.validationErrors.transferError)

        // Generate transfer summary
        val summary = getTransactionSummary(state)
        assertTrue(summary.isValid)
        assertEquals("Move to Savings", summary.title)
        assertEquals(500.00, summary.amount, 0.01)
        assertEquals(TransactionType.TRANSFER, summary.type)
        assertEquals(2, summary.wallets.size)
        assertTrue(summary.wallets.contains(mockPhysicalWallet))
        assertTrue(summary.wallets.contains(mockPhysicalWallet2))
        assertTrue(summary.tags.isEmpty()) // Transfers don't have tags
    }

    @Test
    fun `screen should preserve state across dialog interactions`() {
        var state = CreateTransactionUiState(
            title = "Test Transaction",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE
        )

        // Open and close dialogs - should preserve form data
        state = openDialog(state, DialogType.DATE_PICKER)
        assertTrue(state.dialogStates.showDatePicker)
        assertEquals("Test Transaction", state.title) // Form data preserved
        assertEquals("100.00", state.amount)

        state = closeAllDialogs(state)
        assertFalse(state.dialogStates.showDatePicker)
        assertEquals("Test Transaction", state.title) // Form data still preserved
        assertEquals("100.00", state.amount)

        // Open wallet picker and select wallet
        state = openDialog(state, DialogType.PHYSICAL_WALLET)
        state = updatePhysicalWallet(state, mockPhysicalWallet)

        assertEquals("Test Transaction", state.title) // Form data still preserved
        assertEquals("100.00", state.amount)
        assertEquals(mockPhysicalWallet, state.selectedWallets.physical)
        assertFalse(state.dialogStates.showPhysicalWalletPicker) // Dialog closed
    }

    // ========================================
    // Tag Normalization Integration Tests (Phase 5)
    // ========================================

    @Test
    fun `user enters duplicate tags with different cases - saves as single normalized tag`() {
        // Simulate user entering "Food, food, FOOD" in the tags field
        val initialState = CreateTransactionUiState()

        val stateWithTags = updateTags(initialState, "Food, food, FOOD")

        // Verify that all duplicates are removed and normalized to lowercase
        assertEquals("food", stateWithTags.tags)
    }

    @Test
    fun `user enters tags with whitespace - saves with trimmed tags`() {
        // Simulate user entering " travel " in the tags field
        val initialState = CreateTransactionUiState()

        val stateWithTags = updateTags(initialState, " travel ")

        // Verify that whitespace is trimmed
        assertEquals("travel", stateWithTags.tags)
    }

    @Test
    fun `user enters multiple tags with mixed case and whitespace - normalizes correctly`() {
        // Simulate user entering " Food , TRAVEL, Shopping "
        val initialState = CreateTransactionUiState()

        val stateWithTags = updateTags(initialState, " Food , TRAVEL, Shopping ")

        // Verify that tags are normalized and properly formatted
        assertEquals("food, travel, shopping", stateWithTags.tags)
    }

    @Test
    fun `user enters Untagged keyword - filters it out`() {
        // Simulate user entering "food, Untagged, travel"
        val initialState = CreateTransactionUiState()

        val stateWithTags = updateTags(initialState, "food, Untagged, travel")

        // Verify that "Untagged" is filtered out
        assertEquals("food, travel", stateWithTags.tags)
    }

    @Test
    fun `user enters empty and blank tags - filters them out`() {
        // Simulate user entering "food,  , travel, , shopping"
        val initialState = CreateTransactionUiState()

        val stateWithTags = updateTags(initialState, "food,  , travel, , shopping")

        // Verify that blank tags are filtered out
        assertEquals("food, travel, shopping", stateWithTags.tags)
    }

    @Test
    fun `user enters only blank tags - results in empty tag string`() {
        // Simulate user entering "  ,  , "
        val initialState = CreateTransactionUiState()

        val stateWithTags = updateTags(initialState, "  ,  , ")

        // Verify that result is empty
        assertEquals("", stateWithTags.tags)
    }

    @Test
    fun `getTransactionSummary returns normalized tags`() {
        // Create state with non-normalized tags
        val state = CreateTransactionUiState(
            title = "Test Transaction",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE,
            tags = "Food, TRAVEL, shopping",
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        // Get transaction summary
        val summary = getTransactionSummary(state)

        // Verify that tags are normalized in the summary
        assertEquals(listOf("food", "travel", "shopping"), summary.tags)
    }

    @Test
    fun `getTransactionSummary filters out Untagged keyword`() {
        // Create state with "Untagged" in tags
        val state = CreateTransactionUiState(
            title = "Test Transaction",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE,
            tags = "food, Untagged, travel",
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        // Get transaction summary
        val summary = getTransactionSummary(state)

        // Verify that "Untagged" is not in the summary
        assertEquals(listOf("food", "travel"), summary.tags)
        assertFalse(summary.tags.contains("untagged"))
    }

    @Test
    fun `getTransactionSummary handles empty tags`() {
        // Create state with empty tags
        val state = CreateTransactionUiState(
            title = "Test Transaction",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE,
            tags = "",
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        // Get transaction summary
        val summary = getTransactionSummary(state)

        // Verify that tags list is empty
        assertEquals(emptyList<String>(), summary.tags)
    }

    @Test
    fun `fromExistingTransaction normalizes loaded tags`() {
        // Simulate loading a transaction with non-normalized tags
        val state = CreateTransactionUiState.fromExistingTransaction(
            title = "Existing Transaction",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            tags = listOf("Food", "TRAVEL", "shopping"),
            date = Date()
        )

        // Verify that loaded tags are normalized
        assertEquals("food, travel, shopping", state.tags)
    }

    @Test
    fun `fromExistingTransaction filters out Untagged from loaded tags`() {
        // Simulate loading a transaction with "Untagged" in tags
        val state = CreateTransactionUiState.fromExistingTransaction(
            title = "Existing Transaction",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "Untagged", "travel"),
            date = Date()
        )

        // Verify that "Untagged" is filtered out
        assertEquals("food, travel", state.tags)
        assertFalse(state.tags.contains("untagged"))
    }

    @Test
    fun `fromExistingTransaction handles duplicate tags`() {
        // Simulate loading a transaction with duplicate tags
        val state = CreateTransactionUiState.fromExistingTransaction(
            title = "Existing Transaction",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "Food", "FOOD", "travel"),
            date = Date()
        )

        // Verify that duplicates are removed
        assertEquals("food, travel", state.tags)
    }

    @Test
    fun `complex user flow - enter mixed tags, edit, and save`() {
        // Step 1: User enters initial tags
        val initialState = CreateTransactionUiState()
        val step1 = updateTags(initialState, " Food , TRAVEL, Shopping ")
        assertEquals("food, travel, shopping", step1.tags)

        // Step 2: User edits to add duplicate with different case
        val step2 = updateTags(step1, "food, travel, shopping, FOOD")
        assertEquals("food, travel, shopping", step2.tags)

        // Step 3: User adds new tag with whitespace
        val step3 = updateTags(step2, "food, travel, shopping,  groceries ")
        assertEquals("food, travel, shopping, groceries", step3.tags)

        // Step 4: Get final transaction summary
        val summary = getTransactionSummary(
            step3.copy(
                title = "Test",
                amount = "100.00",
                selectedType = TransactionType.EXPENSE,
                selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
            )
        )
        assertEquals(listOf("food", "travel", "shopping", "groceries"), summary.tags)
    }

    @Test
    fun `user accidentally enters Untagged multiple times - all filtered out`() {
        val initialState = CreateTransactionUiState()

        val stateWithTags = updateTags(initialState, "food, Untagged, travel, UNTAGGED, untagged")

        // Verify that all variations of "Untagged" are filtered out
        assertEquals("food, travel", stateWithTags.tags)
    }

    @Test
    fun `user enters extremely long tag list with duplicates - normalizes efficiently`() {
        val initialState = CreateTransactionUiState()

        // Simulate user entering many tags with duplicates
        val tagInput = "food, Food, FOOD, travel, TRAVEL, shopping, Shopping, " +
                       "groceries, GROCERIES, transport, Transport, entertainment, ENTERTAINMENT"

        val stateWithTags = updateTags(initialState, tagInput)

        // Verify that duplicates are removed
        val expectedTags = "food, travel, shopping, groceries, transport, entertainment"
        assertEquals(expectedTags, stateWithTags.tags)
    }

    @Test
    fun `user clears all tags - results in empty string`() {
        // Start with tags
        val initialState = CreateTransactionUiState(tags = "food, travel, shopping")

        // User clears all tags
        val clearedState = updateTags(initialState, "")

        // Verify that tags are empty
        assertEquals("", clearedState.tags)
    }

    @Test
    fun `unicode and special characters in tags - normalized correctly`() {
        val initialState = CreateTransactionUiState()

        val stateWithTags = updateTags(initialState, " Café , CAFÉ, café ")

        // Verify that unicode characters are normalized (lowercase) and duplicates removed
        assertEquals("café", stateWithTags.tags)
    }
}
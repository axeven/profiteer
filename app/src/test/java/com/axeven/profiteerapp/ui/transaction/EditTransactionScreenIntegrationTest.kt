package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Integration tests for EditTransactionScreen with consolidated state management.
 *
 * Following TDD methodology - these tests define the expected behavior for the
 * migrated edit screen that uses consolidated state instead of scattered mutableStateOf variables.
 *
 * These tests will initially fail (RED phase) and will be made to pass during
 * the screen migration (GREEN phase).
 */
class EditTransactionScreenIntegrationTest {

    // Mock data for testing
    private val mockPhysicalWallet = Wallet(
        id = "physical-1",
        name = "Main Account",
        balance = 1000.0,
        walletType = "Physical"
    )

    private val mockLogicalWallet = Wallet(
        id = "logical-1",
        name = "Savings Goal",
        balance = 500.0,
        walletType = "Logical"
    )

    private val mockPhysicalWallet2 = Wallet(
        id = "physical-2",
        name = "Backup Account",
        balance = 200.0,
        walletType = "Physical"
    )

    private val mockExpenseTransaction = Transaction(
        id = "expense-1",
        title = "Grocery Shopping",
        amount = -85.50,
        category = "Food",
        type = TransactionType.EXPENSE,
        affectedWalletIds = listOf("physical-1", "logical-1"),
        tags = listOf("food", "grocery"),
        transactionDate = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    )

    private val mockTransferTransaction = Transaction(
        id = "transfer-1",
        title = "Move to Savings",
        amount = 200.0,
        category = "Transfer",
        type = TransactionType.TRANSFER,
        sourceWalletId = "physical-1",
        destinationWalletId = "physical-2",
        transactionDate = Date()
    )

    // Tests for state consolidation behavior
    @Test
    fun `screen should use single EditTransactionUiState instead of multiple mutableStateOf`() {
        // This test verifies that the screen uses consolidated state management
        // The actual implementation will be checked during migration

        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Verify consolidated state properties
        assertEquals("Grocery Shopping", editState.title)
        assertEquals("85.50", editState.amount)
        assertEquals(TransactionType.EXPENSE, editState.selectedType)
        assertEquals(mockPhysicalWallet, editState.selectedWallets.physical)
        assertEquals(mockLogicalWallet, editState.selectedWallets.logical)
        assertEquals("food, grocery", editState.tags)
        assertFalse(editState.deletionRequested)
        assertEquals(mockExpenseTransaction, editState.originalTransaction)
    }

    @Test
    fun `screen should initialize from existing transaction correctly`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet, mockPhysicalWallet2)

        // Test expense transaction initialization
        val expenseState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        assertEquals("Grocery Shopping", expenseState.title)
        assertEquals("85.50", expenseState.amount)
        assertEquals(TransactionType.EXPENSE, expenseState.selectedType)
        assertEquals(mockPhysicalWallet, expenseState.selectedWallets.physical)
        assertEquals(mockLogicalWallet, expenseState.selectedWallets.logical)
        assertEquals("food, grocery", expenseState.tags)
        assertTrue(expenseState.isFormValid)

        // Test transfer transaction initialization
        val transferState = EditTransactionUiState.fromTransaction(
            transaction = mockTransferTransaction,
            availableWallets = availableWallets
        )

        assertEquals("Move to Savings", transferState.title)
        assertEquals("200.00", transferState.amount)
        assertEquals(TransactionType.TRANSFER, transferState.selectedType)
        assertEquals(mockPhysicalWallet, transferState.selectedWallets.source)
        assertEquals(mockPhysicalWallet2, transferState.selectedWallets.destination)
        assertEquals("", transferState.tags) // Transfers don't have tags
        assertTrue(transferState.isFormValid)
    }

    @Test
    fun `screen should update state immutably through state manager functions`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val initialState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Test title update
        val titleUpdatedState = initialState.updateTitle("Updated Grocery Shopping")
        assertNotSame(initialState, titleUpdatedState)
        assertEquals("Updated Grocery Shopping", titleUpdatedState.title)
        assertEquals("Grocery Shopping", initialState.title) // Original unchanged
        assertTrue(titleUpdatedState.hasChanges)

        // Test amount update
        val amountUpdatedState = titleUpdatedState.updateAmount("95.75")
        assertEquals("95.75", amountUpdatedState.amount)
        assertEquals("Updated Grocery Shopping", amountUpdatedState.title) // Previous change preserved
        assertTrue(amountUpdatedState.hasChanges)

        // Test wallet update
        val walletUpdatedState = amountUpdatedState.updateLogicalWallet(null)
        assertNull(walletUpdatedState.selectedWallets.logical)
        assertEquals("Updated Grocery Shopping", walletUpdatedState.title) // Previous changes preserved
        assertEquals("95.75", walletUpdatedState.amount) // Previous changes preserved
        assertTrue(walletUpdatedState.hasChanges)
    }

    @Test
    fun `screen should validate state automatically on updates`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Initially valid (from transaction)
        assertTrue(editState.isFormValid)
        assertFalse(editState.validationErrors.hasErrors)

        // Make invalid changes
        editState = editState.updateTitle("") // Invalid empty title
        assertFalse(editState.isFormValid)
        assertTrue(editState.validationErrors.hasErrors)
        assertNotNull(editState.validationErrors.titleError)

        editState = editState.updateAmount("invalid") // Invalid amount
        assertFalse(editState.isFormValid)
        assertNotNull(editState.validationErrors.amountError)

        // Fix the errors
        editState = editState.updateTitle("Fixed Title")
        editState = editState.updateAmount("100.00")

        assertTrue(editState.isFormValid)
        assertFalse(editState.validationErrors.hasErrors)
        assertNull(editState.validationErrors.titleError)
        assertNull(editState.validationErrors.amountError)
    }

    @Test
    fun `screen should prevent transaction type changes in edit mode`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Attempt to change transaction type (should be restricted)
        val typeChangeAttempt = editState.updateTransactionType(TransactionType.INCOME)
        assertEquals(TransactionType.EXPENSE, typeChangeAttempt.selectedType) // Should remain unchanged

        val transferChangeAttempt = editState.updateTransactionType(TransactionType.TRANSFER)
        assertEquals(TransactionType.EXPENSE, transferChangeAttempt.selectedType) // Should remain unchanged
    }

    @Test
    fun `screen should track deletion state correctly`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Initially no deletion requested
        assertFalse(editState.deletionRequested)

        // Request deletion
        editState = editState.requestDeletion()
        assertTrue(editState.deletionRequested)
        assertEquals("Grocery Shopping", editState.title) // Other state preserved

        // Cancel deletion
        editState = editState.cancelDeletion()
        assertFalse(editState.deletionRequested)
        assertEquals("Grocery Shopping", editState.title) // Other state still preserved
    }

    @Test
    fun `screen should manage dialog states correctly`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Open date picker - should close all others
        editState = editState.openDialog(DialogType.DATE_PICKER)
        assertTrue(editState.dialogStates.showDatePicker)
        assertFalse(editState.dialogStates.showPhysicalWalletPicker)
        assertFalse(editState.dialogStates.showLogicalWalletPicker)

        // Open physical wallet picker - should close date picker
        editState = editState.openDialog(DialogType.PHYSICAL_WALLET)
        assertFalse(editState.dialogStates.showDatePicker)
        assertTrue(editState.dialogStates.showPhysicalWalletPicker)
        assertFalse(editState.dialogStates.showLogicalWalletPicker)

        // Close all dialogs
        editState = editState.closeAllDialogs()
        assertFalse(editState.dialogStates.showDatePicker)
        assertFalse(editState.dialogStates.showPhysicalWalletPicker)
        assertFalse(editState.dialogStates.showLogicalWalletPicker)
        assertFalse(editState.dialogStates.showSourceWalletPicker)
        assertFalse(editState.dialogStates.showDestinationWalletPicker)
    }

    @Test
    fun `screen should handle wallet selection with dialog closing`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet, mockPhysicalWallet2)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Open physical wallet picker
        editState = editState.openDialog(DialogType.PHYSICAL_WALLET)
        assertTrue(editState.dialogStates.showPhysicalWalletPicker)

        // Select different physical wallet - should close dialog and update wallet
        editState = editState.updatePhysicalWallet(mockPhysicalWallet2)
        assertEquals(mockPhysicalWallet2, editState.selectedWallets.physical)
        assertFalse(editState.dialogStates.showPhysicalWalletPicker)
        assertTrue(editState.hasChanges) // Wallet change should be detected

        // Open logical wallet picker
        editState = editState.openDialog(DialogType.LOGICAL_WALLET)
        assertTrue(editState.dialogStates.showLogicalWalletPicker)

        // Clear logical wallet - should close dialog and update wallet
        editState = editState.updateLogicalWallet(null)
        assertNull(editState.selectedWallets.logical)
        assertFalse(editState.dialogStates.showLogicalWalletPicker)
        assertEquals(mockPhysicalWallet2, editState.selectedWallets.physical) // Previous selection preserved
        assertTrue(editState.hasChanges)
    }

    @Test
    fun `screen should handle changes detection correctly`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Initially no changes
        assertFalse(editState.hasChanges)

        // Make various types of changes
        editState = editState.updateTitle("Updated Title")
        assertTrue(editState.hasChanges)

        // Reset to original value - should show no changes
        editState = editState.updateTitle("Grocery Shopping")
        assertFalse(editState.hasChanges)

        // Change amount
        editState = editState.updateAmount("100.00")
        assertTrue(editState.hasChanges)

        // Change tags
        editState = editState.updateAmount("85.50") // Reset amount
        editState = editState.updateTags("new, tags")
        assertTrue(editState.hasChanges)

        // Reset tags to original
        editState = editState.updateTags("food, grocery")
        assertFalse(editState.hasChanges)
    }

    @Test
    fun `screen should validate transfer transactions correctly`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet, mockPhysicalWallet2)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransferTransaction,
            availableWallets = availableWallets
        )

        // Initially valid transfer
        assertTrue(editState.isFormValid)
        assertNull(editState.validationErrors.transferError)

        // Change destination to same wallet as source - should be invalid
        editState = editState.updateDestinationWallet(mockPhysicalWallet) // Same as source
        assertFalse(editState.isFormValid)
        assertNotNull(editState.validationErrors.transferError)
        assertTrue(editState.validationErrors.transferError!!.contains("different"))

        // Change destination to different type - should be invalid
        editState = editState.updateDestinationWallet(mockLogicalWallet) // Different type
        assertFalse(editState.isFormValid)
        assertNotNull(editState.validationErrors.transferError)
        assertTrue(editState.validationErrors.transferError!!.contains("same type"))

        // Fix the destination - should be valid again
        editState = editState.updateDestinationWallet(mockPhysicalWallet2)
        assertTrue(editState.isFormValid)
        assertNull(editState.validationErrors.transferError)
    }

    @Test
    fun `screen should handle date selection with dialog management`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        val newDate = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000) // Tomorrow

        // Open date picker
        editState = editState.openDialog(DialogType.DATE_PICKER)
        assertTrue(editState.dialogStates.showDatePicker)

        // Select date - should close dialog and update date
        editState = editState.updateSelectedDate(newDate)
        assertEquals(newDate, editState.selectedDate)
        assertFalse(editState.dialogStates.showDatePicker)
        assertTrue(editState.hasChanges) // Date change should be detected
    }

    @Test
    fun `screen should provide transaction summary for updates`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Make some changes
        editState = editState.updateTitle("Updated Grocery Shopping")
        editState = editState.updateAmount("95.75")
        editState = editState.updateTags("food, grocery, updated")

        val summary = editState.getTransactionSummary()

        assertEquals("Updated Grocery Shopping", summary.title)
        assertEquals(95.75, summary.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, summary.type)
        assertEquals(2, summary.wallets.size)
        assertTrue(summary.wallets.contains(mockPhysicalWallet))
        assertTrue(summary.wallets.contains(mockLogicalWallet))
        assertEquals(listOf("food", "grocery", "updated"), summary.tags)
        assertTrue(summary.isValid)
    }

    @Test
    fun `screen should handle legacy single wallet transaction format`() {
        val legacyTransaction = mockExpenseTransaction.copy(
            affectedWalletIds = emptyList(),
            walletId = "physical-1" // Old single wallet format
        )
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)

        val editState = EditTransactionUiState.fromTransaction(
            transaction = legacyTransaction,
            availableWallets = availableWallets
        )

        assertEquals("Grocery Shopping", editState.title)
        assertEquals("85.50", editState.amount)
        assertEquals(mockPhysicalWallet, editState.selectedWallets.physical)
        assertNull(editState.selectedWallets.logical) // Only one wallet in legacy format
        assertTrue(editState.isFormValid) // Should still be valid with one wallet
    }

    @Test
    fun `screen should maintain performance with consolidated state`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val initialState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Measure performance of state updates
        val startTime = System.currentTimeMillis()

        var state = initialState
        for (i in 1..100) {
            state = state.updateTitle("Title $i")
            state = state.updateAmount("$i.00")
            state = state.updateTags("tag$i")
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete 300 state updates in under 50ms
        assertTrue("Edit state updates should be fast, took ${duration}ms", duration < 50)

        // Verify final state
        assertEquals("Title 100", state.title)
        assertEquals("100.00", state.amount)
        assertEquals("tag100", state.tags)
        assertEquals(mockExpenseTransaction, state.originalTransaction) // Original preserved
        assertTrue(state.hasChanges)
    }

    @Test
    fun `screen should handle complete edit transaction flow`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet, mockPhysicalWallet2)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Verify initial state
        assertEquals("Grocery Shopping", editState.title)
        assertEquals("85.50", editState.amount)
        assertFalse(editState.hasChanges)
        assertTrue(editState.isFormValid)

        // Step 1: Update title
        editState = editState.updateTitle("Updated Grocery Shopping")
        assertEquals("Updated Grocery Shopping", editState.title)
        assertTrue(editState.hasChanges)

        // Step 2: Update amount
        editState = editState.updateAmount("125.00")
        assertEquals("125.00", editState.amount)
        assertTrue(editState.hasChanges)

        // Step 3: Change physical wallet
        editState = editState.updatePhysicalWallet(mockPhysicalWallet2)
        assertEquals(mockPhysicalWallet2, editState.selectedWallets.physical)
        assertTrue(editState.hasChanges)

        // Step 4: Update tags
        editState = editState.updateTags("food, grocery, updated, expensive")
        assertEquals("food, grocery, updated, expensive", editState.tags)
        assertTrue(editState.hasChanges)

        // Step 5: Update date
        val newDate = Date(System.currentTimeMillis() - 48 * 60 * 60 * 1000) // Two days ago
        editState = editState.updateSelectedDate(newDate)
        assertEquals(newDate, editState.selectedDate)
        assertTrue(editState.hasChanges)

        // Final validation - should be completely valid
        assertTrue(editState.isFormValid)
        assertFalse(editState.validationErrors.hasErrors)

        // Generate summary for update
        val summary = editState.getTransactionSummary()
        assertTrue(summary.isValid)
        assertEquals("Updated Grocery Shopping", summary.title)
        assertEquals(125.00, summary.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, summary.type)
        assertEquals(2, summary.wallets.size)
        assertEquals(listOf("food", "grocery", "updated", "expensive"), summary.tags)
        assertEquals(newDate, summary.date)
    }

    @Test
    fun `screen should preserve state across dialog interactions`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Make some changes first
        editState = editState.updateTitle("Modified Transaction")
        editState = editState.updateAmount("150.00")

        // Open and close dialogs - should preserve form data
        editState = editState.openDialog(DialogType.DATE_PICKER)
        assertTrue(editState.dialogStates.showDatePicker)
        assertEquals("Modified Transaction", editState.title) // Form data preserved
        assertEquals("150.00", editState.amount)
        assertTrue(editState.hasChanges)

        editState = editState.closeAllDialogs()
        assertFalse(editState.dialogStates.showDatePicker)
        assertEquals("Modified Transaction", editState.title) // Form data still preserved
        assertEquals("150.00", editState.amount)
        assertTrue(editState.hasChanges)

        // Open wallet picker and select wallet
        editState = editState.openDialog(DialogType.LOGICAL_WALLET)
        editState = editState.updateLogicalWallet(null)

        assertEquals("Modified Transaction", editState.title) // Form data still preserved
        assertEquals("150.00", editState.amount)
        assertNull(editState.selectedWallets.logical)
        assertFalse(editState.dialogStates.showLogicalWalletPicker) // Dialog closed
        assertTrue(editState.hasChanges)
    }

    // ========================================
    // Tag Normalization Integration Tests (Phase 5)
    // ========================================

    @Test
    fun `fromTransaction should normalize loaded tags`() {
        // Create transaction with non-normalized tags
        val transactionWithMixedTags = mockExpenseTransaction.copy(
            tags = listOf("Food", "TRAVEL", "shopping")
        )
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)

        val editState = EditTransactionUiState.fromTransaction(
            transaction = transactionWithMixedTags,
            availableWallets = availableWallets
        )

        // Verify that loaded tags are normalized
        assertEquals("food, travel, shopping", editState.tags)
    }

    @Test
    fun `fromTransaction should filter out Untagged from loaded tags`() {
        // Create transaction with "Untagged" keyword
        val transactionWithUntagged = mockExpenseTransaction.copy(
            tags = listOf("food", "Untagged", "travel")
        )
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)

        val editState = EditTransactionUiState.fromTransaction(
            transaction = transactionWithUntagged,
            availableWallets = availableWallets
        )

        // Verify that "Untagged" is filtered out
        assertEquals("food, travel", editState.tags)
        assertFalse(editState.tags.contains("untagged"))
    }

    @Test
    fun `fromTransaction should handle duplicate tags`() {
        // Create transaction with duplicate tags
        val transactionWithDuplicates = mockExpenseTransaction.copy(
            tags = listOf("food", "Food", "FOOD", "travel")
        )
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)

        val editState = EditTransactionUiState.fromTransaction(
            transaction = transactionWithDuplicates,
            availableWallets = availableWallets
        )

        // Verify that duplicates are removed
        assertEquals("food, travel", editState.tags)
    }

    @Test
    fun `updateTags should normalize tags with mixed case and whitespace`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update tags with mixed case and whitespace
        val updatedState = editState.updateTags(" Food , TRAVEL, Shopping ")

        // Verify that tags are normalized
        assertEquals("food, travel, shopping", updatedState.tags)
        assertTrue(updatedState.hasChanges)
    }

    @Test
    fun `updateTags should filter out Untagged keyword`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update tags with "Untagged" keyword
        val updatedState = editState.updateTags("food, Untagged, travel")

        // Verify that "Untagged" is filtered out
        assertEquals("food, travel", updatedState.tags)
        assertFalse(updatedState.tags.contains("untagged"))
        assertTrue(updatedState.hasChanges)
    }

    @Test
    fun `updateTags should remove duplicate tags`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update tags with duplicates
        val updatedState = editState.updateTags("Food, food, FOOD")

        // Verify that duplicates are removed and normalized
        assertEquals("food", updatedState.tags)
        assertTrue(updatedState.hasChanges)
    }

    @Test
    fun `updateTags should filter out blank tags`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update tags with blank entries
        val updatedState = editState.updateTags("food,  , travel, , shopping")

        // Verify that blank tags are filtered out
        assertEquals("food, travel, shopping", updatedState.tags)
    }

    @Test
    fun `updateTags with only blank tags should result in empty string`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update tags with only blanks
        val updatedState = editState.updateTags("  ,  , ")

        // Verify that result is empty
        assertEquals("", updatedState.tags)
        assertTrue(updatedState.hasChanges)
    }

    @Test
    fun `getTransactionSummary should return normalized tags`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update with non-normalized tags
        val stateWithMixedTags = editState.updateTags("Food, TRAVEL, shopping")

        // Get transaction summary
        val summary = stateWithMixedTags.getTransactionSummary()

        // Verify that tags are normalized in the summary
        assertEquals(listOf("food", "travel", "shopping"), summary.tags)
    }

    @Test
    fun `getTransactionSummary should filter out Untagged keyword`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update with "Untagged" in tags
        val stateWithUntagged = editState.updateTags("food, Untagged, travel")

        // Get transaction summary
        val summary = stateWithUntagged.getTransactionSummary()

        // Verify that "Untagged" is not in the summary
        assertEquals(listOf("food", "travel"), summary.tags)
        assertFalse(summary.tags.contains("untagged"))
    }

    @Test
    fun `hasChanges should detect tag normalization changes correctly`() {
        // Create transaction with non-normalized tags
        val transactionWithMixedTags = mockExpenseTransaction.copy(
            tags = listOf("Food", "Travel")
        )
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)

        val editState = EditTransactionUiState.fromTransaction(
            transaction = transactionWithMixedTags,
            availableWallets = availableWallets
        )

        // Initially no changes (tags are normalized on load)
        assertFalse(editState.hasChanges)
        assertEquals("food, travel", editState.tags)

        // Update to different tags
        val changedState = editState.updateTags("shopping, groceries")
        assertTrue(changedState.hasChanges)

        // Update back to original (normalized form)
        val revertedState = changedState.updateTags("food, travel")
        assertFalse(revertedState.hasChanges)
    }

    @Test
    fun `complex tag editing flow - multiple updates with normalization`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Step 1: Initial tags are "food, grocery"
        assertEquals("food, grocery", editState.tags)
        assertFalse(editState.hasChanges)

        // Step 2: Update with mixed case
        editState = editState.updateTags(" Food , GROCERY, Shopping ")
        assertEquals("food, grocery, shopping", editState.tags)
        assertTrue(editState.hasChanges)

        // Step 3: Add duplicate with different case
        editState = editState.updateTags("food, grocery, shopping, FOOD")
        assertEquals("food, grocery, shopping", editState.tags)
        assertTrue(editState.hasChanges)

        // Step 4: Add new tag with whitespace
        editState = editState.updateTags("food, grocery, shopping,  groceries ")
        assertEquals("food, grocery, shopping, groceries", editState.tags)
        assertTrue(editState.hasChanges)

        // Step 5: Verify final summary
        val summary = editState.getTransactionSummary()
        assertEquals(listOf("food", "grocery", "shopping", "groceries"), summary.tags)
    }

    @Test
    fun `editing tags multiple times with Untagged - all filtered out`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update with multiple variations of "Untagged"
        editState = editState.updateTags("food, Untagged, travel, UNTAGGED, untagged")

        // Verify that all variations of "Untagged" are filtered out
        assertEquals("food, travel", editState.tags)
    }

    @Test
    fun `clearing all tags should result in empty string and detect changes`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Start with tags "food, grocery"
        assertEquals("food, grocery", editState.tags)

        // Clear all tags
        editState = editState.updateTags("")

        // Verify that tags are empty and change is detected
        assertEquals("", editState.tags)
        assertTrue(editState.hasChanges)
    }

    @Test
    fun `unicode and special characters in tags - normalized correctly`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockExpenseTransaction,
            availableWallets = availableWallets
        )

        // Update with unicode characters
        editState = editState.updateTags(" Café , CAFÉ, café ")

        // Verify that unicode characters are normalized (lowercase) and duplicates removed
        assertEquals("café", editState.tags)
    }

    @Test
    fun `transfer transactions load with empty tags but can be edited`() {
        val availableWallets = listOf(mockPhysicalWallet, mockPhysicalWallet2)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransferTransaction,
            availableWallets = availableWallets
        )

        // Transfer transactions should load with empty tags (per business rules)
        assertEquals("", editState.tags)

        // Tags can be added to transfers (normalization still applies)
        editState = editState.updateTags("food, TRAVEL")

        // Verify tags are normalized and changes detected
        assertEquals("food, travel", editState.tags)
        assertTrue(editState.hasChanges)
    }
}
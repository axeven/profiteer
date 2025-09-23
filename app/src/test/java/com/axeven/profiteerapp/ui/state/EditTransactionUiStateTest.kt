package com.axeven.profiteerapp.ui.state

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Test-Driven Development tests for EditTransactionUiState.
 *
 * Following TDD methodology - these tests define the expected behavior for the
 * edit transaction state management. They will initially fail (RED phase)
 * and will be made to pass during implementation (GREEN phase).
 */
class EditTransactionUiStateTest {

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

    private val mockTransaction = Transaction(
        id = "transaction-1",
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

    // Tests for EditTransactionUiState construction and initialization
    @Test
    fun `should create default EditTransactionUiState`() {
        val editState = EditTransactionUiState()

        assertEquals("", editState.title)
        assertEquals("", editState.amount)
        assertEquals(TransactionType.EXPENSE, editState.selectedType)
        assertEquals(SelectedWallets(), editState.selectedWallets)
        assertEquals(DialogStates(), editState.dialogStates)
        assertEquals(ValidationErrors(), editState.validationErrors)
        assertFalse(editState.isFormValid)
        assertFalse(editState.deletionRequested)
        assertNull(editState.originalTransaction)
    }

    @Test
    fun `should create EditTransactionUiState from existing transaction`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransaction)

        assertEquals("Grocery Shopping", editState.title)
        assertEquals("85.50", editState.amount) // Absolute value for display
        assertEquals(TransactionType.EXPENSE, editState.selectedType)
        assertEquals("food, grocery", editState.tags)
        assertEquals(mockTransaction.transactionDate, editState.selectedDate)
        assertEquals(mockTransaction, editState.originalTransaction)
        assertFalse(editState.deletionRequested)
    }

    @Test
    fun `should handle transfer transaction initialization`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransferTransaction)

        assertEquals("Move to Savings", editState.title)
        assertEquals("200.00", editState.amount)
        assertEquals(TransactionType.TRANSFER, editState.selectedType)
        assertEquals("", editState.tags) // Transfers don't have tags
        assertEquals(mockTransferTransaction, editState.originalTransaction)
    }

    @Test
    fun `should initialize wallets from transaction data`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransaction,
            availableWallets = availableWallets
        )

        assertEquals(mockPhysicalWallet, editState.selectedWallets.physical)
        assertEquals(mockLogicalWallet, editState.selectedWallets.logical)
    }

    @Test
    fun `should handle legacy single wallet transaction format`() {
        val legacyTransaction = mockTransaction.copy(
            affectedWalletIds = emptyList(),
            walletId = "physical-1" // Old single wallet format
        )
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)

        val editState = EditTransactionUiState.fromTransaction(
            transaction = legacyTransaction,
            availableWallets = availableWallets
        )

        assertEquals(mockPhysicalWallet, editState.selectedWallets.physical)
        assertNull(editState.selectedWallets.logical) // Only one wallet in legacy format
    }

    // Tests for deletion state management
    @Test
    fun `should track deletion request state`() {
        var editState = EditTransactionUiState()
        assertFalse(editState.deletionRequested)

        editState = editState.requestDeletion()
        assertTrue(editState.deletionRequested)

        editState = editState.cancelDeletion()
        assertFalse(editState.deletionRequested)
    }

    @Test
    fun `should preserve other state when updating deletion status`() {
        val editState = EditTransactionUiState(
            title = "Test Transaction",
            amount = "100.00"
        )

        val deletionRequestedState = editState.requestDeletion()

        assertEquals("Test Transaction", deletionRequestedState.title)
        assertEquals("100.00", deletionRequestedState.amount)
        assertTrue(deletionRequestedState.deletionRequested)
    }

    // Tests for edit-specific validation
    @Test
    fun `should validate that form has changes from original transaction`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransaction,
            availableWallets = availableWallets
        )

        // No changes initially
        assertFalse(editState.hasChanges)

        // Make a change
        val changedState = editState.updateAndValidate(title = "Modified Title")
        assertTrue(changedState.hasChanges)
    }

    @Test
    fun `should detect changes in different fields`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransaction)

        // Title change
        val titleChanged = editState.updateAndValidate(title = "New Title")
        assertTrue(titleChanged.hasChanges)

        // Amount change
        val amountChanged = editState.updateAndValidate(amount = "100.00")
        assertTrue(amountChanged.hasChanges)

        // Tags change
        val tagsChanged = editState.updateAndValidate(tags = "new, tags")
        assertTrue(tagsChanged.hasChanges)

        // Date change
        val dateChanged = editState.updateAndValidate(selectedDate = Date())
        assertTrue(dateChanged.hasChanges)
    }

    @Test
    fun `should not show changes for equivalent values`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransaction,
            availableWallets = availableWallets
        )

        // Set same values as original
        val unchangedState = editState.updateAndValidate(
            title = "Grocery Shopping",
            amount = "85.50",
            tags = "food, grocery"
        )

        assertFalse(unchangedState.hasChanges)
    }

    // Tests for wallet state management in edit mode
    @Test
    fun `should update wallets while preserving edit state`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransaction)
        val newWallet = Wallet(
            id = "physical-2",
            name = "Backup Account",
            balance = 300.0,
            walletType = "Physical"
        )

        val updatedState = editState.updatePhysicalWallet(newWallet)

        assertEquals(newWallet, updatedState.selectedWallets.physical)
        assertEquals(mockTransaction, updatedState.originalTransaction)
        assertTrue(updatedState.hasChanges) // Wallet change should be detected
    }

    @Test
    fun `should handle transaction type restrictions in edit mode`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransaction)

        // Transaction type should not be changeable in edit mode
        assertEquals(TransactionType.EXPENSE, editState.selectedType)

        // This test verifies the business rule that transaction type cannot be changed
        // when editing existing transactions
        val typeChangeAttempt = editState.updateTransactionType(TransactionType.INCOME)
        assertEquals(TransactionType.EXPENSE, typeChangeAttempt.selectedType) // Should remain unchanged
    }

    // Tests for form validation specific to edit mode
    @Test
    fun `should validate edit form with all required fields`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransaction,
            availableWallets = availableWallets
        ).updateAndValidate()

        assertTrue(editState.isFormValid)
        assertFalse(editState.validationErrors.hasErrors)
    }

    @Test
    fun `should show validation errors for invalid edits`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransaction)
            .updateAndValidate(
                title = "", // Invalid empty title
                amount = "invalid" // Invalid amount
            )

        assertFalse(editState.isFormValid)
        assertTrue(editState.validationErrors.hasErrors)
        assertNotNull(editState.validationErrors.titleError)
        assertNotNull(editState.validationErrors.amountError)
    }

    // Tests for immutability and state copying
    @Test
    fun `should maintain immutability during updates`() {
        val originalState = EditTransactionUiState.fromTransaction(mockTransaction)
        val updatedState = originalState.updateAndValidate(title = "New Title")

        // Original state should be unchanged
        assertEquals("Grocery Shopping", originalState.title)
        assertEquals(mockTransaction, originalState.originalTransaction)

        // Updated state should have changes
        assertEquals("New Title", updatedState.title)
        assertEquals(mockTransaction, updatedState.originalTransaction)

        // Objects should be different instances
        assertNotSame(originalState, updatedState)
    }

    @Test
    fun `should preserve original transaction reference across updates`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransaction)

        val updatedStates = listOf(
            editState.updateAndValidate(title = "Title 1"),
            editState.updateAndValidate(amount = "100.00"),
            editState.updatePhysicalWallet(mockPhysicalWallet),
            editState.requestDeletion()
        )

        updatedStates.forEach { state ->
            assertEquals(mockTransaction, state.originalTransaction)
        }
    }

    // Tests for complex edit scenarios
    @Test
    fun `should handle complete edit transaction flow`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransaction,
            availableWallets = availableWallets
        )

        // Verify initial state from transaction
        assertEquals("Grocery Shopping", editState.title)
        assertEquals("85.50", editState.amount)
        assertFalse(editState.hasChanges)

        // Make multiple changes
        editState = editState.updateAndValidate(title = "Updated Grocery Shopping")
        editState = editState.updateAndValidate(amount = "95.75")
        editState = editState.updateAndValidate(tags = "food, grocery, updated")

        // Verify changes are tracked
        assertTrue(editState.hasChanges)
        assertTrue(editState.isFormValid)
        assertEquals("Updated Grocery Shopping", editState.title)
        assertEquals("95.75", editState.amount)
        assertEquals("food, grocery, updated", editState.tags)
        assertEquals(mockTransaction, editState.originalTransaction)
    }

    @Test
    fun `should handle transfer transaction editing`() {
        val availableWallets = listOf(
            mockPhysicalWallet,
            Wallet("physical-2", "Backup Account", 300.0, walletType = "Physical")
        )

        var editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransferTransaction,
            availableWallets = availableWallets
        )

        // Verify transfer initialization
        assertEquals(TransactionType.TRANSFER, editState.selectedType)
        assertEquals(mockPhysicalWallet, editState.selectedWallets.source)

        // Update transfer amount
        editState = editState.updateAndValidate(amount = "250.00")

        assertTrue(editState.hasChanges)
        assertTrue(editState.isFormValid)
        assertEquals("250.00", editState.amount)
    }

    @Test
    fun `should generate transaction summary for edit updates`() {
        val availableWallets = listOf(mockPhysicalWallet, mockLogicalWallet)
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransaction,
            availableWallets = availableWallets
        ).updateAndValidate(
            title = "Updated Transaction",
            amount = "120.00"
        )

        val summary = editState.getTransactionSummary()

        assertEquals("Updated Transaction", summary.title)
        assertEquals(120.00, summary.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, summary.type)
        assertTrue(summary.isValid)
        assertEquals(mockTransaction.transactionDate, summary.date)
    }

    // Tests for dialog state management
    @Test
    fun `should manage dialog states independently of edit state`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransaction)

        val dialogOpenState = editState.openDialog(DialogType.DATE_PICKER)
        assertTrue(dialogOpenState.dialogStates.showDatePicker)
        assertEquals("Grocery Shopping", dialogOpenState.title) // Form data preserved

        val dialogClosedState = dialogOpenState.closeAllDialogs()
        assertFalse(dialogClosedState.dialogStates.showDatePicker)
        assertEquals("Grocery Shopping", dialogClosedState.title) // Form data still preserved
    }

    // Performance tests
    @Test
    fun `should maintain performance with edit state updates`() {
        val editState = EditTransactionUiState.fromTransaction(mockTransaction)

        val startTime = System.currentTimeMillis()

        var state = editState
        for (i in 1..100) {
            state = state.updateAndValidate(title = "Title $i")
            state = state.updateAndValidate(amount = "$i.00")
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete 200 edit state updates in under 50ms
        assertTrue("Edit state updates should be fast, took ${duration}ms", duration < 50)

        // Verify final state
        assertEquals("Title 100", state.title)
        assertEquals("100.00", state.amount)
        assertEquals(mockTransaction, state.originalTransaction) // Original preserved
        assertTrue(state.hasChanges)
    }
}
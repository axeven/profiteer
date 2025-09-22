package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Test-Driven Development tests for CreateTransactionStateManager.
 *
 * Following TDD methodology - these tests define the expected behavior
 * for state update functions. They will initially fail (RED phase)
 * and will be made to pass during implementation (GREEN phase).
 */
class CreateTransactionStateManagerTest {

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

    // Tests for updateTitle function
    @Test
    fun `updateTitle should update title and preserve other state`() {
        val initialState = CreateTransactionUiState(
            title = "Old Title",
            amount = "100.00",
            selectedType = TransactionType.INCOME
        )

        val updatedState = updateTitle(initialState, "New Title")

        assertEquals("New Title", updatedState.title)
        assertEquals("100.00", updatedState.amount)
        assertEquals(TransactionType.INCOME, updatedState.selectedType)
    }

    @Test
    fun `updateTitle should trigger form validation`() {
        // Create an initial state with proper validation calculated
        val initialState = CreateTransactionUiState(title = "").updateAndValidate()

        val updatedState = updateTitle(initialState, "Valid Title")

        assertNotEquals(initialState.validationErrors, updatedState.validationErrors)
        // Initially, title error should exist, after update it should be cleared
        assertNotNull(initialState.validationErrors.titleError)
        assertNull(updatedState.validationErrors.titleError)
    }

    @Test
    fun `updateTitle should handle empty title validation`() {
        val initialState = CreateTransactionUiState(title = "Valid Title")

        val updatedState = updateTitle(initialState, "")

        assertNotNull(updatedState.validationErrors.titleError)
        assertFalse(updatedState.isFormValid)
    }

    // Tests for updateAmount function
    @Test
    fun `updateAmount should update amount and preserve other state`() {
        val initialState = CreateTransactionUiState(
            title = "Test Transaction",
            amount = "50.00",
            selectedType = TransactionType.EXPENSE
        )

        val updatedState = updateAmount(initialState, "75.50")

        assertEquals("75.50", updatedState.amount)
        assertEquals("Test Transaction", updatedState.title)
        assertEquals(TransactionType.EXPENSE, updatedState.selectedType)
    }

    @Test
    fun `updateAmount should trigger form validation`() {
        // Create an initial state with proper validation calculated
        val initialState = CreateTransactionUiState(amount = "invalid").updateAndValidate()

        val updatedState = updateAmount(initialState, "100.00")

        assertNotEquals(initialState.validationErrors, updatedState.validationErrors)
        // Initially, amount error should exist, after update it should be cleared
        assertNotNull(initialState.validationErrors.amountError)
        assertNull(updatedState.validationErrors.amountError)
    }

    @Test
    fun `updateAmount should handle invalid amount validation`() {
        val initialState = CreateTransactionUiState(amount = "100.00")

        val updatedState = updateAmount(initialState, "invalid-amount")

        assertNotNull(updatedState.validationErrors.amountError)
        assertFalse(updatedState.isFormValid)
    }

    @Test
    fun `updateAmount should handle negative amount validation`() {
        val initialState = CreateTransactionUiState(amount = "100.00")

        val updatedState = updateAmount(initialState, "-50.00")

        assertNotNull(updatedState.validationErrors.amountError)
        assertFalse(updatedState.isFormValid)
    }

    // Tests for updateTransactionType function
    @Test
    fun `updateTransactionType should update type and preserve compatible state`() {
        val initialState = CreateTransactionUiState(
            title = "Test Transaction",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        val updatedState = updateTransactionType(initialState, TransactionType.INCOME)

        assertEquals(TransactionType.INCOME, updatedState.selectedType)
        assertEquals("Test Transaction", updatedState.title)
        assertEquals("100.00", updatedState.amount)
        assertEquals(mockPhysicalWallet, updatedState.selectedWallets.physical)
    }

    @Test
    fun `updateTransactionType should clear transfer wallets when switching from transfer`() {
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.TRANSFER,
            selectedWallets = SelectedWallets(
                source = mockPhysicalWallet,
                destination = mockPhysicalWallet2
            )
        )

        val updatedState = updateTransactionType(initialState, TransactionType.EXPENSE)

        assertEquals(TransactionType.EXPENSE, updatedState.selectedType)
        assertNull(updatedState.selectedWallets.source)
        assertNull(updatedState.selectedWallets.destination)
    }

    @Test
    fun `updateTransactionType should preserve regular wallets when switching to transfer`() {
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets(
                physical = mockPhysicalWallet,
                logical = mockLogicalWallet
            )
        )

        val updatedState = updateTransactionType(initialState, TransactionType.TRANSFER)

        assertEquals(TransactionType.TRANSFER, updatedState.selectedType)
        assertEquals(mockPhysicalWallet, updatedState.selectedWallets.physical)
        assertEquals(mockLogicalWallet, updatedState.selectedWallets.logical)
    }

    @Test
    fun `updateTransactionType should trigger form validation`() {
        val initialState = CreateTransactionUiState(selectedType = TransactionType.EXPENSE)

        val updatedState = updateTransactionType(initialState, TransactionType.TRANSFER)

        assertNotEquals(initialState.validationErrors, updatedState.validationErrors)
    }

    // Tests for updatePhysicalWallet function
    @Test
    fun `updatePhysicalWallet should update physical wallet and preserve other state`() {
        val initialState = CreateTransactionUiState(
            title = "Test Transaction",
            selectedWallets = SelectedWallets(logical = mockLogicalWallet)
        )

        val updatedState = updatePhysicalWallet(initialState, mockPhysicalWallet)

        assertEquals(mockPhysicalWallet, updatedState.selectedWallets.physical)
        assertEquals(mockLogicalWallet, updatedState.selectedWallets.logical)
        assertEquals("Test Transaction", updatedState.title)
    }

    @Test
    fun `updatePhysicalWallet should trigger form validation`() {
        // Create an initial state with proper validation calculated
        val initialState = CreateTransactionUiState().updateAndValidate()

        val updatedState = updatePhysicalWallet(initialState, mockPhysicalWallet)

        assertNotEquals(initialState.validationErrors, updatedState.validationErrors)
        // Initially, wallet error should exist (no wallets selected), after update it should be cleared
        assertNotNull(initialState.validationErrors.walletError)
        assertNull(updatedState.validationErrors.walletError)
    }

    @Test
    fun `updatePhysicalWallet should close wallet picker dialog`() {
        val initialState = CreateTransactionUiState(
            dialogStates = DialogStates(showPhysicalWalletPicker = true)
        )

        val updatedState = updatePhysicalWallet(initialState, mockPhysicalWallet)

        assertFalse(updatedState.dialogStates.showPhysicalWalletPicker)
    }

    // Tests for updateLogicalWallet function
    @Test
    fun `updateLogicalWallet should update logical wallet and preserve other state`() {
        val initialState = CreateTransactionUiState(
            title = "Test Transaction",
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        val updatedState = updateLogicalWallet(initialState, mockLogicalWallet)

        assertEquals(mockLogicalWallet, updatedState.selectedWallets.logical)
        assertEquals(mockPhysicalWallet, updatedState.selectedWallets.physical)
        assertEquals("Test Transaction", updatedState.title)
    }

    @Test
    fun `updateLogicalWallet should close wallet picker dialog`() {
        val initialState = CreateTransactionUiState(
            dialogStates = DialogStates(showLogicalWalletPicker = true)
        )

        val updatedState = updateLogicalWallet(initialState, mockLogicalWallet)

        assertFalse(updatedState.dialogStates.showLogicalWalletPicker)
    }

    // Tests for updateSourceWallet function
    @Test
    fun `updateSourceWallet should update source wallet for transfers`() {
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.TRANSFER,
            selectedWallets = SelectedWallets(destination = mockPhysicalWallet2)
        )

        val updatedState = updateSourceWallet(initialState, mockPhysicalWallet)

        assertEquals(mockPhysicalWallet, updatedState.selectedWallets.source)
        assertEquals(mockPhysicalWallet2, updatedState.selectedWallets.destination)
    }

    @Test
    fun `updateSourceWallet should close source wallet picker dialog`() {
        val initialState = CreateTransactionUiState(
            dialogStates = DialogStates(showSourceWalletPicker = true)
        )

        val updatedState = updateSourceWallet(initialState, mockPhysicalWallet)

        assertFalse(updatedState.dialogStates.showSourceWalletPicker)
    }

    // Tests for updateDestinationWallet function
    @Test
    fun `updateDestinationWallet should update destination wallet for transfers`() {
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.TRANSFER,
            selectedWallets = SelectedWallets(source = mockPhysicalWallet)
        )

        val updatedState = updateDestinationWallet(initialState, mockPhysicalWallet2)

        assertEquals(mockPhysicalWallet, updatedState.selectedWallets.source)
        assertEquals(mockPhysicalWallet2, updatedState.selectedWallets.destination)
    }

    @Test
    fun `updateDestinationWallet should close destination wallet picker dialog`() {
        val initialState = CreateTransactionUiState(
            dialogStates = DialogStates(showDestinationWalletPicker = true)
        )

        val updatedState = updateDestinationWallet(initialState, mockPhysicalWallet2)

        assertFalse(updatedState.dialogStates.showDestinationWalletPicker)
    }

    // Tests for updateSelectedDate function
    @Test
    fun `updateSelectedDate should update date and preserve other state`() {
        val initialState = CreateTransactionUiState(
            title = "Test Transaction",
            selectedDate = Date(1000)
        )
        val newDate = Date(2000)

        val updatedState = updateSelectedDate(initialState, newDate)

        assertEquals(newDate, updatedState.selectedDate)
        assertEquals("Test Transaction", updatedState.title)
    }

    @Test
    fun `updateSelectedDate should close date picker dialog`() {
        val initialState = CreateTransactionUiState(
            dialogStates = DialogStates(showDatePicker = true)
        )
        val newDate = Date()

        val updatedState = updateSelectedDate(initialState, newDate)

        assertFalse(updatedState.dialogStates.showDatePicker)
    }

    // Tests for updateTags function
    @Test
    fun `updateTags should update tags and preserve other state`() {
        val initialState = CreateTransactionUiState(
            title = "Test Transaction",
            tags = "old, tags"
        )

        val updatedState = updateTags(initialState, "new, tags, list")

        assertEquals("new, tags, list", updatedState.tags)
        assertEquals("Test Transaction", updatedState.title)
    }

    @Test
    fun `updateTags should trigger form validation`() {
        val initialState = CreateTransactionUiState(tags = "")

        val updatedState = updateTags(initialState, "valid, tags")

        assertNotEquals(initialState.validationErrors, updatedState.validationErrors)
    }

    // Tests for openDialog function
    @Test
    fun `openDialog should open specific dialog and close others`() {
        val initialState = CreateTransactionUiState(
            dialogStates = DialogStates(showPhysicalWalletPicker = true)
        )

        val updatedState = openDialog(initialState, DialogType.DATE_PICKER)

        assertTrue(updatedState.dialogStates.showDatePicker)
        assertFalse(updatedState.dialogStates.showPhysicalWalletPicker)
        assertFalse(updatedState.dialogStates.showLogicalWalletPicker)
        assertFalse(updatedState.dialogStates.showSourceWalletPicker)
        assertFalse(updatedState.dialogStates.showDestinationWalletPicker)
    }

    @Test
    fun `openDialog should preserve non-dialog state`() {
        val initialState = CreateTransactionUiState(
            title = "Test Transaction",
            amount = "100.00"
        )

        val updatedState = openDialog(initialState, DialogType.PHYSICAL_WALLET)

        assertEquals("Test Transaction", updatedState.title)
        assertEquals("100.00", updatedState.amount)
        assertTrue(updatedState.dialogStates.showPhysicalWalletPicker)
    }

    // Tests for closeAllDialogs function
    @Test
    fun `closeAllDialogs should close all open dialogs`() {
        val initialState = CreateTransactionUiState(
            dialogStates = DialogStates(
                showDatePicker = true,
                showPhysicalWalletPicker = true,
                showLogicalWalletPicker = true
            )
        )

        val updatedState = closeAllDialogs(initialState)

        assertFalse(updatedState.dialogStates.showDatePicker)
        assertFalse(updatedState.dialogStates.showPhysicalWalletPicker)
        assertFalse(updatedState.dialogStates.showLogicalWalletPicker)
        assertFalse(updatedState.dialogStates.showSourceWalletPicker)
        assertFalse(updatedState.dialogStates.showDestinationWalletPicker)
    }

    @Test
    fun `closeAllDialogs should preserve non-dialog state`() {
        val initialState = CreateTransactionUiState(
            title = "Test Transaction",
            amount = "100.00",
            dialogStates = DialogStates(showDatePicker = true)
        )

        val updatedState = closeAllDialogs(initialState)

        assertEquals("Test Transaction", updatedState.title)
        assertEquals("100.00", updatedState.amount)
        assertFalse(updatedState.dialogStates.showDatePicker)
    }

    // Integration tests for complex state updates
    @Test
    fun `should handle complete transaction creation flow`() {
        var state = CreateTransactionUiState()

        // Fill in form data
        state = updateTitle(state, "Grocery Shopping")
        state = updateAmount(state, "85.50")
        state = updateTransactionType(state, TransactionType.EXPENSE)
        state = updatePhysicalWallet(state, mockPhysicalWallet)
        state = updateLogicalWallet(state, mockLogicalWallet)
        state = updateTags(state, "food, grocery, household")

        assertEquals("Grocery Shopping", state.title)
        assertEquals("85.50", state.amount)
        assertEquals(TransactionType.EXPENSE, state.selectedType)
        assertEquals(mockPhysicalWallet, state.selectedWallets.physical)
        assertEquals(mockLogicalWallet, state.selectedWallets.logical)
        assertEquals("food, grocery, household", state.tags)
        assertTrue(state.isFormValid)
        assertFalse(state.validationErrors.hasErrors)
    }

    @Test
    fun `should handle transfer transaction flow`() {
        var state = CreateTransactionUiState()

        // Set up transfer
        state = updateTitle(state, "Move to Savings")
        state = updateAmount(state, "200.00")
        state = updateTransactionType(state, TransactionType.TRANSFER)
        state = updateSourceWallet(state, mockPhysicalWallet)
        state = updateDestinationWallet(state, mockPhysicalWallet2)

        assertEquals("Move to Savings", state.title)
        assertEquals("200.00", state.amount)
        assertEquals(TransactionType.TRANSFER, state.selectedType)
        assertEquals(mockPhysicalWallet, state.selectedWallets.source)
        assertEquals(mockPhysicalWallet2, state.selectedWallets.destination)
        assertTrue(state.isFormValid)
        assertFalse(state.validationErrors.hasErrors)
    }

    @Test
    fun `should maintain immutability in all state updates`() {
        val originalState = CreateTransactionUiState(
            title = "Original",
            amount = "50.00"
        )

        val updatedState = updateTitle(originalState, "Updated")

        // Original state should be unchanged
        assertEquals("Original", originalState.title)
        assertEquals("50.00", originalState.amount)

        // Updated state should have changes
        assertEquals("Updated", updatedState.title)
        assertEquals("50.00", updatedState.amount)

        // Objects should be different instances
        assertNotSame(originalState, updatedState)
    }
}
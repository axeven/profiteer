package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.CreateTransactionUiState
import org.junit.Test
import org.junit.Assert.*

/**
 * Edge case and integration tests for CreateTransactionScreen validation fix.
 *
 * This test class covers scenarios that should continue working correctly
 * after the validation fix:
 * - Pre-selected wallet scenarios
 * - Form interaction validation triggers
 * - Transaction type switching
 * - Complete form submission flows
 */
class CreateTransactionEdgeCaseTest {

    // Mock wallets for testing
    private val mockPhysicalWallet = Wallet(
        id = "physical-123",
        name = "Test Physical Wallet",
        balance = 100.0,
        initialBalance = 100.0,
        walletType = "Physical",
        userId = "user-123"
    )

    private val mockLogicalWallet = Wallet(
        id = "logical-123",
        name = "Test Logical Wallet",
        balance = 50.0,
        initialBalance = 50.0,
        walletType = "Logical",
        userId = "user-123"
    )

    @Test
    fun `should_load_cleanly_with_preselected_wallet_id`() {
        // Test: Pre-selected wallet should not trigger validation errors

        // Simulate initial state with pre-selected wallet (as screen does)
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        ) // No .updateAndValidate() - this is our fix!

        // Simulate wallet pre-selection process (as done in LaunchedEffect)
        val stateWithPreselection = updatePhysicalWallet(initialState, mockPhysicalWallet)

        // Assert: Pre-selection should not introduce validation errors
        // (The updatePhysicalWallet function will trigger validation, which is correct)
        // But the initial load should still be clean
        assertFalse("Initial state should not have validation errors",
                   initialState.validationErrors.hasErrors)

        // After wallet selection, validation may occur (which is expected behavior)
        // The key is that the INITIAL load was clean
        assertEquals("Physical wallet should be selected",
                    mockPhysicalWallet, stateWithPreselection.selectedWallets.physical)
    }

    @Test
    fun `should_load_cleanly_with_initial_transaction_type`() {
        // Test: Different initial transaction types should not trigger validation errors

        TransactionType.values().forEach { transactionType ->
            val initialState = CreateTransactionUiState(
                selectedType = transactionType
            ) // No .updateAndValidate() - this is our fix!

            // Assert: No validation errors regardless of transaction type
            assertFalse("Initial state should not have validation errors for $transactionType",
                       initialState.validationErrors.hasErrors)

            // Assert: Form should be invalid but silent (empty required fields)
            assertFalse("Form should be invalid initially for $transactionType",
                       initialState.isFormValid)

            // Assert: Correct transaction type set
            assertEquals("Transaction type should be set correctly",
                        transactionType, initialState.selectedType)
        }
    }

    @Test
    fun `should_trigger_validation_correctly_after_user_interaction`() {
        // Test: Validation should work correctly after user starts interacting

        // Start with clean initial state
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        )

        // User enters invalid data
        val afterInvalidTitle = updateTitle(initialState, "") // Empty title
        val afterInvalidAmount = updateAmount(afterInvalidTitle, "abc") // Invalid amount

        // Assert: Validation errors should appear after user interaction
        assertTrue("Validation errors should appear after invalid input",
                  afterInvalidAmount.validationErrors.hasErrors)

        assertNotNull("Title error should appear",
                     afterInvalidAmount.validationErrors.titleError)
        assertNotNull("Amount error should appear",
                     afterInvalidAmount.validationErrors.amountError)

        // User corrects the data
        val afterValidTitle = updateTitle(afterInvalidAmount, "Valid Transaction")
        val afterValidAmount = updateAmount(afterValidTitle, "100.00")

        // Assert: Title and amount errors should be resolved
        assertNull("Title error should be resolved",
                  afterValidAmount.validationErrors.titleError)
        assertNull("Amount error should be resolved",
                  afterValidAmount.validationErrors.amountError)
    }

    @Test
    fun `should_handle_transaction_type_switching_correctly`() {
        // Test: Switching transaction types should work correctly and trigger validation when appropriate

        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        )

        // Switch to INCOME - validation will trigger, but that's expected behavior
        val incomeState = updateTransactionType(initialState, TransactionType.INCOME)

        assertEquals("Transaction type should be updated",
                    TransactionType.INCOME, incomeState.selectedType)

        // Validation may show errors for empty fields, which is correct behavior after user interaction

        // Switch to TRANSFER
        val transferState = updateTransactionType(incomeState, TransactionType.TRANSFER)

        assertEquals("Transaction type should be updated to transfer",
                    TransactionType.TRANSFER, transferState.selectedType)

        // Switch back to EXPENSE
        val backToExpenseState = updateTransactionType(transferState, TransactionType.EXPENSE)

        assertEquals("Transaction type should be back to expense",
                    TransactionType.EXPENSE, backToExpenseState.selectedType)

        // The key test: wallet state should be properly managed during transitions
        // When switching from TRANSFER to EXPENSE, transfer-specific wallets should be cleared
        assertNull("Source wallet should be cleared when switching from transfer",
                  backToExpenseState.selectedWallets.source)
        assertNull("Destination wallet should be cleared when switching from transfer",
                  backToExpenseState.selectedWallets.destination)
    }

    @Test
    fun `should_handle_wallet_selection_validation_correctly`() {
        // Test: Wallet selection should trigger appropriate validation

        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        )

        // Select physical wallet - should trigger validation
        val withPhysical = updatePhysicalWallet(initialState, mockPhysicalWallet)

        // Validation should trigger after wallet selection (this is expected)
        // But we check that the wallet is properly selected
        assertEquals("Physical wallet should be selected",
                    mockPhysicalWallet, withPhysical.selectedWallets.physical)

        // Select logical wallet
        val withBothWallets = updateLogicalWallet(withPhysical, mockLogicalWallet)

        assertEquals("Logical wallet should be selected",
                    mockLogicalWallet, withBothWallets.selectedWallets.logical)
        assertEquals("Physical wallet should still be selected",
                    mockPhysicalWallet, withBothWallets.selectedWallets.physical)

        // With both wallets selected, the wallet validation error should be resolved
        // (assuming title and amount are still empty, those errors may remain)
        assertNull("Wallet error should be resolved with both wallets selected",
                  withBothWallets.validationErrors.walletError)
    }

    @Test
    fun `should_complete_full_form_flow_correctly`() {
        // Test: Complete form filling flow from empty to valid

        // Start clean
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        )

        // Step 1: Fill title
        var currentState = updateTitle(initialState, "Test Expense")

        // Step 2: Fill amount
        currentState = updateAmount(currentState, "50.00")

        // Step 3: Select wallets
        currentState = updatePhysicalWallet(currentState, mockPhysicalWallet)
        currentState = updateLogicalWallet(currentState, mockLogicalWallet)

        // Step 4: Add tags (optional)
        currentState = updateTags(currentState, "food, groceries")

        // Assert: Form should be valid with all required fields filled
        assertTrue("Form should be valid with all required fields",
                  currentState.isFormValid)

        // Assert: No validation errors should remain
        assertFalse("No validation errors should remain",
                   currentState.validationErrors.hasErrors)

        // Assert: All data should be correctly set
        assertEquals("Title should be set", "Test Expense", currentState.title)
        assertEquals("Amount should be set", "50.00", currentState.amount)
        assertEquals("Tags should be set", "food, groceries", currentState.tags)
        assertEquals("Physical wallet should be selected",
                    mockPhysicalWallet, currentState.selectedWallets.physical)
        assertEquals("Logical wallet should be selected",
                    mockLogicalWallet, currentState.selectedWallets.logical)
    }

    @Test
    fun `should_handle_transfer_transaction_flow_correctly`() {
        // Test: Transfer transaction specific validation flow

        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.TRANSFER
        )

        // Fill required fields for transfer
        var currentState = updateTitle(initialState, "Transfer Test")
        currentState = updateAmount(currentState, "25.00")

        // Select source wallet
        currentState = updateSourceWallet(currentState, mockPhysicalWallet)

        // Select destination wallet (different from source)
        val destinationWallet = Wallet(
            id = "destination-123",
            name = "Destination Wallet",
            balance = 75.0,
            initialBalance = 75.0,
            walletType = "Physical", // Same type as source for valid transfer
            userId = "user-123"
        )
        currentState = updateDestinationWallet(currentState, destinationWallet)

        // Assert: Transfer should be valid with proper setup
        assertTrue("Transfer should be valid with proper wallets",
                  currentState.isFormValid)

        assertNull("No transfer error should remain",
                  currentState.validationErrors.transferError)

        assertEquals("Source wallet should be set",
                    mockPhysicalWallet, currentState.selectedWallets.source)
        assertEquals("Destination wallet should be set",
                    destinationWallet, currentState.selectedWallets.destination)
    }

    @Test
    fun `should_maintain_immutability_throughout_interactions`() {
        // Test: All state updates should maintain immutability

        val originalState = CreateTransactionUiState(
            selectedType = TransactionType.INCOME
        )

        // Perform various state updates
        val step1 = updateTitle(originalState, "Test")
        val step2 = updateAmount(step1, "100.00")
        val step3 = updatePhysicalWallet(step2, mockPhysicalWallet)

        // Assert: Each step should create new instances (immutability)
        assertNotSame("Step 1 should create new instance", originalState, step1)
        assertNotSame("Step 2 should create new instance", step1, step2)
        assertNotSame("Step 3 should create new instance", step2, step3)

        // Assert: Original state should be unchanged
        assertEquals("Original state should be unchanged", "", originalState.title)
        assertEquals("Original state should be unchanged", "", originalState.amount)
        assertNull("Original state should be unchanged", originalState.selectedWallets.physical)

        // Assert: Final state should have all updates
        assertEquals("Final state should have title", "Test", step3.title)
        assertEquals("Final state should have amount", "100.00", step3.amount)
        assertEquals("Final state should have wallet", mockPhysicalWallet, step3.selectedWallets.physical)
    }
}
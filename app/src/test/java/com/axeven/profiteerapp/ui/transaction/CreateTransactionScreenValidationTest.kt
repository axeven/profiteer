package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.ui.CreateTransactionUiState
import org.junit.Test
import org.junit.Assert.*

/**
 * TDD tests for CreateTransactionScreen validation behavior.
 *
 * These tests define the expected behavior:
 * - Initial state should NOT show validation errors (clean form load)
 * - Form should be invalid but silent initially
 * - Validation errors should only appear after user interaction
 *
 * Phase 1 (Red): These tests should FAIL with current implementation
 * Phase 2 (Green): Fix implementation to make tests pass
 */
class CreateTransactionScreenValidationTest {

    @Test
    fun `should_not_show_validation_errors_on_initial_load`() {
        // Test 1: Initial state should have no validation errors displayed

        // Create initial state as the screen would on first load
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        ).updateAndValidate() // This is the current problematic code

        // Assert: No validation errors should be present on initial load
        assertFalse("Initial state should not have validation errors",
                   initialState.validationErrors.hasErrors)

        // Specifically check that required field errors are not present
        assertNull("Title error should be null on initial load",
                  initialState.validationErrors.titleError)
        assertNull("Amount error should be null on initial load",
                  initialState.validationErrors.amountError)
        assertNull("Wallet error should be null on initial load",
                  initialState.validationErrors.walletError)

        // This test should FAIL with current implementation because
        // .updateAndValidate() immediately triggers validation on empty required fields
    }

    @Test
    fun `should_be_invalid_but_silent_on_initial_load`() {
        // Test 2: Form should be invalid (for submission) but not show errors

        // Create initial state as the screen would on first load
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        ).updateAndValidate() // This is the current problematic code

        // Assert: Form should be invalid for submission (empty required fields)
        assertFalse("Initial state should be invalid for submission",
                   initialState.isFormValid)

        // But: No validation errors should be displayed to user
        assertFalse("No validation errors should be shown initially",
                   initialState.validationErrors.hasErrors)

        // This test should FAIL with current implementation because
        // validation errors are present even though form is correctly invalid
    }

    @Test
    fun `should_show_validation_after_user_input`() {
        // Test 3: Validation should trigger correctly after user interaction

        // Start with clean initial state (what we want to achieve)
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        ) // No .updateAndValidate() call

        // Simulate user entering invalid data
        val afterUserInput = updateTitle(initialState, "") // Empty title
        val withInvalidAmount = updateAmount(afterUserInput, "invalid") // Invalid amount

        // Assert: Now validation errors should appear
        assertTrue("Validation errors should appear after invalid user input",
                  withInvalidAmount.validationErrors.hasErrors)

        assertNotNull("Title error should appear after invalid input",
                     withInvalidAmount.validationErrors.titleError)
        assertNotNull("Amount error should appear after invalid input",
                     withInvalidAmount.validationErrors.amountError)

        // This test should PASS even with current implementation
        // (existing validation logic works correctly during user interaction)
    }

    @Test
    fun `should_transition_from_clean_to_validated_correctly`() {
        // Test 4: Complete flow from initial load to user interaction

        // Phase 1: Clean initial state (target behavior)
        val initialState = CreateTransactionUiState(
            selectedType = TransactionType.EXPENSE
        ) // No premature validation

        assertFalse("Clean initial state should have no errors",
                   initialState.validationErrors.hasErrors)
        assertFalse("Clean initial state should be invalid for submission",
                   initialState.isFormValid)

        // Phase 2: User starts entering data
        val withTitle = updateTitle(initialState, "Test Transaction")

        // Should still have amount error after validation
        assertTrue("Should have validation errors after partial input",
                  withTitle.validationErrors.hasErrors)
        assertNull("Title error should be gone",
                  withTitle.validationErrors.titleError)
        assertNotNull("Amount error should appear after validation",
                     withTitle.validationErrors.amountError)

        // Phase 3: User completes form
        val completeForm = updateAmount(withTitle, "100.00")

        // Form may still be invalid due to missing wallets, but amount error should be gone
        assertNull("Amount error should be gone with valid amount",
                  completeForm.validationErrors.amountError)

        // This test defines the expected user experience flow
    }
}
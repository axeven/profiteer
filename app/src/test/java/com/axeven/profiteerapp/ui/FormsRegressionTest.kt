package com.axeven.profiteerapp.ui

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.EditTransactionUiState
import com.axeven.profiteerapp.data.ui.SettingsUiState
import com.axeven.profiteerapp.data.ui.WalletListUiState
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Regression tests to ensure other forms remain unaffected by CreateTransactionScreen fix.
 *
 * These tests verify that forms which currently work correctly continue to work
 * after we fix the CreateTransactionScreen validation issue.
 *
 * All tests in this class should PASS both before and after the fix.
 */
class FormsRegressionTest {

    @Test
    fun `EditTransactionScreen should load without validation errors`() {
        // Create a mock transaction with valid data
        val mockTransaction = Transaction(
            id = "test-123",
            title = "Test Transaction",
            amount = -50.0,
            type = TransactionType.EXPENSE,
            tags = listOf("food", "lunch"),
            transactionDate = Date(),
            userId = "user-123",
            walletId = "wallet-123", // Legacy field
            affectedWalletIds = listOf("wallet-123")
        )

        val mockWallets = listOf(
            Wallet(
                id = "wallet-123",
                name = "Test Wallet",
                walletType = "Physical",
                userId = "user-123"
            )
        )

        // Create EditTransactionUiState from existing transaction
        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransaction,
            availableWallets = mockWallets
        )

        // Assert: No validation errors on initial load (populated with valid data)
        assertFalse("EditTransactionScreen should not show validation errors on load",
                   editState.validationErrors.hasErrors)

        // Assert: Form should be valid initially (loaded with existing valid data)
        assertTrue("EditTransactionScreen should be valid on load with existing data",
                  editState.isFormValid)

        // Assert: Should not have changes initially (same as original)
        assertFalse("EditTransactionScreen should not show changes initially",
                   editState.hasChanges)

        // This test should PASS - EditTransactionScreen works correctly
    }

    @Test
    fun `SettingsScreen should load without validation errors`() {
        // Create initial SettingsUiState as the screen would
        val settingsState = SettingsUiState()

        // Assert: No validation errors on initial load
        assertFalse("SettingsScreen should not show validation errors on load",
                   settingsState.dialogStates.hasAnyDialogOpen)

        // Assert: Sensible default values
        assertEquals("Default currency should be USD",
                    "USD", settingsState.formData.selectedCurrency)

        // Assert: Rate form should have reasonable defaults
        assertNotNull("Rate form should exist",
                     settingsState.formData.rateFormData)
        assertEquals("Default from currency should be USD",
                    "USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("Default to currency should be EUR",
                    "EUR", settingsState.formData.rateFormData.toCurrency)

        // This test should PASS - SettingsScreen works correctly
    }

    @Test
    fun `WalletListScreen should load without validation errors`() {
        // Create initial WalletListUiState as the screen would
        val walletListState = WalletListUiState()

        // Assert: No dialogs open initially
        assertFalse("WalletListScreen should not have dialogs open on load",
                   walletListState.dialogStates.hasAnyDialogOpen)

        // Assert: No selected wallet for editing initially
        assertNull("No wallet should be selected for editing initially",
                  walletListState.selectedWalletForEdit)

        // Assert: Form should have reasonable defaults
        assertEquals("Default wallet name should be empty",
                    "", walletListState.formData.walletName)
        assertEquals("Default wallet type should be Physical",
                    "Physical", walletListState.formData.selectedWalletType)
        assertEquals("Default initial balance should be 0.00",
                    "0.00", walletListState.formData.initialBalanceText)

        // Assert: Form should be invalid initially (empty name) but no errors shown
        assertFalse("Form should be invalid initially (empty wallet name)",
                   walletListState.isFormValid)

        // This test should PASS - WalletListScreen works correctly
    }

    @Test
    fun `forms should maintain their expected validation behavior after fix`() {
        // This test ensures that our fix doesn't accidentally break other forms

        // Test EditTransactionScreen with invalid edits
        val mockTransaction = Transaction(
            id = "test-123",
            title = "Valid Transaction",
            amount = -100.0,
            type = TransactionType.EXPENSE,
            tags = listOf("test"),
            transactionDate = Date(),
            userId = "user-123",
            walletId = "wallet-123",
            affectedWalletIds = listOf("wallet-123")
        )

        val editState = EditTransactionUiState.fromTransaction(
            transaction = mockTransaction,
            availableWallets = emptyList()
        ).updateAndValidate(
            title = "", // Invalid empty title
            amount = "invalid" // Invalid amount
        )

        // Assert: EditTransactionScreen should show validation errors for invalid input
        assertTrue("EditTransactionScreen should show errors for invalid input",
                  editState.validationErrors.hasErrors)
        assertNotNull("Title error should appear for empty title",
                     editState.validationErrors.titleError)
        assertNotNull("Amount error should appear for invalid amount",
                     editState.validationErrors.amountError)

        // Test SettingsScreen rate form validation
        val settingsWithInvalidRate = SettingsUiState()
            .updateRateValue("") // Empty rate

        // Settings form validation occurs at submission time, not during input
        // So this should not trigger validation errors in the state itself
        assertEquals("Settings should maintain empty rate text",
                    "", settingsWithInvalidRate.formData.rateFormData.rateText)

        // Test WalletListScreen form validation
        val walletListWithInvalidData = WalletListUiState()
            .updateWalletName("") // Empty name
            .updateInitialBalance("invalid") // Invalid balance

        // WalletListScreen should recognize invalid form but not show errors until submission
        assertFalse("Wallet form should be invalid with empty name",
                   walletListWithInvalidData.isFormValid)
        assertEquals("Wallet name should be empty",
                    "", walletListWithInvalidData.formData.walletName)
        assertEquals("Invalid balance text should be preserved",
                    "invalid", walletListWithInvalidData.formData.initialBalanceText)

        // All these behaviors should remain consistent after the CreateTransactionScreen fix
    }

    @Test
    fun `all forms should use proper state management patterns`() {
        // This test verifies that all forms follow the consolidated state management pattern

        // EditTransactionUiState should be immutable
        val originalEditState = EditTransactionUiState.fromTransaction(
            Transaction(
                id = "test",
                title = "Original",
                amount = -50.0,
                type = TransactionType.EXPENSE,
                tags = emptyList(),
                transactionDate = Date(),
                userId = "user",
                walletId = "wallet",
                affectedWalletIds = listOf("wallet")
            ),
            availableWallets = emptyList()
        )

        val updatedEditState = originalEditState.updateAndValidate(title = "Updated")

        assertNotSame("EditTransactionUiState should create new instances (immutability)",
                     originalEditState, updatedEditState)
        assertEquals("Original state should be unchanged",
                    "Original", originalEditState.title)
        assertEquals("Updated state should have new value",
                    "Updated", updatedEditState.title)

        // SettingsUiState should be immutable
        val originalSettingsState = SettingsUiState()
        val updatedSettingsState = originalSettingsState.updateSelectedCurrency("EUR")

        assertNotSame("SettingsUiState should create new instances (immutability)",
                     originalSettingsState, updatedSettingsState)
        assertEquals("Original settings should be unchanged",
                    "USD", originalSettingsState.formData.selectedCurrency)
        assertEquals("Updated settings should have new value",
                    "EUR", updatedSettingsState.formData.selectedCurrency)

        // WalletListUiState should be immutable
        val originalWalletState = WalletListUiState()
        val updatedWalletState = originalWalletState.updateWalletName("New Wallet")

        assertNotSame("WalletListUiState should create new instances (immutability)",
                     originalWalletState, updatedWalletState)
        assertEquals("Original wallet state should be unchanged",
                    "", originalWalletState.formData.walletName)
        assertEquals("Updated wallet state should have new value",
                    "New Wallet", updatedWalletState.formData.walletName)

        // This verifies that all forms follow the same immutable state management pattern
    }
}
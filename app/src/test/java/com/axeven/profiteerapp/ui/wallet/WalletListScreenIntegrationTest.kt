package com.axeven.profiteerapp.ui.wallet

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration tests for WalletListScreen migration to consolidated state.
 *
 * These tests verify that the screen properly uses WalletListUiState
 * instead of scattered mutableStateOf variables.
 */
class WalletListScreenIntegrationTest {

    // Mock data for testing
    private val mockPhysicalWallet = Wallet(
        id = "physical-1",
        name = "Main Account",
        balance = 1000.0,
        walletType = "Physical",
        physicalForm = PhysicalForm.FIAT_CURRENCY,
        initialBalance = 1000.0
    )

    private val mockLogicalWallet = Wallet(
        id = "logical-1",
        name = "Savings Goal",
        balance = 500.0,
        walletType = "Logical",
        physicalForm = PhysicalForm.FIAT_CURRENCY,
        initialBalance = 500.0
    )

    @Test
    fun `screen should use single WalletListUiState instead of multiple mutableStateOf`() {
        // Verify that WalletListUiState can replace the 3 scattered mutableStateOf variables:
        // - showCreateWalletDialog
        // - showEditWalletDialog
        // - walletToEdit

        val walletListState = WalletListUiState()

        // Initial state should have all dialogs closed
        assertFalse(walletListState.dialogStates.showCreateWalletDialog)
        assertFalse(walletListState.dialogStates.showEditWalletDialog)
        assertNull(walletListState.selectedWalletForEdit)
        assertFalse(walletListState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should handle create wallet dialog state management`() {
        var walletListState = WalletListUiState()

        // Open create dialog (replaces showCreateWalletDialog = true)
        walletListState = walletListState.openDialog(WalletListDialogType.CREATE)
        assertTrue(walletListState.dialogStates.showCreateWalletDialog)
        assertFalse(walletListState.dialogStates.showEditWalletDialog)
        assertNull(walletListState.selectedWalletForEdit)

        // Update form data
        walletListState = walletListState
            .updateWalletName("New Wallet")
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.FIAT_CURRENCY)
            .updateInitialBalance("100.00")

        // Verify form data
        assertEquals("New Wallet", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.FIAT_CURRENCY, walletListState.formData.selectedPhysicalForm)
        assertEquals("100.00", walletListState.formData.initialBalanceText)
        assertTrue(walletListState.isFormValid)

        // Close dialog (replaces showCreateWalletDialog = false)
        walletListState = walletListState.closeAllDialogs()
        assertFalse(walletListState.dialogStates.showCreateWalletDialog)
    }

    @Test
    fun `should handle edit wallet dialog state management`() {
        var walletListState = WalletListUiState()

        // Open edit dialog with wallet (replaces walletToEdit = wallet, showEditWalletDialog = true)
        walletListState = walletListState.openEditWalletDialog(mockPhysicalWallet)
        assertFalse(walletListState.dialogStates.showCreateWalletDialog)
        assertTrue(walletListState.dialogStates.showEditWalletDialog)
        assertEquals(mockPhysicalWallet, walletListState.selectedWalletForEdit)

        // Initialize form from wallet
        walletListState = walletListState.initializeEditForm(mockPhysicalWallet)
        assertEquals("Main Account", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.FIAT_CURRENCY, walletListState.formData.selectedPhysicalForm)
        assertEquals("1000.0", walletListState.formData.initialBalanceText)

        // Update wallet name
        walletListState = walletListState.updateWalletName("Updated Account")
        assertEquals("Updated Account", walletListState.formData.walletName)

        // Close dialog (replaces showEditWalletDialog = false, walletToEdit = null)
        walletListState = walletListState.closeAllDialogs()
        assertFalse(walletListState.dialogStates.showEditWalletDialog)
        assertNull(walletListState.selectedWalletForEdit)
    }

    @Test
    fun `should manage dropdown states within dialogs`() {
        var walletListState = WalletListUiState()
            .openDialog(WalletListDialogType.CREATE)

        // All dropdowns should be closed initially
        assertFalse(walletListState.formData.dropdownStates.showWalletTypeDropdown)

        // Open wallet type dropdown
        walletListState = walletListState.updateDropdownState(WalletDropdownType.WALLET_TYPE, true)
        assertTrue(walletListState.formData.dropdownStates.showWalletTypeDropdown)

        // Select wallet type and close dropdown
        walletListState = walletListState
            .updateWalletType("Logical")
            .updateDropdownState(WalletDropdownType.WALLET_TYPE, false)

        assertEquals("Logical", walletListState.formData.selectedWalletType)
        assertFalse(walletListState.formData.dropdownStates.showWalletTypeDropdown)

        // Close all dropdowns
        walletListState = walletListState.closeAllDropdowns()
        assertFalse(walletListState.formData.dropdownStates.showWalletTypeDropdown)
    }

    @Test
    fun `should enforce single dialog open business rule`() {
        var walletListState = WalletListUiState()

        // Open create dialog
        walletListState = walletListState.openDialog(WalletListDialogType.CREATE)
        assertTrue(walletListState.dialogStates.showCreateWalletDialog)
        assertFalse(walletListState.dialogStates.showEditWalletDialog)

        // Opening edit dialog should close create dialog
        walletListState = walletListState.openEditWalletDialog(mockPhysicalWallet)
        assertFalse(walletListState.dialogStates.showCreateWalletDialog)
        assertTrue(walletListState.dialogStates.showEditWalletDialog)
        assertEquals(mockPhysicalWallet, walletListState.selectedWalletForEdit)

        // Only one dialog should be open at a time
        assertTrue(walletListState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should handle physical wallet creation workflow`() {
        var walletListState = WalletListUiState()
            .openDialog(WalletListDialogType.CREATE)

        // Set up physical wallet
        walletListState = walletListState
            .updateWalletName("Investment Account")
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.STOCKS)
            .updateInitialBalance("5000.00")

        assertEquals("Investment Account", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.STOCKS, walletListState.formData.selectedPhysicalForm)
        assertEquals("5000.00", walletListState.formData.initialBalanceText)
        assertTrue(walletListState.isFormValid)

        // Form should be ready for submission
        val summary = walletListState.getWalletSummary()
        assertEquals("Investment Account", summary.name)
        assertEquals("Physical", summary.walletType)
        assertEquals(PhysicalForm.STOCKS, summary.physicalForm)
        assertEquals(5000.0, summary.initialBalance, 0.01)
        assertTrue(summary.isValid)
    }

    @Test
    fun `should handle logical wallet creation workflow`() {
        var walletListState = WalletListUiState()
            .openDialog(WalletListDialogType.CREATE)

        // Set up logical wallet
        walletListState = walletListState
            .updateWalletName("Emergency Fund")
            .updateWalletType("Logical")
            .updateInitialBalance("1000.00")

        assertEquals("Emergency Fund", walletListState.formData.walletName)
        assertEquals("Logical", walletListState.formData.selectedWalletType)
        assertEquals("1000.00", walletListState.formData.initialBalanceText)
        assertTrue(walletListState.isFormValid)

        // Physical form is not relevant for logical wallets but preserved
        assertEquals(PhysicalForm.FIAT_CURRENCY, walletListState.formData.selectedPhysicalForm)
    }

    @Test
    fun `should handle precious metals wallet creation workflow`() {
        var walletListState = WalletListUiState()
            .openDialog(WalletListDialogType.CREATE)

        // Set up precious metals wallet
        walletListState = walletListState
            .updateWalletName("Gold Portfolio")
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.PRECIOUS_METALS)
            .updateInitialBalance("50.5")

        assertEquals("Gold Portfolio", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.PRECIOUS_METALS, walletListState.formData.selectedPhysicalForm)
        assertEquals("50.5", walletListState.formData.initialBalanceText)
        assertTrue(walletListState.isFormValid)
    }

    @Test
    fun `should handle cryptocurrency wallet creation workflow`() {
        var walletListState = WalletListUiState()
            .openDialog(WalletListDialogType.CREATE)

        // Set up cryptocurrency wallet
        walletListState = walletListState
            .updateWalletName("Bitcoin Holdings")
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.CRYPTOCURRENCY)
            .updateInitialBalance("0.5")

        assertEquals("Bitcoin Holdings", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.CRYPTOCURRENCY, walletListState.formData.selectedPhysicalForm)
        assertEquals("0.5", walletListState.formData.initialBalanceText)
        assertTrue(walletListState.isFormValid)
    }

    @Test
    fun `should handle wallet editing with different wallet types`() {
        // Test editing physical wallet
        var walletListState = WalletListUiState()
            .openEditWalletDialog(mockPhysicalWallet)
            .initializeEditForm(mockPhysicalWallet)

        assertEquals("Main Account", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.FIAT_CURRENCY, walletListState.formData.selectedPhysicalForm)

        // Test editing logical wallet
        walletListState = WalletListUiState()
            .openEditWalletDialog(mockLogicalWallet)
            .initializeEditForm(mockLogicalWallet)

        assertEquals("Savings Goal", walletListState.formData.walletName)
        assertEquals("Logical", walletListState.formData.selectedWalletType)
        assertEquals("500.0", walletListState.formData.initialBalanceText)
    }

    @Test
    fun `should validate form data correctly`() {
        var walletListState = WalletListUiState()

        // Invalid: empty wallet name
        walletListState = walletListState
            .updateWalletName("")
            .updateWalletType("Physical")
            .updateInitialBalance("100.00")
        assertFalse(walletListState.isFormValid)

        // Invalid: non-numeric balance
        walletListState = walletListState
            .updateWalletName("Valid Wallet")
            .updateInitialBalance("invalid")
        assertFalse(walletListState.isFormValid)

        // Invalid: negative balance
        walletListState = walletListState.updateInitialBalance("-100.00")
        assertFalse(walletListState.isFormValid)

        // Valid: all fields filled correctly
        walletListState = walletListState
            .updateWalletName("Valid Wallet")
            .updateWalletType("Physical")
            .updateInitialBalance("100.00")
        assertTrue(walletListState.isFormValid)
    }

    @Test
    fun `should reset form data after operations`() {
        var walletListState = WalletListUiState()
            .openDialog(WalletListDialogType.CREATE)
            .updateWalletName("Test Wallet")
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.STOCKS)
            .updateInitialBalance("1000.00")

        // Verify form has data
        val beforeReset = walletListState.formData
        assertEquals("Test Wallet", beforeReset.walletName)
        assertEquals("Physical", beforeReset.selectedWalletType)
        assertEquals(PhysicalForm.STOCKS, beforeReset.selectedPhysicalForm)
        assertEquals("1000.00", beforeReset.initialBalanceText)

        // Reset form
        walletListState = walletListState.resetForm()

        // Verify form is reset to defaults
        val afterReset = walletListState.formData
        assertEquals("", afterReset.walletName)
        assertEquals("Physical", afterReset.selectedWalletType)
        assertEquals(PhysicalForm.FIAT_CURRENCY, afterReset.selectedPhysicalForm)
        assertEquals("0.00", afterReset.initialBalanceText)
    }

    @Test
    fun `should preserve form data across dialog state changes`() {
        var walletListState = WalletListUiState()
            .updateWalletName("Test Wallet")
            .updateWalletType("Logical")
            .updateInitialBalance("500.00")

        // Open and close dialogs
        walletListState = walletListState
            .openDialog(WalletListDialogType.CREATE)
            .closeAllDialogs()

        // Form data should be preserved
        assertEquals("Test Wallet", walletListState.formData.walletName)
        assertEquals("Logical", walletListState.formData.selectedWalletType)
        assertEquals("500.00", walletListState.formData.initialBalanceText)
    }

    @Test
    fun `should handle complete wallet creation workflow`() {
        var walletListState = WalletListUiState()

        // 1. Open create dialog
        walletListState = walletListState.openDialog(WalletListDialogType.CREATE)
        assertTrue(walletListState.dialogStates.showCreateWalletDialog)
        assertTrue(walletListState.dialogStates.hasAnyDialogOpen)

        // 2. Fill form data
        walletListState = walletListState
            .updateWalletName("Investment Portfolio")
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.ETFS)
            .updateInitialBalance("10000.00")

        assertTrue(walletListState.isFormValid)

        // 3. Get summary for submission
        val summary = walletListState.getWalletSummary()
        assertEquals("Investment Portfolio", summary.name)
        assertEquals("Physical", summary.walletType)
        assertEquals(PhysicalForm.ETFS, summary.physicalForm)
        assertEquals(10000.0, summary.initialBalance, 0.01)
        assertTrue(summary.isValid)

        // 4. After submission, reset form and close dialog
        walletListState = walletListState.resetForm().closeAllDialogs()
        assertEquals(WalletFormData(), walletListState.formData)
        assertFalse(walletListState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should handle complete wallet editing workflow`() {
        var walletListState = WalletListUiState()

        // 1. Open edit dialog with existing wallet
        walletListState = walletListState.openEditWalletDialog(mockPhysicalWallet)
        assertTrue(walletListState.dialogStates.showEditWalletDialog)
        assertEquals(mockPhysicalWallet, walletListState.selectedWalletForEdit)

        // 2. Initialize form from wallet
        walletListState = walletListState.initializeEditForm(mockPhysicalWallet)
        assertEquals("Main Account", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)

        // 3. Make changes
        walletListState = walletListState
            .updateWalletName("Updated Main Account")
            .updateInitialBalance("1200.00")

        assertTrue(walletListState.isFormValid)

        // 4. Get summary for update
        val summary = walletListState.getWalletSummary()
        assertEquals("Updated Main Account", summary.name)
        assertEquals("Physical", summary.walletType)
        assertEquals(1200.0, summary.initialBalance, 0.01)
        assertTrue(summary.isValid)

        // 5. Close dialog after saving
        walletListState = walletListState.closeAllDialogs()
        assertFalse(walletListState.dialogStates.showEditWalletDialog)
        assertNull(walletListState.selectedWalletForEdit)
    }

    @Test
    fun `should handle wallet type switching correctly`() {
        var walletListState = WalletListUiState()
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.REAL_ESTATE)

        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.REAL_ESTATE, walletListState.formData.selectedPhysicalForm)

        // Switch to logical wallet type
        walletListState = walletListState.updateWalletType("Logical")
        assertEquals("Logical", walletListState.formData.selectedWalletType)
        // Physical form is preserved even for logical wallets
        assertEquals(PhysicalForm.REAL_ESTATE, walletListState.formData.selectedPhysicalForm)

        // Switch back to physical
        walletListState = walletListState.updateWalletType("Physical")
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.REAL_ESTATE, walletListState.formData.selectedPhysicalForm)
    }

    @Test
    fun `should handle error cases gracefully`() {
        var walletListState = WalletListUiState()

        // Test with invalid balance amounts
        val invalidBalances = listOf("", "abc", "-100", "not_a_number", "100.00.00")

        invalidBalances.forEach { invalidBalance ->
            walletListState = walletListState
                .updateWalletName("Valid Wallet")
                .updateWalletType("Physical")
                .updateInitialBalance(invalidBalance)

            assertFalse("Should be invalid for balance: $invalidBalance", walletListState.isFormValid)
        }

        // Test with valid balance
        walletListState = walletListState.updateInitialBalance("100.00")
        assertTrue(walletListState.isFormValid)

        // Test with empty wallet name
        walletListState = walletListState.updateWalletName("")
        assertFalse(walletListState.isFormValid)
    }

    @Test
    fun `should handle different physical form combinations`() {
        val physicalForms = listOf(
            PhysicalForm.FIAT_CURRENCY,
            PhysicalForm.CRYPTOCURRENCY,
            PhysicalForm.PRECIOUS_METALS,
            PhysicalForm.STOCKS,
            PhysicalForm.ETFS,
            PhysicalForm.BONDS,
            PhysicalForm.MUTUAL_FUNDS,
            PhysicalForm.REAL_ESTATE,
            PhysicalForm.COMMODITIES,
            PhysicalForm.CASH_EQUIVALENT,
            PhysicalForm.OTHER
        )

        physicalForms.forEach { form ->
            val walletListState = WalletListUiState()
                .updateWalletName("Test ${form.displayName} Wallet")
                .updateWalletType("Physical")
                .updatePhysicalForm(form)
                .updateInitialBalance("100.00")

            assertEquals(form, walletListState.formData.selectedPhysicalForm)
            assertTrue("Should be valid for form: ${form.displayName}", walletListState.isFormValid)

            val summary = walletListState.getWalletSummary()
            assertEquals(form, summary.physicalForm)
            assertTrue(summary.isValid)
        }
    }

    @Test
    fun `should handle dropdown interaction patterns`() {
        var walletListState = WalletListUiState()
            .openDialog(WalletListDialogType.CREATE)

        // Test dropdown opening and closing
        assertFalse(walletListState.formData.dropdownStates.showWalletTypeDropdown)

        // Open dropdown
        walletListState = walletListState.updateDropdownState(WalletDropdownType.WALLET_TYPE, true)
        assertTrue(walletListState.formData.dropdownStates.showWalletTypeDropdown)

        // Select option and close dropdown
        walletListState = walletListState
            .updateWalletType("Logical")
            .updateDropdownState(WalletDropdownType.WALLET_TYPE, false)

        assertEquals("Logical", walletListState.formData.selectedWalletType)
        assertFalse(walletListState.formData.dropdownStates.showWalletTypeDropdown)

        // Test close all dropdowns
        walletListState = walletListState
            .updateDropdownState(WalletDropdownType.WALLET_TYPE, true)
            .closeAllDropdowns()

        assertFalse(walletListState.formData.dropdownStates.showWalletTypeDropdown)
    }
}
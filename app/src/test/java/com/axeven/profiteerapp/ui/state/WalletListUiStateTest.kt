package com.axeven.profiteerapp.ui.state

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Test-Driven Development tests for WalletListUiState.
 *
 * Following TDD methodology - these tests define the expected behavior for the
 * wallet list state management. They will initially fail (RED phase)
 * and will be made to pass during implementation (GREEN phase).
 */
class WalletListUiStateTest {

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

    // Tests for WalletListUiState construction and initialization
    @Test
    fun `should create default WalletListUiState`() {
        val walletListState = WalletListUiState()

        assertEquals(WalletListDialogStates(), walletListState.dialogStates)
        assertNull(walletListState.selectedWalletForEdit)
        assertEquals(WalletFormData(), walletListState.formData)
        assertFalse(walletListState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should create WalletListUiState with initial form data`() {
        val initialFormData = WalletFormData(
            walletName = "Test Wallet",
            selectedWalletType = "Physical",
            selectedPhysicalForm = PhysicalForm.PRECIOUS_METALS
        )

        val walletListState = WalletListUiState(
            formData = initialFormData
        )

        assertEquals("Test Wallet", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.PRECIOUS_METALS, walletListState.formData.selectedPhysicalForm)
    }

    // Tests for dialog state management
    @Test
    fun `should open create wallet dialog and close others`() {
        val walletListState = WalletListUiState()

        val openedState = walletListState.openDialog(WalletListDialogType.CREATE)

        assertTrue(openedState.dialogStates.showCreateWalletDialog)
        assertFalse(openedState.dialogStates.showEditWalletDialog)
        assertTrue(openedState.dialogStates.hasAnyDialogOpen)
        assertNull(openedState.selectedWalletForEdit)
    }

    @Test
    fun `should open edit wallet dialog with selected wallet`() {
        val walletListState = WalletListUiState()

        val openedState = walletListState.openEditWalletDialog(mockPhysicalWallet)

        assertFalse(openedState.dialogStates.showCreateWalletDialog)
        assertTrue(openedState.dialogStates.showEditWalletDialog)
        assertEquals(mockPhysicalWallet, openedState.selectedWalletForEdit)
        assertTrue(openedState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should close all dialogs`() {
        val walletListState = WalletListUiState(
            dialogStates = WalletListDialogStates(
                showCreateWalletDialog = true,
                showEditWalletDialog = true
            ),
            selectedWalletForEdit = mockPhysicalWallet
        )

        val closedState = walletListState.closeAllDialogs()

        assertFalse(closedState.dialogStates.showCreateWalletDialog)
        assertFalse(closedState.dialogStates.showEditWalletDialog)
        assertNull(closedState.selectedWalletForEdit)
        assertFalse(closedState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should enforce single dialog open business rule`() {
        val walletListState = WalletListUiState()
            .openDialog(WalletListDialogType.CREATE)
            .openEditWalletDialog(mockPhysicalWallet)

        // Only the last opened dialog should be open
        assertFalse(walletListState.dialogStates.showCreateWalletDialog)
        assertTrue(walletListState.dialogStates.showEditWalletDialog)
        assertEquals(mockPhysicalWallet, walletListState.selectedWalletForEdit)
    }

    // Tests for form data management
    @Test
    fun `should update wallet name`() {
        val walletListState = WalletListUiState()

        val updatedState = walletListState.updateWalletName("New Wallet")

        assertEquals("New Wallet", updatedState.formData.walletName)
        assertEquals("", walletListState.formData.walletName) // Original unchanged
    }

    @Test
    fun `should update wallet type`() {
        val walletListState = WalletListUiState()

        val updatedState = walletListState.updateWalletType("Logical")

        assertEquals("Logical", updatedState.formData.selectedWalletType)
    }

    @Test
    fun `should update physical form`() {
        val walletListState = WalletListUiState()

        val updatedState = walletListState.updatePhysicalForm(PhysicalForm.PRECIOUS_METALS)

        assertEquals(PhysicalForm.PRECIOUS_METALS, updatedState.formData.selectedPhysicalForm)
    }

    @Test
    fun `should update initial balance`() {
        val walletListState = WalletListUiState()

        val updatedState = walletListState.updateInitialBalance("500.00")

        assertEquals("500.00", updatedState.formData.initialBalanceText)
    }

    @Test
    fun `should reset form data`() {
        val walletListState = WalletListUiState(
            formData = WalletFormData(
                walletName = "Test Wallet",
                selectedWalletType = "Logical",
                selectedPhysicalForm = PhysicalForm.PRECIOUS_METALS,
                initialBalanceText = "1000.00"
            )
        )

        val resetState = walletListState.resetForm()

        assertEquals(WalletFormData(), resetState.formData)
    }

    // Tests for edit wallet initialization
    @Test
    fun `should initialize form from wallet for editing`() {
        val walletToEdit = Wallet(
            id = "wallet-1",
            name = "Test Account",
            balance = 1500.0,
            walletType = "Physical",
            physicalForm = PhysicalForm.PRECIOUS_METALS,
            initialBalance = 1000.0
        )

        val walletListState = WalletListUiState().initializeEditForm(walletToEdit)

        assertEquals("Test Account", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.PRECIOUS_METALS, walletListState.formData.selectedPhysicalForm)
        assertEquals("1000.0", walletListState.formData.initialBalanceText)
    }

    @Test
    fun `should initialize form for logical wallet`() {
        val logicalWallet = Wallet(
            id = "logical-1",
            name = "Savings",
            balance = 800.0,
            walletType = "Logical",
            physicalForm = PhysicalForm.FIAT_CURRENCY,
            initialBalance = 800.0
        )

        val walletListState = WalletListUiState().initializeEditForm(logicalWallet)

        assertEquals("Savings", walletListState.formData.walletName)
        assertEquals("Logical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.FIAT_CURRENCY, walletListState.formData.selectedPhysicalForm)
        assertEquals("800.0", walletListState.formData.initialBalanceText)
    }

    // Tests for dropdown state management
    @Test
    fun `should manage wallet type dropdown state`() {
        val walletListState = WalletListUiState()

        val openedState = walletListState.updateDropdownState(WalletDropdownType.WALLET_TYPE, true)
        assertTrue(openedState.formData.dropdownStates.showWalletTypeDropdown)

        val closedState = openedState.updateDropdownState(WalletDropdownType.WALLET_TYPE, false)
        assertFalse(closedState.formData.dropdownStates.showWalletTypeDropdown)
    }

    @Test
    fun `should close all dropdowns`() {
        val walletListState = WalletListUiState(
            formData = WalletFormData(
                dropdownStates = WalletDropdownStates(
                    showWalletTypeDropdown = true
                )
            )
        )

        val closedState = walletListState.closeAllDropdowns()

        assertFalse(closedState.formData.dropdownStates.showWalletTypeDropdown)
    }

    // Tests for validation
    @Test
    fun `should validate wallet form data`() {
        val validFormData = WalletFormData(
            walletName = "Valid Wallet",
            selectedWalletType = "Physical",
            selectedPhysicalForm = PhysicalForm.FIAT_CURRENCY,
            initialBalanceText = "100.00"
        )

        val walletListState = WalletListUiState(
            formData = validFormData
        )

        assertTrue(walletListState.isFormValid)
    }

    @Test
    fun `should reject invalid wallet form data`() {
        val invalidFormData = WalletFormData(
            walletName = "",
            selectedWalletType = "Physical",
            selectedPhysicalForm = PhysicalForm.FIAT_CURRENCY,
            initialBalanceText = "invalid"
        )

        val walletListState = WalletListUiState(
            formData = invalidFormData
        )

        assertFalse(walletListState.isFormValid)
    }

    @Test
    fun `should reject empty wallet name`() {
        val invalidFormData = WalletFormData(
            walletName = "",
            selectedWalletType = "Physical",
            selectedPhysicalForm = PhysicalForm.FIAT_CURRENCY,
            initialBalanceText = "100.00"
        )

        val walletListState = WalletListUiState(
            formData = invalidFormData
        )

        assertFalse(walletListState.isFormValid)
    }

    @Test
    fun `should reject invalid balance amount`() {
        val invalidFormData = WalletFormData(
            walletName = "Valid Wallet",
            selectedWalletType = "Physical",
            selectedPhysicalForm = PhysicalForm.FIAT_CURRENCY,
            initialBalanceText = "not_a_number"
        )

        val walletListState = WalletListUiState(
            formData = invalidFormData
        )

        assertFalse(walletListState.isFormValid)
    }

    // Tests for immutability and state copying
    @Test
    fun `should maintain immutability during updates`() {
        val originalState = WalletListUiState()
        val updatedState = originalState.updateWalletName("New Wallet")

        // Original state should be unchanged
        assertEquals("", originalState.formData.walletName)

        // Updated state should have changes
        assertEquals("New Wallet", updatedState.formData.walletName)

        // Objects should be different instances
        assertNotSame(originalState, updatedState)
        assertNotSame(originalState.formData, updatedState.formData)
    }

    @Test
    fun `should preserve other state during dialog updates`() {
        val walletListState = WalletListUiState(
            formData = WalletFormData(
                walletName = "Test Wallet",
                selectedWalletType = "Physical",
                selectedPhysicalForm = PhysicalForm.PRECIOUS_METALS
            )
        )

        val dialogOpenedState = walletListState.openDialog(WalletListDialogType.CREATE)

        // Form data should be preserved
        assertEquals("Test Wallet", dialogOpenedState.formData.walletName)
        assertEquals("Physical", dialogOpenedState.formData.selectedWalletType)
        assertEquals(PhysicalForm.PRECIOUS_METALS, dialogOpenedState.formData.selectedPhysicalForm)

        // Dialog state should be updated
        assertTrue(dialogOpenedState.dialogStates.showCreateWalletDialog)
    }

    // Tests for complex wallet management scenarios
    @Test
    fun `should handle complete wallet creation flow`() {
        var walletListState = WalletListUiState()

        // Open create dialog
        walletListState = walletListState.openDialog(WalletListDialogType.CREATE)
        assertTrue(walletListState.dialogStates.showCreateWalletDialog)

        // Update form data
        walletListState = walletListState
            .updateWalletName("New Account")
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.FIAT_CURRENCY)
            .updateInitialBalance("1000.00")

        assertEquals("New Account", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.FIAT_CURRENCY, walletListState.formData.selectedPhysicalForm)
        assertEquals("1000.00", walletListState.formData.initialBalanceText)
        assertTrue(walletListState.isFormValid)

        // Reset form after submission
        walletListState = walletListState.resetForm().closeAllDialogs()
        assertEquals(WalletFormData(), walletListState.formData)
        assertFalse(walletListState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should handle complete wallet editing flow`() {
        var walletListState = WalletListUiState()

        // Open edit dialog with existing wallet
        walletListState = walletListState.openEditWalletDialog(mockPhysicalWallet)
        assertTrue(walletListState.dialogStates.showEditWalletDialog)
        assertEquals(mockPhysicalWallet, walletListState.selectedWalletForEdit)

        // Initialize form from wallet
        walletListState = walletListState.initializeEditForm(mockPhysicalWallet)
        assertEquals("Main Account", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.FIAT_CURRENCY, walletListState.formData.selectedPhysicalForm)

        // Update wallet name
        walletListState = walletListState.updateWalletName("Updated Account")
        assertEquals("Updated Account", walletListState.formData.walletName)
        assertTrue(walletListState.isFormValid)

        // Close dialog after saving
        walletListState = walletListState.closeAllDialogs()
        assertFalse(walletListState.dialogStates.showEditWalletDialog)
        assertNull(walletListState.selectedWalletForEdit)
    }

    @Test
    fun `should handle wallet type changes correctly`() {
        var walletListState = WalletListUiState()
            .updateWalletType("Physical")
            .updatePhysicalForm(PhysicalForm.PRECIOUS_METALS)

        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals(PhysicalForm.PRECIOUS_METALS, walletListState.formData.selectedPhysicalForm)

        // Change to logical wallet type
        walletListState = walletListState.updateWalletType("Logical")
        assertEquals("Logical", walletListState.formData.selectedWalletType)
        // Physical form should still be preserved for when switching back
        assertEquals(PhysicalForm.PRECIOUS_METALS, walletListState.formData.selectedPhysicalForm)
    }

    @Test
    fun `should handle different physical forms`() {
        val forms = listOf(
            PhysicalForm.FIAT_CURRENCY,
            PhysicalForm.PRECIOUS_METALS,
            PhysicalForm.STOCKS,
            PhysicalForm.CRYPTOCURRENCY,
            PhysicalForm.REAL_ESTATE,
            PhysicalForm.COMMODITIES,
            PhysicalForm.CASH_EQUIVALENT
        )

        forms.forEach { form ->
            val walletListState = WalletListUiState()
                .updateWalletType("Physical")
                .updatePhysicalForm(form)
                .updateWalletName("Test Wallet")
                .updateInitialBalance("100.00")

            assertEquals(form, walletListState.formData.selectedPhysicalForm)
            assertTrue(walletListState.isFormValid)
        }
    }

    @Test
    fun `should handle dropdown interactions`() {
        var walletListState = WalletListUiState()

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
    fun `should handle error cases gracefully`() {
        var walletListState = WalletListUiState()

        // Test with invalid balance amounts
        val invalidBalances = listOf("", "abc", "-100", "not_a_number")

        invalidBalances.forEach { invalidBalance ->
            walletListState = walletListState
                .updateWalletName("Valid Wallet")
                .updateInitialBalance(invalidBalance)

            assertFalse(walletListState.isFormValid)
        }

        // Test with valid balance
        walletListState = walletListState.updateInitialBalance("100.00")
        assertTrue(walletListState.isFormValid)
    }

    // Performance tests
    @Test
    fun `should maintain performance with state updates`() {
        var walletListState = WalletListUiState()

        val startTime = System.currentTimeMillis()

        for (i in 1..100) {
            walletListState = walletListState
                .updateWalletName("Wallet $i")
                .updateWalletType(if (i % 2 == 0) "Physical" else "Logical")
                .updateInitialBalance("$i.00")
                .openDialog(WalletListDialogType.CREATE)
                .closeAllDialogs()
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete 500 state updates in under 50ms
        assertTrue("Wallet list state updates should be fast, took ${duration}ms", duration < 50)

        // Verify final state
        assertEquals("Wallet 100", walletListState.formData.walletName)
        assertEquals("Physical", walletListState.formData.selectedWalletType)
        assertEquals("100.00", walletListState.formData.initialBalanceText)
        assertFalse(walletListState.dialogStates.hasAnyDialogOpen)
    }

    // Tests for form defaults and initialization
    @Test
    fun `should initialize form with correct defaults for wallet type`() {
        val physicalState = WalletListUiState()
            .updateWalletType("Physical")

        assertEquals("Physical", physicalState.formData.selectedWalletType)
        assertEquals(PhysicalForm.FIAT_CURRENCY, physicalState.formData.selectedPhysicalForm)

        val logicalState = WalletListUiState()
            .updateWalletType("Logical")

        assertEquals("Logical", logicalState.formData.selectedWalletType)
        // Physical form is not relevant for logical wallets but preserved
        assertEquals(PhysicalForm.FIAT_CURRENCY, logicalState.formData.selectedPhysicalForm)
    }

    @Test
    fun `should provide transaction summary for wallet creation`() {
        val walletListState = WalletListUiState(
            formData = WalletFormData(
                walletName = "New Wallet",
                selectedWalletType = "Physical",
                selectedPhysicalForm = PhysicalForm.PRECIOUS_METALS,
                initialBalanceText = "50.5"
            )
        )

        val summary = walletListState.getWalletSummary()

        assertEquals("New Wallet", summary.name)
        assertEquals("Physical", summary.walletType)
        assertEquals(PhysicalForm.PRECIOUS_METALS, summary.physicalForm)
        assertEquals(50.5, summary.initialBalance, 0.01)
        assertTrue(summary.isValid)
    }

    @Test
    fun `should handle wallet summary with invalid data`() {
        val walletListState = WalletListUiState(
            formData = WalletFormData(
                walletName = "",
                selectedWalletType = "Physical",
                selectedPhysicalForm = PhysicalForm.PRECIOUS_METALS,
                initialBalanceText = "invalid"
            )
        )

        val summary = walletListState.getWalletSummary()

        assertEquals("", summary.name)
        assertEquals("Physical", summary.walletType)
        assertEquals(PhysicalForm.PRECIOUS_METALS, summary.physicalForm)
        assertEquals(0.0, summary.initialBalance, 0.01)
        assertFalse(summary.isValid)
    }
}
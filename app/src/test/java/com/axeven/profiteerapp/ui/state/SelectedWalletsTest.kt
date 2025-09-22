package com.axeven.profiteerapp.ui.state

import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.SelectedWallets
import com.axeven.profiteerapp.data.ui.WalletRole
import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive tests for SelectedWallets data class.
 * Validates wallet selection logic and convenience methods.
 */
class SelectedWalletsTest {

    private val physicalWallet = Wallet(
        id = "physical_1",
        name = "Physical Wallet",
        balance = 1000.0,
        initialBalance = 0.0,
        walletType = "Physical",
        userId = "user_1"
    )

    private val logicalWallet = Wallet(
        id = "logical_1",
        name = "Logical Wallet",
        balance = 500.0,
        initialBalance = 0.0,
        walletType = "Logical",
        userId = "user_1"
    )

    private val physicalWallet2 = Wallet(
        id = "physical_2",
        name = "Physical Wallet 2",
        balance = 800.0,
        initialBalance = 0.0,
        walletType = "Physical",
        userId = "user_1"
    )

    @Test
    fun `should validate regular transaction wallet selection`() {
        val emptySelection = SelectedWallets()
        assertFalse("Empty selection should be invalid", emptySelection.isValidForTransaction)

        val physicalOnly = SelectedWallets(physical = physicalWallet)
        assertTrue("Physical wallet only should be valid", physicalOnly.isValidForTransaction)

        val logicalOnly = SelectedWallets(logical = logicalWallet)
        assertTrue("Logical wallet only should be valid", logicalOnly.isValidForTransaction)

        val both = SelectedWallets(physical = physicalWallet, logical = logicalWallet)
        assertTrue("Both wallets should be valid", both.isValidForTransaction)
    }

    @Test
    fun `should validate transfer wallet selection`() {
        val emptySelection = SelectedWallets()
        assertFalse("Empty selection should be invalid for transfer", emptySelection.isValidForTransfer)

        val sourceOnly = SelectedWallets(source = physicalWallet)
        assertFalse("Source only should be invalid for transfer", sourceOnly.isValidForTransfer)

        val destinationOnly = SelectedWallets(destination = physicalWallet2)
        assertFalse("Destination only should be invalid for transfer", destinationOnly.isValidForTransfer)

        val sameWallet = SelectedWallets(source = physicalWallet, destination = physicalWallet)
        assertFalse("Same wallet as source and destination should be invalid", sameWallet.isValidForTransfer)

        val validTransfer = SelectedWallets(source = physicalWallet, destination = physicalWallet2)
        assertTrue("Different wallets should be valid for transfer", validTransfer.isValidForTransfer)
    }

    @Test
    fun `should calculate total balance correctly`() {
        val selection = SelectedWallets(
            physical = physicalWallet,    // 1000.0
            logical = logicalWallet       // 500.0
        )

        assertEquals("Total balance should be sum of selected wallets",
                    1500.0, selection.totalBalance, 0.01)
    }

    @Test
    fun `should update wallet selections immutably`() {
        val original = SelectedWallets()
        val updated = original.updatePhysical(physicalWallet)

        assertNull("Original should remain unchanged", original.physical)
        assertEquals("Updated should have physical wallet", physicalWallet, updated.physical)
    }

    @Test
    fun `should validate transfer compatibility`() {
        // Valid transfer: same type, different wallets
        val validTransfer = SelectedWallets(source = physicalWallet, destination = physicalWallet2)
        assertNull("Valid transfer should have no errors", validTransfer.validateTransferCompatibility())

        // Invalid: missing source
        val missingSource = SelectedWallets(destination = physicalWallet2)
        assertNotNull("Missing source should have error", missingSource.validateTransferCompatibility())

        // Invalid: same wallet
        val sameWallet = SelectedWallets(source = physicalWallet, destination = physicalWallet)
        assertNotNull("Same wallet should have error", sameWallet.validateTransferCompatibility())

        // Invalid: different types
        val differentTypes = SelectedWallets(source = physicalWallet, destination = logicalWallet)
        assertNotNull("Different types should have error", differentTypes.validateTransferCompatibility())
    }

    @Test
    fun `should track wallet roles correctly`() {
        val selection = SelectedWallets(
            physical = physicalWallet,
            source = physicalWallet
        )

        val roles = selection.getWalletRoles(physicalWallet)
        assertTrue("Should have physical role", roles.contains(WalletRole.PHYSICAL))
        assertTrue("Should have source role", roles.contains(WalletRole.SOURCE))
        assertEquals("Should have 2 roles", 2, roles.size)
    }

    @Test
    fun `should handle pre-selection correctly`() {
        val empty = SelectedWallets()

        val withPhysical = empty.withPreSelected(physicalWallet)
        assertEquals("Physical wallet should be pre-selected", physicalWallet, withPhysical.physical)

        val withLogical = empty.withPreSelected(logicalWallet)
        assertEquals("Logical wallet should be pre-selected", logicalWallet, withLogical.logical)
    }
}
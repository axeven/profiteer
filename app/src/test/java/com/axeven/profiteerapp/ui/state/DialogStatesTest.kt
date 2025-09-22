package com.axeven.profiteerapp.ui.state

import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive tests for DialogStates data class.
 * Validates dialog state management and business rules.
 */
class DialogStatesTest {

    @Test
    fun `should start with all dialogs closed`() {
        val initialState = DialogStates()

        assertFalse("Should have no open dialogs", initialState.hasOpenDialog)
        assertNull("Should have no open dialog type", initialState.openDialogType)
        assertTrue("Should have empty open dialogs list", initialState.openDialogs.isEmpty())
    }

    @Test
    fun `should open single dialog and close others`() {
        val initial = DialogStates(showPhysicalWalletPicker = true)
        val updated = initial.openDatePicker()

        assertTrue("Date picker should be open", updated.showDatePicker)
        assertFalse("Physical wallet picker should be closed", updated.showPhysicalWalletPicker)
        assertEquals("Should have date picker as open dialog", DialogType.DATE_PICKER, updated.openDialogType)
    }

    @Test
    fun `should validate single dialog business rule`() {
        val validState = DialogStates(showDatePicker = true)
        assertNull("Single dialog should be valid", validState.validateSingleDialog())

        // This should not happen in normal usage, but test for robustness
        val invalidState = DialogStates(showDatePicker = true, showPhysicalWalletPicker = true)
        assertNotNull("Multiple dialogs should be invalid", invalidState.validateSingleDialog())
    }

    @Test
    fun `should toggle dialog states correctly`() {
        val initial = DialogStates()

        // Toggle open
        val opened = initial.toggle(DialogType.DATE_PICKER)
        assertTrue("Should open when toggled from closed", opened.showDatePicker)

        // Toggle closed
        val closed = opened.toggle(DialogType.DATE_PICKER)
        assertFalse("Should close when toggled from open", closed.showDatePicker)
    }

    @Test
    fun `should close specific dialog types`() {
        val initial = DialogStates(showDatePicker = true, showPhysicalWalletPicker = true)
        val updated = initial.close(DialogType.DATE_PICKER)

        assertFalse("Date picker should be closed", updated.showDatePicker)
        assertTrue("Physical wallet picker should remain open", updated.showPhysicalWalletPicker)
    }

    @Test
    fun `should close all dialogs`() {
        val initial = DialogStates(
            showDatePicker = true,
            showPhysicalWalletPicker = true,
            showLogicalWalletPicker = true
        )

        val closed = initial.closeAll()

        assertFalse("Date picker should be closed", closed.showDatePicker)
        assertFalse("Physical wallet picker should be closed", closed.showPhysicalWalletPicker)
        assertFalse("Logical wallet picker should be closed", closed.showLogicalWalletPicker)
        assertFalse("Should have no open dialogs", closed.hasOpenDialog)
    }

    @Test
    fun `should check if specific dialog is open`() {
        val state = DialogStates(showDatePicker = true)

        assertTrue("Date picker should be reported as open", state.isOpen(DialogType.DATE_PICKER))
        assertFalse("Physical wallet picker should be reported as closed", state.isOpen(DialogType.PHYSICAL_WALLET))
    }

    @Test
    fun `should filter to wallet dialogs only`() {
        val initial = DialogStates(
            showDatePicker = true,
            showPhysicalWalletPicker = true
        )

        val filtered = initial.walletDialogsOnly()

        assertFalse("Date picker should be closed", filtered.showDatePicker)
        assertTrue("Physical wallet picker should remain open", filtered.showPhysicalWalletPicker)
    }

    @Test
    fun `should filter to transfer dialogs only`() {
        val initial = DialogStates(
            showPhysicalWalletPicker = true,
            showSourceWalletPicker = true
        )

        val filtered = initial.transferDialogsOnly()

        assertFalse("Physical wallet picker should be closed", filtered.showPhysicalWalletPicker)
        assertTrue("Source wallet picker should remain open", filtered.showSourceWalletPicker)
    }

    @Test
    fun `should filter to regular dialogs only`() {
        val initial = DialogStates(
            showPhysicalWalletPicker = true,
            showSourceWalletPicker = true
        )

        val filtered = initial.regularDialogsOnly()

        assertTrue("Physical wallet picker should remain open", filtered.showPhysicalWalletPicker)
        assertFalse("Source wallet picker should be closed", filtered.showSourceWalletPicker)
    }

    @Test
    fun `should handle dialog type display names`() {
        assertEquals("Date Picker", DialogType.DATE_PICKER.displayName())
        assertEquals("Physical Wallet Picker", DialogType.PHYSICAL_WALLET.displayName())
        assertEquals("Logical Wallet Picker", DialogType.LOGICAL_WALLET.displayName())
        assertEquals("Source Wallet Picker", DialogType.SOURCE_WALLET.displayName())
        assertEquals("Destination Wallet Picker", DialogType.DESTINATION_WALLET.displayName())
    }

    @Test
    fun `should create dialog state with only specified dialog open`() {
        val datePickerOnly = DialogType.DATE_PICKER.openOnly()
        assertTrue("Date picker should be open", datePickerOnly.showDatePicker)
        assertFalse("Other dialogs should be closed", datePickerOnly.showPhysicalWalletPicker)

        val walletPickerOnly = DialogType.PHYSICAL_WALLET.openOnly()
        assertTrue("Physical wallet picker should be open", walletPickerOnly.showPhysicalWalletPicker)
        assertFalse("Other dialogs should be closed", walletPickerOnly.showDatePicker)
    }
}
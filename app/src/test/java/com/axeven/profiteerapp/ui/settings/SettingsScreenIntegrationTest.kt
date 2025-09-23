package com.axeven.profiteerapp.ui.settings

import com.axeven.profiteerapp.data.model.CurrencyRate
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration tests for SettingsScreen migration to consolidated state.
 *
 * These tests verify that the screen properly uses SettingsUiState
 * instead of scattered mutableStateOf variables.
 */
class SettingsScreenIntegrationTest {

    // Mock data for testing
    private val mockCurrencyRate = CurrencyRate(
        id = "rate-1",
        fromCurrency = "USD",
        toCurrency = "EUR",
        rate = 0.85,
        month = "January 2024"
    )

    private val mockGoldRate = CurrencyRate(
        id = "rate-2",
        fromCurrency = "GOLD",
        toCurrency = "USD",
        rate = 65.50,
        month = null
    )

    @Test
    fun `screen should use single SettingsUiState instead of multiple mutableStateOf`() {
        // Verify that SettingsUiState can replace the 4 scattered mutableStateOf variables:
        // - showCurrencyDialog
        // - showRateDialog
        // - showEditRateDialog
        // - rateToEdit

        val settingsState = SettingsUiState()

        // Initial state should have all dialogs closed
        assertFalse(settingsState.dialogStates.showCurrencyDialog)
        assertFalse(settingsState.dialogStates.showRateDialog)
        assertFalse(settingsState.dialogStates.showEditRateDialog)
        assertNull(settingsState.selectedRateForEdit)
        assertFalse(settingsState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should handle currency dialog state management`() {
        var settingsState = SettingsUiState()

        // Open currency dialog (replaces showCurrencyDialog = true)
        settingsState = settingsState.openDialog(SettingsDialogType.CURRENCY)
        assertTrue(settingsState.dialogStates.showCurrencyDialog)
        assertFalse(settingsState.dialogStates.showRateDialog)
        assertFalse(settingsState.dialogStates.showEditRateDialog)

        // Update currency selection
        settingsState = settingsState.updateSelectedCurrency("EUR")
        assertEquals("EUR", settingsState.formData.selectedCurrency)

        // Close dialog (replaces showCurrencyDialog = false)
        settingsState = settingsState.closeAllDialogs()
        assertFalse(settingsState.dialogStates.showCurrencyDialog)

        // Currency selection should be preserved
        assertEquals("EUR", settingsState.formData.selectedCurrency)
    }

    @Test
    fun `should handle rate dialog state management`() {
        var settingsState = SettingsUiState()

        // Open rate dialog (replaces showRateDialog = true)
        settingsState = settingsState.openDialog(SettingsDialogType.RATE)
        assertFalse(settingsState.dialogStates.showCurrencyDialog)
        assertTrue(settingsState.dialogStates.showRateDialog)
        assertFalse(settingsState.dialogStates.showEditRateDialog)

        // Update rate form data
        settingsState = settingsState
            .updateRateFromCurrency("USD")
            .updateRateToCurrency("EUR")
            .updateRateValue("0.85")
            .updateRateYear("2024")
            .updateRateMonth("January")

        // Verify form data
        assertEquals("USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("EUR", settingsState.formData.rateFormData.toCurrency)
        assertEquals("0.85", settingsState.formData.rateFormData.rateText)
        assertEquals("2024", settingsState.formData.rateFormData.selectedYear)
        assertEquals("January", settingsState.formData.rateFormData.selectedMonth)
        assertTrue(settingsState.isRateFormValid)

        // Close dialog (replaces showRateDialog = false)
        settingsState = settingsState.closeAllDialogs()
        assertFalse(settingsState.dialogStates.showRateDialog)
    }

    @Test
    fun `should handle edit rate dialog state management`() {
        var settingsState = SettingsUiState()

        // Open edit dialog with rate (replaces rateToEdit = rate, showEditRateDialog = true)
        settingsState = settingsState.openEditRateDialog(mockCurrencyRate)
        assertFalse(settingsState.dialogStates.showCurrencyDialog)
        assertFalse(settingsState.dialogStates.showRateDialog)
        assertTrue(settingsState.dialogStates.showEditRateDialog)
        assertEquals(mockCurrencyRate, settingsState.selectedRateForEdit)

        // Initialize form from rate
        settingsState = settingsState.initializeEditForm(mockCurrencyRate)
        assertEquals("USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("EUR", settingsState.formData.rateFormData.toCurrency)
        assertEquals("0.85", settingsState.formData.rateFormData.rateText)
        assertEquals("2024", settingsState.formData.rateFormData.selectedYear)
        assertEquals("January", settingsState.formData.rateFormData.selectedMonth)

        // Update rate value
        settingsState = settingsState.updateRateValue("0.87")
        assertEquals("0.87", settingsState.formData.rateFormData.rateText)

        // Close dialog (replaces showEditRateDialog = false, rateToEdit = null)
        settingsState = settingsState.closeAllDialogs()
        assertFalse(settingsState.dialogStates.showEditRateDialog)
        assertNull(settingsState.selectedRateForEdit)
    }

    @Test
    fun `should manage dropdown states within dialogs`() {
        var settingsState = SettingsUiState()
            .openDialog(SettingsDialogType.RATE)

        // All dropdowns should be closed initially
        assertFalse(settingsState.formData.rateFormData.dropdownStates.showFromDropdown)
        assertFalse(settingsState.formData.rateFormData.dropdownStates.showToDropdown)
        assertFalse(settingsState.formData.rateFormData.dropdownStates.showYearDropdown)
        assertFalse(settingsState.formData.rateFormData.dropdownStates.showMonthDropdown)

        // Open from dropdown
        settingsState = settingsState.updateRateFormDropdown(RateDropdownType.FROM, true)
        assertTrue(settingsState.formData.rateFormData.dropdownStates.showFromDropdown)

        // Select currency and close dropdown
        settingsState = settingsState
            .updateRateFromCurrency("EUR")
            .updateRateFormDropdown(RateDropdownType.FROM, false)

        assertEquals("EUR", settingsState.formData.rateFormData.fromCurrency)
        assertFalse(settingsState.formData.rateFormData.dropdownStates.showFromDropdown)

        // Test other dropdowns
        settingsState = settingsState
            .updateRateFormDropdown(RateDropdownType.TO, true)
            .updateRateToCurrency("GBP")
            .updateRateFormDropdown(RateDropdownType.TO, false)

        assertEquals("GBP", settingsState.formData.rateFormData.toCurrency)
        assertFalse(settingsState.formData.rateFormData.dropdownStates.showToDropdown)

        // Close all dropdowns
        settingsState = settingsState.closeAllRateDropdowns()
        val dropdowns = settingsState.formData.rateFormData.dropdownStates
        assertFalse(dropdowns.showFromDropdown)
        assertFalse(dropdowns.showToDropdown)
        assertFalse(dropdowns.showYearDropdown)
        assertFalse(dropdowns.showMonthDropdown)
    }

    @Test
    fun `should enforce single dialog open business rule`() {
        var settingsState = SettingsUiState()

        // Open currency dialog
        settingsState = settingsState.openDialog(SettingsDialogType.CURRENCY)
        assertTrue(settingsState.dialogStates.showCurrencyDialog)
        assertFalse(settingsState.dialogStates.showRateDialog)

        // Opening rate dialog should close currency dialog
        settingsState = settingsState.openDialog(SettingsDialogType.RATE)
        assertFalse(settingsState.dialogStates.showCurrencyDialog)
        assertTrue(settingsState.dialogStates.showRateDialog)

        // Opening edit dialog should close rate dialog
        settingsState = settingsState.openEditRateDialog(mockCurrencyRate)
        assertFalse(settingsState.dialogStates.showCurrencyDialog)
        assertFalse(settingsState.dialogStates.showRateDialog)
        assertTrue(settingsState.dialogStates.showEditRateDialog)

        // Only one dialog should be open at a time
        assertTrue(settingsState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should handle gold rate creation workflow`() {
        var settingsState = SettingsUiState()
            .openDialog(SettingsDialogType.RATE)

        // Set up gold rate
        settingsState = settingsState
            .updateRateFromCurrency("GOLD")
            .updateRateToCurrency("USD")
            .updateRateValue("65.50")
            .updateRateYear("Default")
            .updateRateMonth("All Months")

        assertEquals("GOLD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("USD", settingsState.formData.rateFormData.toCurrency)
        assertEquals("65.50", settingsState.formData.rateFormData.rateText)
        assertTrue(settingsState.isRateFormValid)

        // Form should be ready for submission
        val formData = settingsState.formData.rateFormData
        assertNotNull(formData.rateText.toDoubleOrNull())
        assertTrue(formData.fromCurrency.isNotBlank())
        assertTrue(formData.toCurrency.isNotBlank())
    }

    @Test
    fun `should handle bitcoin rate creation workflow`() {
        var settingsState = SettingsUiState()
            .openDialog(SettingsDialogType.RATE)

        // Set up bitcoin rate
        settingsState = settingsState
            .updateRateFromCurrency("BTC")
            .updateRateToCurrency("USD")
            .updateRateValue("45000.00")
            .updateRateYear("2024")
            .updateRateMonth("February")

        assertEquals("BTC", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("USD", settingsState.formData.rateFormData.toCurrency)
        assertEquals("45000.00", settingsState.formData.rateFormData.rateText)
        assertEquals("2024", settingsState.formData.rateFormData.selectedYear)
        assertEquals("February", settingsState.formData.rateFormData.selectedMonth)
        assertTrue(settingsState.isRateFormValid)
    }

    @Test
    fun `should handle monthly rate creation workflow`() {
        var settingsState = SettingsUiState()
            .openDialog(SettingsDialogType.RATE)

        // Set up monthly rate
        settingsState = settingsState
            .updateRateFromCurrency("EUR")
            .updateRateToCurrency("GBP")
            .updateRateValue("0.87")
            .updateRateYear("2024")
            .updateRateMonth("March")

        val formData = settingsState.formData.rateFormData
        assertEquals("EUR", formData.fromCurrency)
        assertEquals("GBP", formData.toCurrency)
        assertEquals("0.87", formData.rateText)
        assertEquals("2024", formData.selectedYear)
        assertEquals("March", formData.selectedMonth)
        assertTrue(settingsState.isRateFormValid)

        // Verify month value formatting for submission
        // Should create "March 2024" when not default
        assertTrue(formData.selectedYear != "Default")
        assertTrue(formData.selectedMonth != "All Months")
    }

    @Test
    fun `should handle default rate creation workflow`() {
        var settingsState = SettingsUiState()
            .openDialog(SettingsDialogType.RATE)

        // Set up default rate (no specific month/year)
        settingsState = settingsState
            .updateRateFromCurrency("USD")
            .updateRateToCurrency("JPY")
            .updateRateValue("110.50")
            .updateRateYear("Default")
            .updateRateMonth("All Months")

        val formData = settingsState.formData.rateFormData
        assertEquals("USD", formData.fromCurrency)
        assertEquals("JPY", formData.toCurrency)
        assertEquals("110.50", formData.rateText)
        assertEquals("Default", formData.selectedYear)
        assertEquals("All Months", formData.selectedMonth)
        assertTrue(settingsState.isRateFormValid)
    }

    @Test
    fun `should handle rate editing with different month formats`() {
        // Test editing rate with null month
        var settingsState = SettingsUiState()
            .openEditRateDialog(mockGoldRate)
            .initializeEditForm(mockGoldRate)

        assertEquals("GOLD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("USD", settingsState.formData.rateFormData.toCurrency)
        assertEquals("65.5", settingsState.formData.rateFormData.rateText)
        assertEquals("Default", settingsState.formData.rateFormData.selectedYear)
        assertEquals("All Months", settingsState.formData.rateFormData.selectedMonth)

        // Test editing rate with specific month
        settingsState = SettingsUiState()
            .openEditRateDialog(mockCurrencyRate)
            .initializeEditForm(mockCurrencyRate)

        assertEquals("USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("EUR", settingsState.formData.rateFormData.toCurrency)
        assertEquals("0.85", settingsState.formData.rateFormData.rateText)
        assertEquals("2024", settingsState.formData.rateFormData.selectedYear)
        assertEquals("January", settingsState.formData.rateFormData.selectedMonth)
    }

    @Test
    fun `should validate form data correctly`() {
        var settingsState = SettingsUiState()

        // Invalid: empty rate text
        settingsState = settingsState
            .updateRateFromCurrency("USD")
            .updateRateToCurrency("EUR")
            .updateRateValue("")
        assertFalse(settingsState.isRateFormValid)

        // Invalid: non-numeric rate text
        settingsState = settingsState.updateRateValue("invalid")
        assertFalse(settingsState.isRateFormValid)

        // Invalid: empty currencies
        settingsState = settingsState
            .updateRateFromCurrency("")
            .updateRateValue("0.85")
        assertFalse(settingsState.isRateFormValid)

        // Valid: all fields filled correctly
        settingsState = settingsState
            .updateRateFromCurrency("USD")
            .updateRateToCurrency("EUR")
            .updateRateValue("0.85")
        assertTrue(settingsState.isRateFormValid)
    }

    @Test
    fun `should reset form data after operations`() {
        var settingsState = SettingsUiState()
            .openDialog(SettingsDialogType.RATE)
            .updateRateFromCurrency("EUR")
            .updateRateToCurrency("GBP")
            .updateRateValue("0.87")
            .updateRateYear("2024")
            .updateRateMonth("April")

        // Verify form has data
        val beforeReset = settingsState.formData.rateFormData
        assertEquals("EUR", beforeReset.fromCurrency)
        assertEquals("GBP", beforeReset.toCurrency)
        assertEquals("0.87", beforeReset.rateText)
        assertEquals("2024", beforeReset.selectedYear)
        assertEquals("April", beforeReset.selectedMonth)

        // Reset form
        settingsState = settingsState.resetRateForm()

        // Verify form is reset to defaults
        val afterReset = settingsState.formData.rateFormData
        assertEquals("USD", afterReset.fromCurrency)
        assertEquals("EUR", afterReset.toCurrency)
        assertEquals("", afterReset.rateText)
        assertEquals("Default", afterReset.selectedYear)
        assertEquals("All Months", afterReset.selectedMonth)
    }

    @Test
    fun `should preserve form data across dialog state changes`() {
        var settingsState = SettingsUiState()
            .updateSelectedCurrency("EUR")
            .updateRateFromCurrency("GBP")
            .updateRateToCurrency("USD")

        // Open and close dialogs
        settingsState = settingsState
            .openDialog(SettingsDialogType.CURRENCY)
            .closeAllDialogs()
            .openDialog(SettingsDialogType.RATE)
            .closeAllDialogs()

        // Form data should be preserved
        assertEquals("EUR", settingsState.formData.selectedCurrency)
        assertEquals("GBP", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("USD", settingsState.formData.rateFormData.toCurrency)
    }

    @Test
    fun `should handle complete settings configuration workflow`() {
        var settingsState = SettingsUiState()

        // 1. Configure default currency
        settingsState = settingsState
            .openDialog(SettingsDialogType.CURRENCY)
            .updateSelectedCurrency("EUR")
            .closeAllDialogs()

        assertEquals("EUR", settingsState.formData.selectedCurrency)
        assertFalse(settingsState.dialogStates.hasAnyDialogOpen)

        // 2. Create default conversion rate
        settingsState = settingsState
            .openDialog(SettingsDialogType.RATE)
            .updateRateFromCurrency("EUR")
            .updateRateToCurrency("USD")
            .updateRateValue("1.08")
            .updateRateYear("Default")
            .updateRateMonth("All Months")

        assertTrue(settingsState.isRateFormValid)

        // After submission, reset form and close dialog
        settingsState = settingsState.resetRateForm().closeAllDialogs()
        assertEquals(RateFormData(), settingsState.formData.rateFormData)
        assertFalse(settingsState.dialogStates.hasAnyDialogOpen)

        // 3. Create monthly rate
        settingsState = settingsState
            .openDialog(SettingsDialogType.RATE)
            .updateRateFromCurrency("EUR")
            .updateRateToCurrency("GBP")
            .updateRateValue("0.87")
            .updateRateYear("2024")
            .updateRateMonth("May")

        assertTrue(settingsState.isRateFormValid)
        assertEquals("EUR", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("GBP", settingsState.formData.rateFormData.toCurrency)

        // Currency selection should still be preserved
        assertEquals("EUR", settingsState.formData.selectedCurrency)
    }

    @Test
    fun `should handle error cases gracefully`() {
        var settingsState = SettingsUiState()

        // Test with malformed month in rate
        val malformedRate = CurrencyRate(
            id = "rate-3",
            fromCurrency = "USD",
            toCurrency = "EUR",
            rate = 0.85,
            month = "InvalidFormat"
        )

        settingsState = settingsState.initializeEditForm(malformedRate)

        // Should fallback to defaults for malformed month
        assertEquals("Default", settingsState.formData.rateFormData.selectedYear)
        assertEquals("All Months", settingsState.formData.rateFormData.selectedMonth)

        // Other fields should still be initialized correctly
        assertEquals("USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("EUR", settingsState.formData.rateFormData.toCurrency)
        assertEquals("0.85", settingsState.formData.rateFormData.rateText)
    }
}
package com.axeven.profiteerapp.ui.state

import com.axeven.profiteerapp.data.model.CurrencyRate
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Test-Driven Development tests for SettingsUiState.
 *
 * Following TDD methodology - these tests define the expected behavior for the
 * settings state management. They will initially fail (RED phase)
 * and will be made to pass during implementation (GREEN phase).
 */
class SettingsUiStateTest {

    // Mock data for testing
    private val mockCurrencyRate = CurrencyRate(
        id = "rate-1",
        fromCurrency = "USD",
        toCurrency = "EUR",
        rate = 0.85,
        month = "January 2024"
    )

    // Tests for SettingsUiState construction and initialization
    @Test
    fun `should create default SettingsUiState`() {
        val settingsState = SettingsUiState()

        assertEquals(SettingsDialogStates(), settingsState.dialogStates)
        assertNull(settingsState.selectedRateForEdit)
        assertEquals(SettingsFormData(), settingsState.formData)
        assertFalse(settingsState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should create SettingsUiState with initial data`() {
        val initialFormData = SettingsFormData(
            selectedCurrency = "EUR",
            rateFormData = RateFormData(
                fromCurrency = "USD",
                toCurrency = "EUR"
            )
        )

        val settingsState = SettingsUiState(
            formData = initialFormData
        )

        assertEquals("EUR", settingsState.formData.selectedCurrency)
        assertEquals("USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("EUR", settingsState.formData.rateFormData.toCurrency)
    }

    // Tests for dialog state management
    @Test
    fun `should open currency dialog and close others`() {
        val settingsState = SettingsUiState()

        val openedState = settingsState.openDialog(SettingsDialogType.CURRENCY)

        assertTrue(openedState.dialogStates.showCurrencyDialog)
        assertFalse(openedState.dialogStates.showRateDialog)
        assertFalse(openedState.dialogStates.showEditRateDialog)
        assertTrue(openedState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should open rate dialog and close others`() {
        val settingsState = SettingsUiState(
            dialogStates = SettingsDialogStates(showCurrencyDialog = true)
        )

        val openedState = settingsState.openDialog(SettingsDialogType.RATE)

        assertFalse(openedState.dialogStates.showCurrencyDialog)
        assertTrue(openedState.dialogStates.showRateDialog)
        assertFalse(openedState.dialogStates.showEditRateDialog)
        assertTrue(openedState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should open edit rate dialog with selected rate`() {
        val settingsState = SettingsUiState()

        val openedState = settingsState.openEditRateDialog(mockCurrencyRate)

        assertFalse(openedState.dialogStates.showCurrencyDialog)
        assertFalse(openedState.dialogStates.showRateDialog)
        assertTrue(openedState.dialogStates.showEditRateDialog)
        assertEquals(mockCurrencyRate, openedState.selectedRateForEdit)
        assertTrue(openedState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should close all dialogs`() {
        val settingsState = SettingsUiState(
            dialogStates = SettingsDialogStates(
                showCurrencyDialog = true,
                showRateDialog = true,
                showEditRateDialog = true
            ),
            selectedRateForEdit = mockCurrencyRate
        )

        val closedState = settingsState.closeAllDialogs()

        assertFalse(closedState.dialogStates.showCurrencyDialog)
        assertFalse(closedState.dialogStates.showRateDialog)
        assertFalse(closedState.dialogStates.showEditRateDialog)
        assertNull(closedState.selectedRateForEdit)
        assertFalse(closedState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should enforce single dialog open business rule`() {
        val settingsState = SettingsUiState()
            .openDialog(SettingsDialogType.CURRENCY)
            .openDialog(SettingsDialogType.RATE)

        // Only the last opened dialog should be open
        assertFalse(settingsState.dialogStates.showCurrencyDialog)
        assertTrue(settingsState.dialogStates.showRateDialog)
        assertFalse(settingsState.dialogStates.showEditRateDialog)
    }

    // Tests for form data management
    @Test
    fun `should update currency selection`() {
        val settingsState = SettingsUiState()

        val updatedState = settingsState.updateSelectedCurrency("EUR")

        assertEquals("EUR", updatedState.formData.selectedCurrency)
        assertEquals("USD", settingsState.formData.selectedCurrency) // Original unchanged
    }

    @Test
    fun `should update rate form from currency`() {
        val settingsState = SettingsUiState()

        val updatedState = settingsState.updateRateFromCurrency("EUR")

        assertEquals("EUR", updatedState.formData.rateFormData.fromCurrency)
    }

    @Test
    fun `should update rate form to currency`() {
        val settingsState = SettingsUiState()

        val updatedState = settingsState.updateRateToCurrency("GBP")

        assertEquals("GBP", updatedState.formData.rateFormData.toCurrency)
    }

    @Test
    fun `should update rate form rate value`() {
        val settingsState = SettingsUiState()

        val updatedState = settingsState.updateRateValue("1.25")

        assertEquals("1.25", updatedState.formData.rateFormData.rateText)
    }

    @Test
    fun `should update rate form year`() {
        val settingsState = SettingsUiState()

        val updatedState = settingsState.updateRateYear("2024")

        assertEquals("2024", updatedState.formData.rateFormData.selectedYear)
    }

    @Test
    fun `should update rate form month`() {
        val settingsState = SettingsUiState()

        val updatedState = settingsState.updateRateMonth("January")

        assertEquals("January", updatedState.formData.rateFormData.selectedMonth)
    }

    @Test
    fun `should reset rate form data`() {
        val settingsState = SettingsUiState(
            formData = SettingsFormData(
                rateFormData = RateFormData(
                    fromCurrency = "EUR",
                    toCurrency = "GBP",
                    rateText = "1.15",
                    selectedYear = "2024",
                    selectedMonth = "January"
                )
            )
        )

        val resetState = settingsState.resetRateForm()

        assertEquals(RateFormData(), resetState.formData.rateFormData)
    }

    // Tests for edit rate initialization
    @Test
    fun `should initialize form from currency rate for editing`() {
        val rateToEdit = CurrencyRate(
            id = "rate-1",
            fromCurrency = "EUR",
            toCurrency = "GBP",
            rate = 0.87,
            month = "February 2024"
        )

        val settingsState = SettingsUiState().initializeEditForm(rateToEdit)

        assertEquals("EUR", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("GBP", settingsState.formData.rateFormData.toCurrency)
        assertEquals("0.87", settingsState.formData.rateFormData.rateText)
        assertEquals("2024", settingsState.formData.rateFormData.selectedYear)
        assertEquals("February", settingsState.formData.rateFormData.selectedMonth)
    }

    @Test
    fun `should initialize form with default values for null month`() {
        val rateToEdit = CurrencyRate(
            id = "rate-1",
            fromCurrency = "USD",
            toCurrency = "EUR",
            rate = 0.85,
            month = null
        )

        val settingsState = SettingsUiState().initializeEditForm(rateToEdit)

        assertEquals("USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("EUR", settingsState.formData.rateFormData.toCurrency)
        assertEquals("0.85", settingsState.formData.rateFormData.rateText)
        assertEquals("Default", settingsState.formData.rateFormData.selectedYear)
        assertEquals("All Months", settingsState.formData.rateFormData.selectedMonth)
    }

    @Test
    fun `should initialize form with default values for empty month`() {
        val rateToEdit = CurrencyRate(
            id = "rate-1",
            fromCurrency = "USD",
            toCurrency = "EUR",
            rate = 0.85,
            month = ""
        )

        val settingsState = SettingsUiState().initializeEditForm(rateToEdit)

        assertEquals("Default", settingsState.formData.rateFormData.selectedYear)
        assertEquals("All Months", settingsState.formData.rateFormData.selectedMonth)
    }

    // Tests for dropdown state management within dialogs
    @Test
    fun `should manage dropdown states independently`() {
        val settingsState = SettingsUiState()

        val withFromDropdown = settingsState.updateRateFormDropdown(RateDropdownType.FROM, true)
        val withToDropdown = withFromDropdown.updateRateFormDropdown(RateDropdownType.TO, true)

        assertTrue(withToDropdown.formData.rateFormData.dropdownStates.showFromDropdown)
        assertTrue(withToDropdown.formData.rateFormData.dropdownStates.showToDropdown)
        assertFalse(withToDropdown.formData.rateFormData.dropdownStates.showYearDropdown)
        assertFalse(withToDropdown.formData.rateFormData.dropdownStates.showMonthDropdown)
    }

    @Test
    fun `should close specific dropdown`() {
        val settingsState = SettingsUiState(
            formData = SettingsFormData(
                rateFormData = RateFormData(
                    dropdownStates = RateDropdownStates(
                        showFromDropdown = true,
                        showToDropdown = true,
                        showYearDropdown = true,
                        showMonthDropdown = true
                    )
                )
            )
        )

        val updatedState = settingsState.updateRateFormDropdown(RateDropdownType.FROM, false)

        assertFalse(updatedState.formData.rateFormData.dropdownStates.showFromDropdown)
        assertTrue(updatedState.formData.rateFormData.dropdownStates.showToDropdown)
        assertTrue(updatedState.formData.rateFormData.dropdownStates.showYearDropdown)
        assertTrue(updatedState.formData.rateFormData.dropdownStates.showMonthDropdown)
    }

    @Test
    fun `should close all dropdowns`() {
        val settingsState = SettingsUiState(
            formData = SettingsFormData(
                rateFormData = RateFormData(
                    dropdownStates = RateDropdownStates(
                        showFromDropdown = true,
                        showToDropdown = true,
                        showYearDropdown = true,
                        showMonthDropdown = true
                    )
                )
            )
        )

        val closedState = settingsState.closeAllRateDropdowns()

        assertFalse(closedState.formData.rateFormData.dropdownStates.showFromDropdown)
        assertFalse(closedState.formData.rateFormData.dropdownStates.showToDropdown)
        assertFalse(closedState.formData.rateFormData.dropdownStates.showYearDropdown)
        assertFalse(closedState.formData.rateFormData.dropdownStates.showMonthDropdown)
    }

    // Tests for validation
    @Test
    fun `should validate rate form data`() {
        val validFormData = RateFormData(
            fromCurrency = "USD",
            toCurrency = "EUR",
            rateText = "0.85"
        )

        val settingsState = SettingsUiState(
            formData = SettingsFormData(rateFormData = validFormData)
        )

        assertTrue(settingsState.isRateFormValid)
    }

    @Test
    fun `should reject invalid rate form data`() {
        val invalidFormData = RateFormData(
            fromCurrency = "",
            toCurrency = "EUR",
            rateText = "invalid"
        )

        val settingsState = SettingsUiState(
            formData = SettingsFormData(rateFormData = invalidFormData)
        )

        assertFalse(settingsState.isRateFormValid)
    }

    @Test
    fun `should reject empty rate text`() {
        val invalidFormData = RateFormData(
            fromCurrency = "USD",
            toCurrency = "EUR",
            rateText = ""
        )

        val settingsState = SettingsUiState(
            formData = SettingsFormData(rateFormData = invalidFormData)
        )

        assertFalse(settingsState.isRateFormValid)
    }

    // Tests for immutability and state copying
    @Test
    fun `should maintain immutability during updates`() {
        val originalState = SettingsUiState()
        val updatedState = originalState.updateSelectedCurrency("EUR")

        // Original state should be unchanged
        assertEquals("USD", originalState.formData.selectedCurrency)

        // Updated state should have changes
        assertEquals("EUR", updatedState.formData.selectedCurrency)

        // Objects should be different instances
        assertNotSame(originalState, updatedState)
        assertNotSame(originalState.formData, updatedState.formData)
    }

    @Test
    fun `should preserve other state during dialog updates`() {
        val settingsState = SettingsUiState(
            formData = SettingsFormData(
                selectedCurrency = "EUR",
                rateFormData = RateFormData(
                    fromCurrency = "GBP",
                    toCurrency = "USD"
                )
            )
        )

        val dialogOpenedState = settingsState.openDialog(SettingsDialogType.CURRENCY)

        // Form data should be preserved
        assertEquals("EUR", dialogOpenedState.formData.selectedCurrency)
        assertEquals("GBP", dialogOpenedState.formData.rateFormData.fromCurrency)
        assertEquals("USD", dialogOpenedState.formData.rateFormData.toCurrency)

        // Dialog state should be updated
        assertTrue(dialogOpenedState.dialogStates.showCurrencyDialog)
    }

    // Tests for complex state scenarios
    @Test
    fun `should handle complete currency selection flow`() {
        var settingsState = SettingsUiState()

        // Open currency dialog
        settingsState = settingsState.openDialog(SettingsDialogType.CURRENCY)
        assertTrue(settingsState.dialogStates.showCurrencyDialog)

        // Update currency selection
        settingsState = settingsState.updateSelectedCurrency("EUR")
        assertEquals("EUR", settingsState.formData.selectedCurrency)

        // Close dialog
        settingsState = settingsState.closeAllDialogs()
        assertFalse(settingsState.dialogStates.showCurrencyDialog)

        // Currency should be preserved
        assertEquals("EUR", settingsState.formData.selectedCurrency)
    }

    @Test
    fun `should handle complete rate creation flow`() {
        var settingsState = SettingsUiState()

        // Open rate dialog
        settingsState = settingsState.openDialog(SettingsDialogType.RATE)
        assertTrue(settingsState.dialogStates.showRateDialog)

        // Update form data
        settingsState = settingsState
            .updateRateFromCurrency("USD")
            .updateRateToCurrency("EUR")
            .updateRateValue("0.85")
            .updateRateYear("2024")
            .updateRateMonth("January")

        assertEquals("USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("EUR", settingsState.formData.rateFormData.toCurrency)
        assertEquals("0.85", settingsState.formData.rateFormData.rateText)
        assertEquals("2024", settingsState.formData.rateFormData.selectedYear)
        assertEquals("January", settingsState.formData.rateFormData.selectedMonth)
        assertTrue(settingsState.isRateFormValid)

        // Reset form after submission
        settingsState = settingsState.resetRateForm().closeAllDialogs()
        assertEquals(RateFormData(), settingsState.formData.rateFormData)
        assertFalse(settingsState.dialogStates.hasAnyDialogOpen)
    }

    @Test
    fun `should handle complete rate editing flow`() {
        var settingsState = SettingsUiState()

        // Open edit dialog with existing rate
        settingsState = settingsState.openEditRateDialog(mockCurrencyRate)
        assertTrue(settingsState.dialogStates.showEditRateDialog)
        assertEquals(mockCurrencyRate, settingsState.selectedRateForEdit)

        // Initialize form from rate
        settingsState = settingsState.initializeEditForm(mockCurrencyRate)
        assertEquals("USD", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("EUR", settingsState.formData.rateFormData.toCurrency)
        assertEquals("0.85", settingsState.formData.rateFormData.rateText)

        // Update rate value
        settingsState = settingsState.updateRateValue("0.87")
        assertEquals("0.87", settingsState.formData.rateFormData.rateText)
        assertTrue(settingsState.isRateFormValid)

        // Close dialog after saving
        settingsState = settingsState.closeAllDialogs()
        assertFalse(settingsState.dialogStates.showEditRateDialog)
        assertNull(settingsState.selectedRateForEdit)
    }

    // Performance tests
    @Test
    fun `should maintain performance with state updates`() {
        var settingsState = SettingsUiState()

        val startTime = System.currentTimeMillis()

        for (i in 1..100) {
            settingsState = settingsState
                .updateSelectedCurrency("Currency$i")
                .updateRateFromCurrency("From$i")
                .updateRateToCurrency("To$i")
                .updateRateValue("$i.0")
                .openDialog(SettingsDialogType.RATE)
                .closeAllDialogs()
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete 600 state updates in under 50ms
        assertTrue("Settings state updates should be fast, took ${duration}ms", duration < 50)

        // Verify final state
        assertEquals("Currency100", settingsState.formData.selectedCurrency)
        assertEquals("From100", settingsState.formData.rateFormData.fromCurrency)
        assertEquals("To100", settingsState.formData.rateFormData.toCurrency)
        assertEquals("100.0", settingsState.formData.rateFormData.rateText)
        assertFalse(settingsState.dialogStates.hasAnyDialogOpen)
    }
}
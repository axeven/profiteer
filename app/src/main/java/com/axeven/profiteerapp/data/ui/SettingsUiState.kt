package com.axeven.profiteerapp.data.ui

import com.axeven.profiteerapp.data.model.CurrencyRate

/**
 * Consolidated state for SettingsScreen.
 *
 * This data class consolidates all UI state for the settings screen,
 * replacing scattered mutableStateOf variables with a single source of truth.
 */
data class SettingsUiState(
    val dialogStates: SettingsDialogStates = SettingsDialogStates(),
    val selectedRateForEdit: CurrencyRate? = null,
    val formData: SettingsFormData = SettingsFormData()
) {

    /**
     * Whether the rate form has valid data for submission.
     */
    val isRateFormValid: Boolean
        get() = formData.rateFormData.let { form ->
            form.fromCurrency.isNotBlank() &&
            form.toCurrency.isNotBlank() &&
            form.rateText.toDoubleOrNull() != null
        }

    /**
     * Open a specific dialog, closing others to enforce single dialog business rule.
     */
    fun openDialog(dialogType: SettingsDialogType): SettingsUiState {
        return this.copy(
            dialogStates = when (dialogType) {
                SettingsDialogType.CURRENCY -> SettingsDialogStates(showCurrencyDialog = true)
                SettingsDialogType.RATE -> SettingsDialogStates(showRateDialog = true)
            },
            selectedRateForEdit = null // Clear edit selection when opening other dialogs
        )
    }

    /**
     * Open edit rate dialog with specific rate to edit.
     */
    fun openEditRateDialog(rate: CurrencyRate): SettingsUiState {
        return this.copy(
            dialogStates = SettingsDialogStates(showEditRateDialog = true),
            selectedRateForEdit = rate
        )
    }

    /**
     * Close all dialogs and clear related state.
     */
    fun closeAllDialogs(): SettingsUiState {
        return this.copy(
            dialogStates = SettingsDialogStates(),
            selectedRateForEdit = null
        )
    }

    /**
     * Update selected currency.
     */
    fun updateSelectedCurrency(currency: String): SettingsUiState {
        return this.copy(
            formData = formData.copy(selectedCurrency = currency)
        )
    }

    /**
     * Update rate form from currency.
     */
    fun updateRateFromCurrency(currency: String): SettingsUiState {
        return this.copy(
            formData = formData.copy(
                rateFormData = formData.rateFormData.copy(fromCurrency = currency)
            )
        )
    }

    /**
     * Update rate form to currency.
     */
    fun updateRateToCurrency(currency: String): SettingsUiState {
        return this.copy(
            formData = formData.copy(
                rateFormData = formData.rateFormData.copy(toCurrency = currency)
            )
        )
    }

    /**
     * Update rate form rate value.
     */
    fun updateRateValue(rate: String): SettingsUiState {
        return this.copy(
            formData = formData.copy(
                rateFormData = formData.rateFormData.copy(rateText = rate)
            )
        )
    }

    /**
     * Update rate form year.
     */
    fun updateRateYear(year: String): SettingsUiState {
        return this.copy(
            formData = formData.copy(
                rateFormData = formData.rateFormData.copy(selectedYear = year)
            )
        )
    }

    /**
     * Update rate form month.
     */
    fun updateRateMonth(month: String): SettingsUiState {
        return this.copy(
            formData = formData.copy(
                rateFormData = formData.rateFormData.copy(selectedMonth = month)
            )
        )
    }

    /**
     * Reset rate form data to defaults.
     */
    fun resetRateForm(): SettingsUiState {
        return this.copy(
            formData = formData.copy(rateFormData = RateFormData())
        )
    }

    /**
     * Initialize rate form from existing currency rate for editing.
     */
    fun initializeEditForm(rate: CurrencyRate): SettingsUiState {
        val (year, month) = if (rate.month.isNullOrEmpty()) {
            "Default" to "All Months"
        } else {
            val parts = rate.month.split(" ")
            if (parts.size == 2) {
                parts[1] to parts[0] // "January 2024" -> "2024" to "January"
            } else {
                "Default" to "All Months"
            }
        }

        return this.copy(
            formData = formData.copy(
                rateFormData = RateFormData(
                    fromCurrency = rate.fromCurrency,
                    toCurrency = rate.toCurrency,
                    rateText = rate.rate.toString(),
                    selectedYear = year,
                    selectedMonth = month
                )
            )
        )
    }

    /**
     * Update dropdown state for rate form.
     */
    fun updateRateFormDropdown(dropdownType: RateDropdownType, isOpen: Boolean): SettingsUiState {
        val currentDropdowns = formData.rateFormData.dropdownStates
        val newDropdowns = when (dropdownType) {
            RateDropdownType.FROM -> currentDropdowns.copy(showFromDropdown = isOpen)
            RateDropdownType.TO -> currentDropdowns.copy(showToDropdown = isOpen)
            RateDropdownType.YEAR -> currentDropdowns.copy(showYearDropdown = isOpen)
            RateDropdownType.MONTH -> currentDropdowns.copy(showMonthDropdown = isOpen)
        }

        return this.copy(
            formData = formData.copy(
                rateFormData = formData.rateFormData.copy(dropdownStates = newDropdowns)
            )
        )
    }

    /**
     * Close all rate form dropdowns.
     */
    fun closeAllRateDropdowns(): SettingsUiState {
        return this.copy(
            formData = formData.copy(
                rateFormData = formData.rateFormData.copy(
                    dropdownStates = RateDropdownStates()
                )
            )
        )
    }
}

/**
 * Dialog states for settings screen.
 */
data class SettingsDialogStates(
    val showCurrencyDialog: Boolean = false,
    val showRateDialog: Boolean = false,
    val showEditRateDialog: Boolean = false
) {
    /**
     * Whether any dialog is currently open.
     */
    val hasAnyDialogOpen: Boolean
        get() = showCurrencyDialog || showRateDialog || showEditRateDialog
}

/**
 * Form data for settings screen.
 */
data class SettingsFormData(
    val selectedCurrency: String = "USD",
    val rateFormData: RateFormData = RateFormData()
)

/**
 * Form data specific to rate dialogs.
 */
data class RateFormData(
    val fromCurrency: String = "USD",
    val toCurrency: String = "EUR",
    val rateText: String = "",
    val selectedYear: String = "Default",
    val selectedMonth: String = "All Months",
    val dropdownStates: RateDropdownStates = RateDropdownStates()
)

/**
 * Dropdown states for rate form.
 */
data class RateDropdownStates(
    val showFromDropdown: Boolean = false,
    val showToDropdown: Boolean = false,
    val showYearDropdown: Boolean = false,
    val showMonthDropdown: Boolean = false
)

/**
 * Types of dialogs in settings screen.
 */
enum class SettingsDialogType {
    CURRENCY,
    RATE
}

/**
 * Types of dropdowns in rate form.
 */
enum class RateDropdownType {
    FROM,
    TO,
    YEAR,
    MONTH
}
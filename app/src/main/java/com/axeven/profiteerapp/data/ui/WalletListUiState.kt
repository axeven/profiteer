package com.axeven.profiteerapp.data.ui

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.utils.NumberFormatter

/**
 * Consolidated state for WalletListScreen.
 *
 * This data class consolidates all UI state for the wallet list screen,
 * replacing scattered mutableStateOf variables with a single source of truth.
 */
data class WalletListUiState(
    val dialogStates: WalletListDialogStates = WalletListDialogStates(),
    val selectedWalletForEdit: Wallet? = null,
    val formData: WalletFormData = WalletFormData()
) {

    /**
     * Whether the wallet form has valid data for submission.
     */
    val isFormValid: Boolean
        get() = formData.let { form ->
            form.walletName.isNotBlank() &&
            form.selectedWalletType.isNotBlank() &&
            NumberFormatter.parseDouble(form.initialBalanceText) != null &&
            NumberFormatter.parseDouble(form.initialBalanceText)!! >= 0.0
        }

    /**
     * Open a specific dialog, closing others to enforce single dialog business rule.
     */
    fun openDialog(dialogType: WalletListDialogType): WalletListUiState {
        return this.copy(
            dialogStates = when (dialogType) {
                WalletListDialogType.CREATE -> WalletListDialogStates(showCreateWalletDialog = true)
            },
            selectedWalletForEdit = null // Clear edit selection when opening other dialogs
        )
    }

    /**
     * Open edit wallet dialog with specific wallet to edit.
     */
    fun openEditWalletDialog(wallet: Wallet): WalletListUiState {
        return this.copy(
            dialogStates = WalletListDialogStates(showEditWalletDialog = true),
            selectedWalletForEdit = wallet
        )
    }

    /**
     * Close all dialogs and clear related state.
     */
    fun closeAllDialogs(): WalletListUiState {
        return this.copy(
            dialogStates = WalletListDialogStates(),
            selectedWalletForEdit = null
        )
    }

    /**
     * Update wallet name in form.
     */
    fun updateWalletName(name: String): WalletListUiState {
        return this.copy(
            formData = formData.copy(walletName = name)
        )
    }

    /**
     * Update wallet type in form.
     */
    fun updateWalletType(type: String): WalletListUiState {
        return this.copy(
            formData = formData.copy(selectedWalletType = type)
        )
    }

    /**
     * Update physical form in form.
     */
    fun updatePhysicalForm(physicalForm: PhysicalForm): WalletListUiState {
        return this.copy(
            formData = formData.copy(selectedPhysicalForm = physicalForm)
        )
    }

    /**
     * Update initial balance in form.
     */
    fun updateInitialBalance(balance: String): WalletListUiState {
        return this.copy(
            formData = formData.copy(initialBalanceText = balance)
        )
    }

    /**
     * Reset form data to defaults.
     */
    fun resetForm(): WalletListUiState {
        return this.copy(
            formData = WalletFormData()
        )
    }

    /**
     * Initialize form from existing wallet for editing.
     */
    fun initializeEditForm(wallet: Wallet): WalletListUiState {
        return this.copy(
            formData = WalletFormData(
                walletName = wallet.name,
                selectedWalletType = wallet.walletType,
                selectedPhysicalForm = wallet.physicalForm,
                initialBalanceText = wallet.initialBalance.toString()
            )
        )
    }

    /**
     * Update dropdown state for form.
     */
    fun updateDropdownState(dropdownType: WalletDropdownType, isOpen: Boolean): WalletListUiState {
        val currentDropdowns = formData.dropdownStates
        val newDropdowns = when (dropdownType) {
            WalletDropdownType.WALLET_TYPE -> currentDropdowns.copy(showWalletTypeDropdown = isOpen)
        }

        return this.copy(
            formData = formData.copy(dropdownStates = newDropdowns)
        )
    }

    /**
     * Close all form dropdowns.
     */
    fun closeAllDropdowns(): WalletListUiState {
        return this.copy(
            formData = formData.copy(
                dropdownStates = WalletDropdownStates()
            )
        )
    }

    /**
     * Get wallet summary for creation/update operations.
     */
    fun getWalletSummary(): WalletSummary {
        val initialBalance = NumberFormatter.parseDouble(formData.initialBalanceText) ?: 0.0

        return WalletSummary(
            name = formData.walletName,
            walletType = formData.selectedWalletType,
            physicalForm = formData.selectedPhysicalForm,
            initialBalance = initialBalance,
            isValid = isFormValid
        )
    }
}

/**
 * Dialog states for wallet list screen.
 */
data class WalletListDialogStates(
    val showCreateWalletDialog: Boolean = false,
    val showEditWalletDialog: Boolean = false
) {
    /**
     * Whether any dialog is currently open.
     */
    val hasAnyDialogOpen: Boolean
        get() = showCreateWalletDialog || showEditWalletDialog
}

/**
 * Form data for wallet dialogs.
 */
data class WalletFormData(
    val walletName: String = "",
    val selectedWalletType: String = "Physical",
    val selectedPhysicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY,
    val initialBalanceText: String = "0.00",
    val dropdownStates: WalletDropdownStates = WalletDropdownStates()
)

/**
 * Dropdown states for wallet form.
 */
data class WalletDropdownStates(
    val showWalletTypeDropdown: Boolean = false
)

/**
 * Summary data for wallet operations.
 */
data class WalletSummary(
    val name: String,
    val walletType: String,
    val physicalForm: PhysicalForm,
    val initialBalance: Double,
    val isValid: Boolean
)

/**
 * Types of dialogs in wallet list screen.
 */
enum class WalletListDialogType {
    CREATE
}

/**
 * Types of dropdowns in wallet form.
 */
enum class WalletDropdownType {
    WALLET_TYPE
}
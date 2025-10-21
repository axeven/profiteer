package com.axeven.profiteerapp.data.ui

import com.axeven.profiteerapp.data.constants.ValidationConstants
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.utils.TagNormalizer
import java.util.*

/**
 * Dialog types for consistent dialog management.
 */
enum class DialogType {
    DATE_PICKER,
    PHYSICAL_WALLET,
    LOGICAL_WALLET,
    SOURCE_WALLET,
    DESTINATION_WALLET
}

/**
 * Consolidated UI state for CreateTransactionScreen.
 *
 * This replaces the scattered mutableStateOf variables with a single source of truth
 * for all transaction creation state, improving testability and maintainability.
 *
 * Following TDD methodology - this implementation will make failing tests pass.
 */
data class CreateTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedWallets: SelectedWallets = SelectedWallets(),
    val selectedDate: Date = Date(),

    /**
     * Comma-separated tag string in normalized (lowercase) format.
     *
     * Tags are automatically normalized to lowercase for consistent storage and filtering.
     * The UI layer should apply TagFormatter.formatTags() for display purposes only.
     *
     * Example:
     * - Input: "Food, TRAVEL, Grocery Shopping"
     * - Stored: "food, travel, grocery shopping"
     * - Displayed: "Food, Travel, Grocery Shopping"
     *
     * @see com.axeven.profiteerapp.utils.TagNormalizer for normalization
     * @see com.axeven.profiteerapp.utils.TagFormatter for display formatting
     */
    val tags: String = "",

    val dialogStates: DialogStates = DialogStates(),
    val validationErrors: ValidationErrors = ValidationErrors(),
    val isFormValid: Boolean = false
) {

    /**
     * Convenience method to update state while maintaining immutability.
     * Automatically recalculates form validation after updates.
     */
    fun updateAndValidate(
        title: String = this.title,
        amount: String = this.amount,
        selectedType: TransactionType = this.selectedType,
        selectedWallets: SelectedWallets = this.selectedWallets,
        selectedDate: Date = this.selectedDate,
        tags: String = this.tags,
        dialogStates: DialogStates = this.dialogStates
    ): CreateTransactionUiState {
        val newState = copy(
            title = title,
            amount = amount,
            selectedType = selectedType,
            selectedWallets = selectedWallets,
            selectedDate = selectedDate,
            tags = tags,
            dialogStates = dialogStates
        )

        return newState.copy(
            validationErrors = validateState(newState),
            isFormValid = isStateValid(newState)
        )
    }

    /**
     * Updates transaction type and clears incompatible fields.
     * When switching from TRANSFER to INCOME/EXPENSE, clears transfer-specific wallets.
     */
    fun updateTransactionType(newType: TransactionType): CreateTransactionUiState {
        val updatedWallets = when {
            // Switching from TRANSFER to INCOME/EXPENSE - clear transfer wallets
            selectedType == TransactionType.TRANSFER && newType != TransactionType.TRANSFER -> {
                selectedWallets.copy(source = null, destination = null)
            }
            // Switching to TRANSFER from INCOME/EXPENSE - keep existing wallets
            else -> selectedWallets
        }

        return updateAndValidate(
            selectedType = newType,
            selectedWallets = updatedWallets
        )
    }

    /**
     * Opens a specific dialog while closing all others (business rule).
     */
    fun openDialog(dialogType: DialogType): CreateTransactionUiState {
        val newDialogStates = when (dialogType) {
            DialogType.DATE_PICKER -> dialogStates.copy(
                showDatePicker = true,
                showPhysicalWalletPicker = false,
                showLogicalWalletPicker = false,
                showSourceWalletPicker = false,
                showDestinationWalletPicker = false
            )
            DialogType.PHYSICAL_WALLET -> dialogStates.copy(
                showDatePicker = false,
                showPhysicalWalletPicker = true,
                showLogicalWalletPicker = false,
                showSourceWalletPicker = false,
                showDestinationWalletPicker = false
            )
            DialogType.LOGICAL_WALLET -> dialogStates.copy(
                showDatePicker = false,
                showPhysicalWalletPicker = false,
                showLogicalWalletPicker = true,
                showSourceWalletPicker = false,
                showDestinationWalletPicker = false
            )
            DialogType.SOURCE_WALLET -> dialogStates.copy(
                showDatePicker = false,
                showPhysicalWalletPicker = false,
                showLogicalWalletPicker = false,
                showSourceWalletPicker = true,
                showDestinationWalletPicker = false
            )
            DialogType.DESTINATION_WALLET -> dialogStates.copy(
                showDatePicker = false,
                showPhysicalWalletPicker = false,
                showLogicalWalletPicker = false,
                showSourceWalletPicker = false,
                showDestinationWalletPicker = true
            )
        }

        return copy(dialogStates = newDialogStates)
    }

    /**
     * Closes all dialogs.
     */
    fun closeAllDialogs(): CreateTransactionUiState {
        return copy(dialogStates = DialogStates())
    }

    /**
     * Creates initial state with pre-selected wallet if provided.
     */
    companion object {
        fun withPreSelectedWallet(
            walletId: String?,
            walletType: String?,
            initialTransactionType: TransactionType?
        ): CreateTransactionUiState {
            // This will be implemented when wallet repository is available
            // For now, return default state
            return CreateTransactionUiState(
                selectedType = initialTransactionType ?: TransactionType.EXPENSE
            )
        }

        /**
         * Creates initial state for editing an existing transaction.
         *
         * Tags are normalized during loading to ensure consistency:
         * - Converted to lowercase
         * - Trimmed of whitespace
         * - Deduplicated (case-insensitive)
         */
        fun fromExistingTransaction(
            title: String,
            amount: Double,
            type: TransactionType,
            tags: List<String>,
            date: Date
        ): CreateTransactionUiState {
            // Normalize tags when loading from existing transaction
            val normalizedTags = TagNormalizer.normalizeTags(tags).joinToString(", ")

            return CreateTransactionUiState(
                title = title,
                amount = Math.abs(amount).toString(),
                selectedType = type,
                tags = normalizedTags,
                selectedDate = date
            ).let { state ->
                state.copy(
                    validationErrors = validateState(state),
                    isFormValid = isStateValid(state)
                )
            }
        }
    }
}


/**
 * Validates the current state and returns validation errors.
 */
private fun validateState(state: CreateTransactionUiState): ValidationErrors {
    val titleError = when {
        state.title.isBlank() -> "Title is required"
        state.title.length > ValidationConstants.TRANSACTION_TITLE_MAX_LENGTH ->
            "Title must be less than ${ValidationConstants.TRANSACTION_TITLE_MAX_LENGTH} characters"
        else -> null
    }

    val amountError = when {
        state.amount.isBlank() -> "Amount is required"
        state.amount.toDoubleOrNull() == null -> "Amount must be a valid number"
        state.amount.toDoubleOrNull()?.let { it <= 0 } == true -> "Amount must be greater than 0"
        else -> null
    }

    val walletError = when (state.selectedType) {
        TransactionType.INCOME, TransactionType.EXPENSE -> {
            if (!state.selectedWallets.isValidForTransaction) {
                "At least one wallet must be selected"
            } else null
        }
        TransactionType.TRANSFER -> null // Transfer validation handled separately
    }

    val transferError = when (state.selectedType) {
        TransactionType.TRANSFER -> {
            when {
                state.selectedWallets.source == null -> "Source wallet is required for transfers"
                state.selectedWallets.destination == null -> "Destination wallet is required for transfers"
                state.selectedWallets.source == state.selectedWallets.destination -> "Source and destination wallets must be different"
                state.selectedWallets.source?.walletType != state.selectedWallets.destination?.walletType -> "Source and destination wallets must be the same type"
                // Currency validation will be added when currency support is implemented
                false -> "Source and destination wallets must use the same currency"
                else -> null
            }
        }
        else -> null
    }

    return ValidationErrors(
        titleError = titleError,
        amountError = amountError,
        walletError = walletError,
        transferError = transferError
    )
}

/**
 * Checks if the current state is valid for form submission.
 */
private fun isStateValid(state: CreateTransactionUiState): Boolean {
    val validationErrors = validateState(state)
    return !validationErrors.hasErrors
}
package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import com.axeven.profiteerapp.utils.TagNormalizer
import java.util.*

/**
 * State management functions for CreateTransactionScreen.
 *
 * This file contains pure functions for updating CreateTransactionUiState,
 * following functional programming principles and maintaining immutability.
 *
 * Each function takes a current state and returns a new state with the
 * requested changes, automatically triggering validation and maintaining
 * consistency across the entire state object.
 *
 * Following TDD methodology - these implementations make the failing tests pass.
 */

/**
 * Updates the transaction title and triggers form validation.
 *
 * @param state Current UI state
 * @param title New title value
 * @return Updated state with new title and refreshed validation
 */
fun updateTitle(state: CreateTransactionUiState, title: String): CreateTransactionUiState {
    return state.updateAndValidate(title = title)
}

/**
 * Updates the transaction amount and triggers form validation.
 *
 * @param state Current UI state
 * @param amount New amount value as string
 * @return Updated state with new amount and refreshed validation
 */
fun updateAmount(state: CreateTransactionUiState, amount: String): CreateTransactionUiState {
    return state.updateAndValidate(amount = amount)
}

/**
 * Updates the transaction type and clears incompatible fields.
 *
 * When switching from TRANSFER to INCOME/EXPENSE, clears transfer-specific wallets.
 * When switching to TRANSFER, preserves existing wallet selections.
 *
 * @param state Current UI state
 * @param transactionType New transaction type
 * @return Updated state with new type and compatible wallet selections
 */
fun updateTransactionType(
    state: CreateTransactionUiState,
    transactionType: TransactionType
): CreateTransactionUiState {
    return state.updateTransactionType(transactionType)
}

/**
 * Updates the physical wallet selection and closes the wallet picker dialog.
 *
 * @param state Current UI state
 * @param wallet New physical wallet (null to clear selection)
 * @return Updated state with new physical wallet and closed dialog
 */
fun updatePhysicalWallet(
    state: CreateTransactionUiState,
    wallet: Wallet?
): CreateTransactionUiState {
    val updatedWallets = state.selectedWallets.updatePhysical(wallet)
    val updatedDialogStates = state.dialogStates.copy(showPhysicalWalletPicker = false)

    return state.updateAndValidate(
        selectedWallets = updatedWallets,
        dialogStates = updatedDialogStates
    )
}

/**
 * Updates the logical wallet selection and closes the wallet picker dialog.
 *
 * @param state Current UI state
 * @param wallet New logical wallet (null to clear selection)
 * @return Updated state with new logical wallet and closed dialog
 */
fun updateLogicalWallet(
    state: CreateTransactionUiState,
    wallet: Wallet?
): CreateTransactionUiState {
    val updatedWallets = state.selectedWallets.updateLogical(wallet)
    val updatedDialogStates = state.dialogStates.copy(showLogicalWalletPicker = false)

    return state.updateAndValidate(
        selectedWallets = updatedWallets,
        dialogStates = updatedDialogStates
    )
}

/**
 * Updates the source wallet for transfer transactions and closes the picker dialog.
 *
 * @param state Current UI state
 * @param wallet New source wallet (null to clear selection)
 * @return Updated state with new source wallet and closed dialog
 */
fun updateSourceWallet(
    state: CreateTransactionUiState,
    wallet: Wallet?
): CreateTransactionUiState {
    val updatedWallets = state.selectedWallets.updateSource(wallet)
    val updatedDialogStates = state.dialogStates.copy(showSourceWalletPicker = false)

    return state.updateAndValidate(
        selectedWallets = updatedWallets,
        dialogStates = updatedDialogStates
    )
}

/**
 * Updates the destination wallet for transfer transactions and closes the picker dialog.
 *
 * @param state Current UI state
 * @param wallet New destination wallet (null to clear selection)
 * @return Updated state with new destination wallet and closed dialog
 */
fun updateDestinationWallet(
    state: CreateTransactionUiState,
    wallet: Wallet?
): CreateTransactionUiState {
    val updatedWallets = state.selectedWallets.updateDestination(wallet)
    val updatedDialogStates = state.dialogStates.copy(showDestinationWalletPicker = false)

    return state.updateAndValidate(
        selectedWallets = updatedWallets,
        dialogStates = updatedDialogStates
    )
}

/**
 * Updates the selected date and closes the date picker dialog.
 *
 * @param state Current UI state
 * @param date New selected date
 * @return Updated state with new date and closed dialog
 */
fun updateSelectedDate(
    state: CreateTransactionUiState,
    date: Date
): CreateTransactionUiState {
    val updatedDialogStates = state.dialogStates.copy(showDatePicker = false)

    return state.updateAndValidate(
        selectedDate = date,
        dialogStates = updatedDialogStates
    )
}

/**
 * Updates the transaction tags and triggers form validation.
 *
 * Tags are automatically normalized:
 * - Trimmed of leading/trailing whitespace
 * - Converted to lowercase
 * - Deduplicated (case-insensitive)
 * - "Untagged" keyword removed
 *
 * @param state Current UI state
 * @param tags New tags string (comma-separated)
 * @return Updated state with normalized tags and refreshed validation
 */
fun updateTags(state: CreateTransactionUiState, tags: String): CreateTransactionUiState {
    val normalizedTags = if (tags.isBlank()) {
        ""
    } else {
        TagNormalizer.parseTagInput(tags).joinToString(", ")
    }
    return state.updateAndValidate(tags = normalizedTags)
}

/**
 * Opens a specific dialog while closing all others.
 *
 * Follows the business rule that only one dialog should be open at a time.
 *
 * @param state Current UI state
 * @param dialogType Type of dialog to open
 * @return Updated state with specified dialog open and others closed
 */
fun openDialog(
    state: CreateTransactionUiState,
    dialogType: DialogType
): CreateTransactionUiState {
    return state.openDialog(dialogType)
}

/**
 * Closes all open dialogs.
 *
 * @param state Current UI state
 * @return Updated state with all dialogs closed
 */
fun closeAllDialogs(state: CreateTransactionUiState): CreateTransactionUiState {
    return state.closeAllDialogs()
}

/**
 * Convenience function to update multiple fields atomically.
 *
 * This function allows updating multiple state fields in a single operation
 * while ensuring validation runs only once at the end.
 *
 * @param state Current UI state
 * @param title Optional new title
 * @param amount Optional new amount
 * @param transactionType Optional new transaction type
 * @param tags Optional new tags
 * @param date Optional new date
 * @return Updated state with all specified changes and refreshed validation
 */
fun updateMultipleFields(
    state: CreateTransactionUiState,
    title: String? = null,
    amount: String? = null,
    transactionType: TransactionType? = null,
    tags: String? = null,
    date: Date? = null
): CreateTransactionUiState {
    return state.updateAndValidate(
        title = title ?: state.title,
        amount = amount ?: state.amount,
        selectedType = transactionType ?: state.selectedType,
        tags = tags ?: state.tags,
        selectedDate = date ?: state.selectedDate
    )
}

/**
 * Resets the form to initial state while preserving any pre-selected wallets.
 *
 * Useful for clearing the form after successful submission or when user
 * wants to start over.
 *
 * @param state Current UI state
 * @param preserveWallets Whether to keep current wallet selections
 * @return Reset state with optional wallet preservation
 */
fun resetForm(
    state: CreateTransactionUiState,
    preserveWallets: Boolean = false
): CreateTransactionUiState {
    val wallets = if (preserveWallets) state.selectedWallets else SelectedWallets()

    return CreateTransactionUiState(
        selectedType = state.selectedType, // Preserve transaction type
        selectedWallets = wallets
    )
}

/**
 * Creates a copy of the current state for editing an existing transaction.
 *
 * This function is used when transitioning from create mode to edit mode,
 * preserving the current form state while preparing for edit operations.
 *
 * @param state Current UI state
 * @param existingTransactionId ID of transaction being edited
 * @return State configured for editing with preserved form data
 */
fun prepareForEdit(
    state: CreateTransactionUiState,
    existingTransactionId: String
): CreateTransactionUiState {
    // For now, just return the current state
    // In future iterations, this might load existing transaction data
    return state.copy(
        validationErrors = ValidationErrors(), // Clear any validation errors
        dialogStates = DialogStates() // Close any open dialogs
    )
}

/**
 * Validates the current state and returns whether the form is ready for submission.
 *
 * This is a convenience function that provides a quick check without needing
 * to access the state's validation properties directly.
 *
 * @param state Current UI state
 * @return True if the form is valid and ready for submission
 */
fun isFormReadyForSubmission(state: CreateTransactionUiState): Boolean {
    return state.isFormValid && !state.validationErrors.hasErrors
}

/**
 * Gets a summary of the current transaction for confirmation display.
 *
 * This function creates a human-readable summary of the transaction
 * for display in confirmation dialogs or review screens.
 *
 * @param state Current UI state
 * @return Transaction summary object
 */
fun getTransactionSummary(state: CreateTransactionUiState): TransactionSummary {
    return TransactionSummary(
        title = state.title,
        amount = state.amount.toDoubleOrNull() ?: 0.0,
        type = state.selectedType,
        wallets = when (state.selectedType) {
            TransactionType.TRANSFER -> listOfNotNull(
                state.selectedWallets.source,
                state.selectedWallets.destination
            )
            else -> state.selectedWallets.allSelected
        },
        tags = if (state.tags.isBlank()) emptyList() else
               TagNormalizer.parseTagInput(state.tags),
        date = state.selectedDate,
        isValid = state.isFormValid
    )
}

/**
 * Data class representing a transaction summary for display purposes.
 */
data class TransactionSummary(
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val wallets: List<Wallet>,
    val tags: List<String>,
    val date: Date,
    val isValid: Boolean
) {
    /**
     * Gets a formatted amount string for display.
     */
    val formattedAmount: String
        get() = String.format(Locale.US, "%.2f", amount)

    /**
     * Gets a formatted date string for display.
     * Note: This is a simplified format. In actual usage, pass a Context to get proper formatting.
     */
    val formattedDate: String
        get() = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)

    /**
     * Gets a comma-separated wallet names string.
     */
    val walletNames: String
        get() = wallets.joinToString(", ") { it.name }

    /**
     * Gets a comma-separated tags string.
     */
    val tagsString: String
        get() = tags.joinToString(", ")
}
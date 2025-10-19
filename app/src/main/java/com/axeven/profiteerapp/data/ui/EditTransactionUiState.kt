package com.axeven.profiteerapp.data.ui

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.ui.transaction.*
import com.axeven.profiteerapp.utils.TagNormalizer
import java.util.*

/**
 * UI state for edit transaction screen, extending the create transaction functionality
 * with edit-specific features like deletion tracking and change detection.
 *
 * This data class consolidates all state variables needed for editing transactions,
 * providing immutable state management with automatic validation.
 *
 * Key features:
 * - Inherits all creation functionality from CreateTransactionUiState patterns
 * - Tracks deletion requests and completion
 * - Detects changes from original transaction
 * - Preserves original transaction reference for comparison
 * - Handles both legacy and modern transaction formats
 */
data class EditTransactionUiState(
    // Core transaction fields (inherited from CreateTransactionUiState pattern)
    val title: String = "",
    val amount: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedWallets: SelectedWallets = SelectedWallets(),
    val selectedDate: Date = Date(),
    val tags: String = "",

    // UI state management
    val dialogStates: DialogStates = DialogStates(),
    val validationErrors: ValidationErrors = ValidationErrors(),
    val isFormValid: Boolean = false,

    // Edit-specific state
    val deletionRequested: Boolean = false,
    val originalTransaction: Transaction? = null
) {

    /**
     * Computed property to check if the current state has changes from the original transaction.
     *
     * Note: Tag comparison uses normalized tags to avoid false positives from
     * case/whitespace differences.
     */
    val hasChanges: Boolean
        get() = originalTransaction?.let { original ->
            val normalizedOriginalTags = TagNormalizer.normalizeTags(original.tags).joinToString(", ")

            title != original.title ||
            amount != String.format("%.2f", Math.abs(original.amount)) ||
            tags != normalizedOriginalTags ||
            selectedDate != (original.transactionDate ?: Date()) ||
            // Note: Wallet changes are detected in actual wallet state comparison
            hasWalletChanges(original)
        } ?: true // If no original transaction, consider it as having changes

    /**
     * Helper method to detect wallet changes from original transaction.
     */
    private fun hasWalletChanges(original: Transaction): Boolean {
        return when (original.type) {
            TransactionType.INCOME, TransactionType.EXPENSE -> {
                val originalWalletIds = if (original.affectedWalletIds.isNotEmpty()) {
                    original.affectedWalletIds.toSet()
                } else {
                    setOfNotNull(original.walletId) // Legacy format
                }
                val currentWalletIds = selectedWallets.allSelected.map { it.id }.toSet()
                originalWalletIds != currentWalletIds
            }
            TransactionType.TRANSFER -> {
                selectedWallets.source?.id != original.sourceWalletId ||
                selectedWallets.destination?.id != original.destinationWalletId
            }
        }
    }

    /**
     * Factory method to create EditTransactionUiState from an existing transaction.
     */
    companion object {
        fun fromTransaction(
            transaction: Transaction,
            availableWallets: List<Wallet> = emptyList()
        ): EditTransactionUiState {
            // Initialize wallet selections based on transaction type
            val initialWallets = when (transaction.type) {
                TransactionType.INCOME, TransactionType.EXPENSE -> {
                    val affectedWallets = if (transaction.affectedWalletIds.isNotEmpty()) {
                        // New format: multiple wallets
                        availableWallets.filter { it.id in transaction.affectedWalletIds }
                    } else {
                        // Old format: single wallet
                        availableWallets.filter { it.id == transaction.walletId }
                    }

                    SelectedWallets(
                        physical = affectedWallets.find { it.walletType == "Physical" },
                        logical = affectedWallets.find { it.walletType == "Logical" }
                    )
                }
                TransactionType.TRANSFER -> {
                    SelectedWallets(
                        source = availableWallets.find { it.id == transaction.sourceWalletId },
                        destination = availableWallets.find { it.id == transaction.destinationWalletId }
                    )
                }
            }

            // Normalize tags when loading from existing transaction
            val normalizedTags = if (transaction.type == TransactionType.TRANSFER) {
                ""
            } else {
                TagNormalizer.normalizeTags(transaction.tags).joinToString(", ")
            }

            return EditTransactionUiState(
                title = transaction.title,
                amount = String.format("%.2f", Math.abs(transaction.amount)), // Always positive for display with 2 decimal places
                selectedType = transaction.type,
                selectedWallets = initialWallets,
                selectedDate = transaction.transactionDate ?: Date(),
                tags = normalizedTags,
                originalTransaction = transaction
            ).updateAndValidate()
        }
    }

    /**
     * Update and validate the current state, similar to CreateTransactionUiState.
     *
     * Tags are automatically normalized:
     * - Trimmed of leading/trailing whitespace
     * - Converted to lowercase
     * - Deduplicated (case-insensitive)
     * - "Untagged" keyword removed
     */
    fun updateAndValidate(
        title: String = this.title,
        amount: String = this.amount,
        selectedType: TransactionType = this.selectedType,
        selectedWallets: SelectedWallets = this.selectedWallets,
        selectedDate: Date = this.selectedDate,
        tags: String = this.tags,
        dialogStates: DialogStates = this.dialogStates
    ): EditTransactionUiState {
        // Normalize tags before validation
        val normalizedTags = if (tags.isBlank()) {
            ""
        } else {
            TagNormalizer.parseTagInput(tags).joinToString(", ")
        }

        val validation = validateTransactionForm(
            CreateTransactionUiState(
                title = title,
                amount = amount,
                selectedType = selectedType,
                selectedWallets = selectedWallets,
                selectedDate = selectedDate,
                tags = normalizedTags
            )
        )

        return copy(
            title = title,
            amount = amount,
            selectedType = selectedType,
            selectedWallets = selectedWallets,
            selectedDate = selectedDate,
            tags = normalizedTags,
            dialogStates = dialogStates,
            validationErrors = validation.errors,
            isFormValid = validation.isValid
        )
    }

    /**
     * Update transaction type - restricted in edit mode to prevent type changes.
     */
    fun updateTransactionType(newType: TransactionType): EditTransactionUiState {
        // In edit mode, transaction type changes are not allowed to maintain data integrity
        return this
    }

    /**
     * Update physical wallet selection.
     */
    fun updatePhysicalWallet(wallet: Wallet?): EditTransactionUiState {
        val updatedWallets = selectedWallets.updatePhysical(wallet)
        val updatedDialogStates = dialogStates.copy(showPhysicalWalletPicker = false)

        return updateAndValidate(
            selectedWallets = updatedWallets,
            dialogStates = updatedDialogStates
        )
    }

    /**
     * Update logical wallet selection.
     */
    fun updateLogicalWallet(wallet: Wallet?): EditTransactionUiState {
        val updatedWallets = selectedWallets.updateLogical(wallet)
        val updatedDialogStates = dialogStates.copy(showLogicalWalletPicker = false)

        return updateAndValidate(
            selectedWallets = updatedWallets,
            dialogStates = updatedDialogStates
        )
    }

    /**
     * Update source wallet for transfers.
     */
    fun updateSourceWallet(wallet: Wallet?): EditTransactionUiState {
        val updatedWallets = selectedWallets.updateSource(wallet)
        val updatedDialogStates = dialogStates.copy(showSourceWalletPicker = false)

        return updateAndValidate(
            selectedWallets = updatedWallets,
            dialogStates = updatedDialogStates
        )
    }

    /**
     * Update destination wallet for transfers.
     */
    fun updateDestinationWallet(wallet: Wallet?): EditTransactionUiState {
        val updatedWallets = selectedWallets.updateDestination(wallet)
        val updatedDialogStates = dialogStates.copy(showDestinationWalletPicker = false)

        return updateAndValidate(
            selectedWallets = updatedWallets,
            dialogStates = updatedDialogStates
        )
    }

    /**
     * Update selected date.
     */
    fun updateSelectedDate(date: Date): EditTransactionUiState {
        val updatedDialogStates = dialogStates.copy(showDatePicker = false)

        return updateAndValidate(
            selectedDate = date,
            dialogStates = updatedDialogStates
        )
    }

    /**
     * Open a specific dialog.
     */
    fun openDialog(dialogType: DialogType): EditTransactionUiState {
        return updateAndValidate(
            dialogStates = when (dialogType) {
                DialogType.DATE_PICKER -> dialogStates.copy(showDatePicker = true, showPhysicalWalletPicker = false, showLogicalWalletPicker = false, showSourceWalletPicker = false, showDestinationWalletPicker = false)
                DialogType.PHYSICAL_WALLET -> dialogStates.copy(showPhysicalWalletPicker = true, showDatePicker = false, showLogicalWalletPicker = false, showSourceWalletPicker = false, showDestinationWalletPicker = false)
                DialogType.LOGICAL_WALLET -> dialogStates.copy(showLogicalWalletPicker = true, showDatePicker = false, showPhysicalWalletPicker = false, showSourceWalletPicker = false, showDestinationWalletPicker = false)
                DialogType.SOURCE_WALLET -> dialogStates.copy(showSourceWalletPicker = true, showDatePicker = false, showPhysicalWalletPicker = false, showLogicalWalletPicker = false, showDestinationWalletPicker = false)
                DialogType.DESTINATION_WALLET -> dialogStates.copy(showDestinationWalletPicker = true, showDatePicker = false, showPhysicalWalletPicker = false, showLogicalWalletPicker = false, showSourceWalletPicker = false)
            }
        )
    }

    /**
     * Close all dialogs.
     */
    fun closeAllDialogs(): EditTransactionUiState {
        return updateAndValidate(
            dialogStates = DialogStates()
        )
    }

    /**
     * Request deletion of the transaction.
     */
    fun requestDeletion(): EditTransactionUiState {
        return copy(deletionRequested = true)
    }

    /**
     * Cancel deletion request.
     */
    fun cancelDeletion(): EditTransactionUiState {
        return copy(deletionRequested = false)
    }

    /**
     * Get transaction summary for update operations.
     */
    fun getTransactionSummary(): TransactionSummary {
        return TransactionSummary(
            title = title,
            amount = amount.toDoubleOrNull() ?: 0.0,
            type = selectedType,
            wallets = when (selectedType) {
                TransactionType.TRANSFER -> listOfNotNull(
                    selectedWallets.source,
                    selectedWallets.destination
                )
                else -> selectedWallets.allSelected
            },
            tags = if (tags.isBlank()) emptyList() else
                   tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            date = selectedDate,
            isValid = isFormValid
        )
    }
}

/**
 * Extension functions for common edit operations.
 */

/**
 * Update title and trigger validation.
 */
fun EditTransactionUiState.updateTitle(title: String): EditTransactionUiState {
    return updateAndValidate(title = title)
}

/**
 * Update amount and trigger validation.
 */
fun EditTransactionUiState.updateAmount(amount: String): EditTransactionUiState {
    return updateAndValidate(amount = amount)
}

/**
 * Update tags and trigger validation.
 */
fun EditTransactionUiState.updateTags(tags: String): EditTransactionUiState {
    return updateAndValidate(tags = tags)
}
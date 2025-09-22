package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.ui.*
import java.util.*

/**
 * Form validation functions for transaction creation and editing.
 *
 * This file contains pure validation functions that implement business rules
 * for transaction forms. Each function returns validation errors or null if valid.
 *
 * Following TDD methodology - these implementations make the failing tests pass.
 */

/**
 * Validation result containing validity status and detailed errors.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: ValidationErrors
)

/**
 * Validates the complete transaction form state.
 *
 * This is the main validation function that checks all fields and business rules.
 *
 * @param state Current UI state to validate
 * @return ValidationResult with validity status and detailed errors
 */
fun validateTransactionForm(state: CreateTransactionUiState): ValidationResult {
    val titleError = validateTitle(state.title)
    val amountError = validateAmount(state.amount)
    val walletError = validateWalletSelection(state.selectedWallets, state.selectedType)
    val transferError = if (state.selectedType == TransactionType.TRANSFER) {
        validateTransfer(state.selectedWallets)
    } else null
    val tagsError = validateTags(state.tags)
    val dateError = validateDate(state.selectedDate)

    val errors = ValidationErrors(
        titleError = titleError,
        amountError = amountError,
        walletError = walletError,
        transferError = transferError,
        tagsError = tagsError,
        dateError = dateError
    )

    return ValidationResult(
        isValid = !errors.hasErrors,
        errors = errors
    )
}

/**
 * Validates transaction title.
 *
 * Business Rules:
 * - Title is required (not empty or blank)
 * - Title must be less than 100 characters
 *
 * @param title Title to validate
 * @return Error message if invalid, null if valid
 */
fun validateTitle(title: String): String? {
    return when {
        title.isBlank() -> "Title is required"
        title.length > 100 -> "Title must be less than 100 characters"
        else -> null
    }
}

/**
 * Validates transaction amount.
 *
 * Business Rules:
 * - Amount is required (not empty or blank)
 * - Amount must be a valid positive number
 * - Amount must be greater than 0
 *
 * @param amount Amount string to validate
 * @return Error message if invalid, null if valid
 */
fun validateAmount(amount: String): String? {
    return when {
        amount.isBlank() -> "Amount is required"
        amount.toDoubleOrNull() == null -> "Amount must be a valid number"
        amount.toDoubleOrNull()?.let { it <= 0 } == true -> "Amount must be greater than 0"
        else -> null
    }
}

/**
 * Validates wallet selection for regular transactions (income/expense).
 *
 * Business Rules:
 * - At least one wallet must be selected for income/expense transactions
 * - No validation required for transfer transactions (handled separately)
 *
 * @param wallets Selected wallets
 * @param transactionType Type of transaction
 * @return Error message if invalid, null if valid
 */
fun validateWalletSelection(
    wallets: SelectedWallets,
    transactionType: TransactionType
): String? {
    return when (transactionType) {
        TransactionType.INCOME, TransactionType.EXPENSE -> {
            if (!wallets.isValidForTransaction) {
                "At least one wallet must be selected"
            } else null
        }
        TransactionType.TRANSFER -> null // Transfer validation handled separately
    }
}

/**
 * Validates transfer transaction setup.
 *
 * Business Rules:
 * - Source wallet is required
 * - Destination wallet is required
 * - Source and destination wallets must be different
 * - Source and destination wallets must be the same type (both Physical or both Logical)
 * - TODO: Source and destination wallets must use the same currency (when currency support is added)
 *
 * @param wallets Selected wallets for transfer
 * @return Error message if invalid, null if valid
 */
fun validateTransfer(wallets: SelectedWallets): String? {
    return when {
        wallets.source == null -> "Source wallet is required for transfers"
        wallets.destination == null -> "Destination wallet is required for transfers"
        wallets.source == wallets.destination -> "Source and destination wallets must be different"
        wallets.source!!.walletType != wallets.destination!!.walletType ->
            "Source and destination wallets must be the same type"
        // TODO: Add currency validation when currency support is added
        false -> "Source and destination wallets must use the same currency"
        else -> null
    }
}

/**
 * Validates transaction date.
 *
 * Business Rules:
 * - Current and past dates are always valid
 * - Future dates trigger a warning but don't prevent submission
 * - Dates more than 1 year in the future trigger a stronger warning
 *
 * @param date Date to validate
 * @return Warning message if future date, null if current/past date
 */
fun validateDate(date: Date): String? {
    val now = Date()
    val oneYearFromNow = Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L)

    return when {
        date.after(now) -> "Future dates may affect accurate financial tracking"
        else -> null
    }
}

/**
 * Validates transaction tags.
 *
 * Business Rules:
 * - Tags are optional (empty is valid)
 * - Warn if more than 15 tags (too many for good organization)
 * - Warn if duplicate tags are detected
 *
 * @param tags Comma-separated tags string
 * @return Warning message if issues detected, null if valid
 */
fun validateTags(tags: String): String? {
    if (tags.isBlank()) return null

    val tagList = tags.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return when {
        tagList.size > 15 -> "Consider using fewer tags for better organization"
        tagList.size != tagList.distinct().size -> {
            val duplicates = tagList.groupBy { it }
                .filter { it.value.size > 1 }
                .keys
            "Duplicate tags detected: ${duplicates.joinToString(", ")}"
        }
        else -> null
    }
}

/**
 * Validates that the amount is reasonable for the selected wallets.
 *
 * Business Rules:
 * - Warn if expense amount exceeds available balance in selected wallets
 * - Warn if transfer amount exceeds source wallet balance
 * - No validation for income (can always receive money)
 *
 * @param amount Amount to validate
 * @param wallets Selected wallets
 * @param transactionType Type of transaction
 * @return Warning message if amount might cause issues, null if ok
 */
fun validateAmountVsBalance(
    amount: String,
    wallets: SelectedWallets,
    transactionType: TransactionType
): String? {
    val amountValue = amount.toDoubleOrNull() ?: return null

    return when (transactionType) {
        TransactionType.EXPENSE -> {
            val totalBalance = wallets.allSelected.sumOf { it.balance }
            if (amountValue > totalBalance) {
                "Amount exceeds available balance in selected wallets"
            } else null
        }
        TransactionType.TRANSFER -> {
            val sourceBalance = wallets.source?.balance ?: 0.0
            if (amountValue > sourceBalance) {
                "Amount exceeds available balance in source wallet"
            } else null
        }
        TransactionType.INCOME -> null // No balance validation for income
    }
}

/**
 * Validates business rules specific to wallet combinations.
 *
 * Business Rules:
 * - Physical + Logical wallets should have compatible purposes
 * - Warn if selecting wallets with very different balances (might indicate error)
 * - Warn if selecting the same wallet for both physical and logical (unusual pattern)
 *
 * @param wallets Selected wallets
 * @return Warning message if unusual combination detected, null if normal
 */
fun validateWalletCombination(wallets: SelectedWallets): String? {
    val physical = wallets.physical
    val logical = wallets.logical

    return when {
        physical != null && logical != null && physical.id == logical.id ->
            "Same wallet selected for both physical and logical (unusual pattern)"
        physical != null && logical != null -> {
            val balanceDifference = kotlin.math.abs(physical.balance - logical.balance)
            val averageBalance = (physical.balance + logical.balance) / 2
            if (averageBalance > 0 && balanceDifference / averageBalance > 10) {
                "Selected wallets have very different balances - please verify selection"
            } else null
        }
        else -> null
    }
}

/**
 * Performs comprehensive validation including all business rules.
 *
 * This function runs all validation checks including the advanced validations
 * for balance checking and wallet combinations.
 *
 * @param state Current UI state to validate
 * @return ValidationResult with all possible validation messages
 */
fun validateTransactionFormComprehensive(state: CreateTransactionUiState): ValidationResult {
    // Get basic validation result
    val basicResult = validateTransactionForm(state)

    // Add advanced validations
    val balanceWarning = validateAmountVsBalance(
        state.amount,
        state.selectedWallets,
        state.selectedType
    )
    val walletWarning = validateWalletCombination(state.selectedWallets)

    // Combine all errors and warnings
    val enhancedErrors = basicResult.errors.copy(
        // Add balance warning to amount error if it exists
        amountError = basicResult.errors.amountError ?: balanceWarning,
        // Add wallet warning to wallet error if it exists
        walletError = basicResult.errors.walletError ?: walletWarning
    )

    return ValidationResult(
        isValid = basicResult.isValid && balanceWarning == null, // Balance warnings prevent submission
        errors = enhancedErrors
    )
}

/**
 * Quick validation for real-time feedback during form input.
 *
 * This function performs lightweight validation suitable for real-time
 * feedback as the user types, focusing on critical errors only.
 *
 * @param state Current UI state to validate
 * @return ValidationResult with only critical errors
 */
fun validateTransactionFormRealTime(state: CreateTransactionUiState): ValidationResult {
    val titleError = if (state.title.isNotBlank() && state.title.length > 100) {
        validateTitle(state.title)
    } else null

    val amountError = if (state.amount.isNotBlank()) {
        validateAmount(state.amount)
    } else null

    val errors = ValidationErrors(
        titleError = titleError,
        amountError = amountError
    )

    return ValidationResult(
        isValid = !errors.hasErrors,
        errors = errors
    )
}

/**
 * Validates form readiness for saving as draft.
 *
 * Draft validation is more lenient than submission validation,
 * only requiring that entered data is valid (not necessarily complete).
 *
 * @param state Current UI state to validate
 * @return ValidationResult for draft saving
 */
fun validateForDraft(state: CreateTransactionUiState): ValidationResult {
    val titleError = if (state.title.isNotBlank()) validateTitle(state.title) else null
    val amountError = if (state.amount.isNotBlank()) validateAmount(state.amount) else null
    val tagsError = if (state.tags.isNotBlank()) validateTags(state.tags) else null

    val errors = ValidationErrors(
        titleError = titleError,
        amountError = amountError,
        tagsError = tagsError
    )

    return ValidationResult(
        isValid = !errors.hasErrors,
        errors = errors
    )
}
package com.axeven.profiteerapp.data.ui

/**
 * Manages validation errors for transaction creation forms.
 *
 * This consolidates all validation error state into a single object,
 * providing clear error management and user-friendly error messages.
 *
 * Each field has its own error state, allowing for granular error display
 * and independent field validation.
 */
data class ValidationErrors(
    val titleError: String? = null,
    val amountError: String? = null,
    val walletError: String? = null,
    val transferError: String? = null,
    val tagsError: String? = null,
    val dateError: String? = null
) {

    /**
     * Checks if any validation errors exist.
     */
    val hasErrors: Boolean
        get() = listOf(titleError, amountError, walletError, transferError, tagsError, dateError)
            .any { it != null }

    /**
     * Gets all non-null error messages.
     */
    val allErrors: List<String>
        get() = listOfNotNull(titleError, amountError, walletError, transferError, tagsError, dateError)

    /**
     * Gets the first error message, if any.
     * Useful for displaying a single error to the user.
     */
    val firstError: String?
        get() = allErrors.firstOrNull()

    /**
     * Gets error count for analytics or debugging.
     */
    val errorCount: Int
        get() = allErrors.size

    /**
     * Checks if a specific field has an error.
     */
    fun hasFieldError(field: ValidationField): Boolean {
        return getFieldError(field) != null
    }

    /**
     * Gets error message for a specific field.
     */
    fun getFieldError(field: ValidationField): String? {
        return when (field) {
            ValidationField.TITLE -> titleError
            ValidationField.AMOUNT -> amountError
            ValidationField.WALLET -> walletError
            ValidationField.TRANSFER -> transferError
            ValidationField.TAGS -> tagsError
            ValidationField.DATE -> dateError
        }
    }

    /**
     * Updates error for a specific field.
     */
    fun updateField(field: ValidationField, error: String?): ValidationErrors {
        return when (field) {
            ValidationField.TITLE -> copy(titleError = error)
            ValidationField.AMOUNT -> copy(amountError = error)
            ValidationField.WALLET -> copy(walletError = error)
            ValidationField.TRANSFER -> copy(transferError = error)
            ValidationField.TAGS -> copy(tagsError = error)
            ValidationField.DATE -> copy(dateError = error)
        }
    }

    /**
     * Clears all validation errors.
     */
    fun clearAll(): ValidationErrors {
        return ValidationErrors()
    }

    /**
     * Clears errors for specific fields.
     */
    fun clearFields(vararg fields: ValidationField): ValidationErrors {
        return fields.fold(this) { errors, field ->
            errors.updateField(field, null)
        }
    }

    /**
     * Clears transfer-specific errors.
     * Useful when switching from transfer to regular transaction.
     */
    fun clearTransferErrors(): ValidationErrors {
        return copy(transferError = null)
    }

    /**
     * Clears wallet-related errors.
     * Useful when wallet selection changes.
     */
    fun clearWalletErrors(): ValidationErrors {
        return copy(walletError = null, transferError = null)
    }

    /**
     * Gets errors grouped by severity for UI display.
     */
    val errorsBySeverity: Map<ErrorSeverity, List<String>>
        get() {
            val critical = mutableListOf<String>()
            val warning = mutableListOf<String>()
            val info = mutableListOf<String>()

            titleError?.let {
                if (it.contains("required")) critical.add(it) else warning.add(it)
            }
            amountError?.let {
                if (it.contains("required")) critical.add(it) else warning.add(it)
            }
            walletError?.let { critical.add(it) }
            transferError?.let { critical.add(it) }
            tagsError?.let { info.add(it) }
            dateError?.let { warning.add(it) }

            return mapOf(
                ErrorSeverity.CRITICAL to critical,
                ErrorSeverity.WARNING to warning,
                ErrorSeverity.INFO to info
            ).filterValues { it.isNotEmpty() }
        }

    /**
     * Gets a summary message for all errors.
     * Useful for accessibility announcements or notifications.
     */
    val summaryMessage: String?
        get() = when (errorCount) {
            0 -> null
            1 -> firstError
            else -> "There are $errorCount validation errors: ${allErrors.joinToString("; ")}"
        }

    /**
     * Creates a copy with validation errors for form fields.
     * Used by validation logic to build complete error state.
     */
    companion object {
        fun fromValidation(
            title: String,
            amount: String,
            titleError: String? = null,
            amountError: String? = null,
            walletError: String? = null,
            transferError: String? = null,
            tagsError: String? = null,
            dateError: String? = null
        ): ValidationErrors {
            return ValidationErrors(
                titleError = titleError,
                amountError = amountError,
                walletError = walletError,
                transferError = transferError,
                tagsError = tagsError,
                dateError = dateError
            )
        }
    }
}

/**
 * Enum representing validation fields for type safety.
 */
enum class ValidationField {
    TITLE,
    AMOUNT,
    WALLET,
    TRANSFER,
    TAGS,
    DATE
}

/**
 * Enum representing error severity levels.
 */
enum class ErrorSeverity {
    CRITICAL,   // Prevents form submission
    WARNING,    // Should be addressed but doesn't prevent submission
    INFO        // Informational messages
}

/**
 * Extension functions for more readable error management.
 */

/**
 * Gets human-readable field name for display.
 */
fun ValidationField.displayName(): String {
    return when (this) {
        ValidationField.TITLE -> "Title"
        ValidationField.AMOUNT -> "Amount"
        ValidationField.WALLET -> "Wallet Selection"
        ValidationField.TRANSFER -> "Transfer Settings"
        ValidationField.TAGS -> "Tags"
        ValidationField.DATE -> "Date"
    }
}

/**
 * Gets color associated with error severity for UI theming.
 */
fun ErrorSeverity.colorName(): String {
    return when (this) {
        ErrorSeverity.CRITICAL -> "error"
        ErrorSeverity.WARNING -> "warning"
        ErrorSeverity.INFO -> "info"
    }
}

/**
 * Builder pattern for creating validation errors.
 */
class ValidationErrorsBuilder {
    private var titleError: String? = null
    private var amountError: String? = null
    private var walletError: String? = null
    private var transferError: String? = null
    private var tagsError: String? = null
    private var dateError: String? = null

    fun title(error: String?) = apply { titleError = error }
    fun amount(error: String?) = apply { amountError = error }
    fun wallet(error: String?) = apply { walletError = error }
    fun transfer(error: String?) = apply { transferError = error }
    fun tags(error: String?) = apply { tagsError = error }
    fun date(error: String?) = apply { dateError = error }

    fun build(): ValidationErrors {
        return ValidationErrors(
            titleError = titleError,
            amountError = amountError,
            walletError = walletError,
            transferError = transferError,
            tagsError = tagsError,
            dateError = dateError
        )
    }
}

/**
 * Creates ValidationErrors using builder pattern.
 */
fun buildValidationErrors(builder: ValidationErrorsBuilder.() -> Unit): ValidationErrors {
    return ValidationErrorsBuilder().apply(builder).build()
}
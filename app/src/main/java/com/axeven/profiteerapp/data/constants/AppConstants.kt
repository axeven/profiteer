package com.axeven.profiteerapp.data.constants

/**
 * Central repository for application-wide constants.
 * Use these constants instead of magic values for better maintainability.
 *
 * This file consolidates all hardcoded values that were previously scattered
 * throughout the codebase. See docs/antipatterns.md #6 for context.
 */

/**
 * Constants used across repository layer for query configuration and defaults.
 */
object RepositoryConstants {
    /**
     * Default page size for transaction queries.
     * Used when fetching recent transactions to limit result set size.
     */
    const val TRANSACTION_PAGE_SIZE = 20

    /**
     * Limit for queries that should return a single result.
     * Commonly used for user preferences and default value lookups.
     */
    const val SINGLE_RESULT_LIMIT = 1

    /**
     * Default currency code (ISO 4217 format).
     * Applied when user has not set a preferred currency.
     */
    const val DEFAULT_CURRENCY = "USD"

    /**
     * Maximum number of tag suggestions to show in autocomplete.
     * Prevents UI overflow while providing sufficient options.
     */
    const val MAX_TAG_SUGGESTIONS = 10
}

/**
 * Wallet type classification enum.
 * Replaces hardcoded "Physical" and "Logical" strings throughout the codebase.
 *
 * @property displayName Human-readable name for UI display and Firebase storage
 */
enum class WalletType(val displayName: String) {
    /**
     * Physical wallet - represents actual money storage (cash, bank account, etc.)
     * Sum of all physical wallet balances represents total actual wealth.
     */
    PHYSICAL("Physical"),

    /**
     * Logical wallet - represents virtual categorization of physical funds.
     * Used for budgeting and expense tracking purposes.
     */
    LOGICAL("Logical");

    companion object {
        /**
         * Converts a string representation to WalletType enum.
         * Case-insensitive matching against displayName.
         *
         * @param value String to convert (e.g., "Physical", "physical", "PHYSICAL")
         * @return Matching WalletType or null if no match found
         */
        fun fromString(value: String): WalletType? {
            val trimmedValue = value.trim()
            if (trimmedValue.isBlank()) return null
            return values().find { it.displayName.equals(trimmedValue, ignoreCase = true) }
        }
    }
}

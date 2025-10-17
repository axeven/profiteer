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
 * Constants for UI behavior and user experience.
 * Controls autocomplete, suggestions, and display preferences.
 */
object UIConstants {
    /**
     * Minimum number of characters required before showing tag autocomplete suggestions.
     * Prevents excessive filtering and improves performance.
     */
    const val TAG_AUTOCOMPLETE_MIN_CHARS = 3

    /**
     * Maximum number of tag suggestions to show in autocomplete dropdown.
     * Balances discoverability with UI simplicity.
     */
    const val TAG_SUGGESTION_LIMIT = 5

    /**
     * Number of transaction groups to initially show as expanded in list view.
     * Provides quick access to recent transactions without overwhelming the UI.
     */
    const val INITIAL_EXPANDED_GROUPS = 3
}

/**
 * Constants for form validation rules.
 * Defines length limits, thresholds, and validation constraints.
 */
object ValidationConstants {
    /**
     * Minimum length for wallet names.
     * Ensures wallet names are meaningful and identifiable.
     */
    const val WALLET_NAME_MIN_LENGTH = 2

    /**
     * Maximum length for wallet names.
     * Prevents overly long names that affect UI layout.
     */
    const val WALLET_NAME_MAX_LENGTH = 50

    /**
     * Maximum length for transaction titles.
     * Ensures titles are concise while allowing descriptive text.
     */
    const val TRANSACTION_TITLE_MAX_LENGTH = 100

    /**
     * Maximum number of tags allowed per transaction.
     * Prevents tag overuse that reduces organizational effectiveness.
     */
    const val MAX_TAGS_PER_TRANSACTION = 15

    /**
     * Threshold ratio for wallet balance difference warnings.
     * Triggers warning when physical/logical wallet balances differ by this factor.
     */
    const val BALANCE_DIFFERENCE_THRESHOLD = 10.0
}

/**
 * Constants for performance tuning and timeout configuration.
 * Controls retry delays, operation thresholds, and performance monitoring.
 */
object PerformanceConstants {
    /**
     * Delay in milliseconds before retrying failed operations.
     * Short delay prevents immediate retry storms while maintaining responsiveness.
     */
    const val RETRY_DELAY_MS = 100L

    /**
     * Threshold in milliseconds for identifying slow operations.
     * Operations exceeding this duration are logged for performance monitoring.
     */
    const val SLOW_OPERATION_THRESHOLD_MS = 5000L
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

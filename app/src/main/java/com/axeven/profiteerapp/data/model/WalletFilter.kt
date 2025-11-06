package com.axeven.profiteerapp.data.model

/**
 * Represents a wallet filter for portfolio reports.
 *
 * This sealed class defines two types of wallet filters that can be applied
 * to transactions and wallet balances in the Reports screen:
 * - **AllWallets**: No filtering, shows data from all wallets (default)
 * - **SpecificWallet**: Filters data to show only transactions and balances for a specific wallet
 *
 * ## Usage:
 * ```kotlin
 * // Show all wallets (no filter)
 * val filter = WalletFilter.AllWallets
 *
 * // Filter by specific wallet
 * val filter = WalletFilter.SpecificWallet(
 *     walletId = "wallet-123",
 *     walletName = "Cash Wallet"
 * )
 *
 * // Get display text
 * val displayText = filter.getDisplayText() // "All Wallets" or "Cash Wallet"
 * ```
 *
 * ## Integration with Reports:
 * The wallet filter works alongside date filters to provide granular transaction analysis:
 * - Filters transactions based on `affectedWalletIds` field
 * - Filters wallet balances using historical reconstruction
 * - Persists across chart type changes
 * - Resets gracefully if selected wallet is deleted
 *
 * @see com.axeven.profiteerapp.utils.WalletFilterUtils for filtering utility functions
 * @see com.axeven.profiteerapp.utils.BalanceReconstructionUtils for historical balance reconstruction
 * @see com.axeven.profiteerapp.viewmodel.ReportViewModel for usage in ViewModel
 */
sealed class WalletFilter {

    /**
     * Returns a human-readable display text for this filter.
     * Examples: "All Wallets", "Cash Wallet"
     */
    abstract fun getDisplayText(): String

    /**
     * Represents all-wallets filtering (no wallet restriction).
     */
    object AllWallets : WalletFilter() {
        override fun getDisplayText(): String = "All Wallets"

        override fun toString(): String = "AllWallets"
    }

    /**
     * Represents filtering by a specific wallet.
     *
     * @param walletId The unique ID of the wallet (must be non-blank)
     * @param walletName The display name of the wallet (must be non-blank)
     * @throws IllegalArgumentException if walletId or walletName is blank
     */
    data class SpecificWallet(
        val walletId: String,
        val walletName: String
    ) : WalletFilter() {

        init {
            require(walletId.isNotBlank()) {
                "walletId must not be blank"
            }
            require(walletName.isNotBlank()) {
                "walletName must not be blank"
            }
        }

        override fun getDisplayText(): String = walletName
    }
}

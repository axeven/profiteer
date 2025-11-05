package com.axeven.profiteerapp.data.model

/**
 * Represents a wallet filter for portfolio reports.
 *
 * This sealed class defines two types of wallet filters:
 * - AllWallets: No filtering, shows all wallets
 * - SpecificWallet: Filters data for a specific wallet by ID
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

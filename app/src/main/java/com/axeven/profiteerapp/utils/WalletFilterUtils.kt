package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.WalletFilter

/**
 * Utility functions for filtering transactions and wallets by wallet filter.
 *
 * This utility provides filtering logic for portfolio reports, allowing users to:
 * - View all data (AllWallets filter)
 * - View data for a specific wallet (SpecificWallet filter)
 *
 * Transaction filtering uses the `affectedWalletIds` field to determine which
 * transactions involve a specific wallet.
 */
object WalletFilterUtils {

    /**
     * Filters a list of transactions based on the wallet filter.
     *
     * Behavior:
     * - AllWallets: Returns all transactions unchanged
     * - SpecificWallet: Returns only transactions where the wallet ID appears in affectedWalletIds
     *
     * Transactions with empty affectedWalletIds are excluded when filtering by SpecificWallet.
     *
     * @param transactions The list of transactions to filter
     * @param filter The wallet filter to apply
     * @return Filtered list of transactions
     *
     * @see Transaction.affectedWalletIds
     */
    fun filterTransactionsByWallet(
        transactions: List<Transaction>,
        filter: WalletFilter
    ): List<Transaction> {
        return when (filter) {
            is WalletFilter.AllWallets -> transactions
            is WalletFilter.SpecificWallet -> {
                transactions.filter { transaction ->
                    transaction.affectedWalletIds.contains(filter.walletId)
                }
            }
        }
    }

    /**
     * Filters a list of wallets based on the wallet filter.
     *
     * Behavior:
     * - AllWallets: Returns all wallets unchanged
     * - SpecificWallet: Returns only the wallet matching the filter's walletId
     *
     * If the specified wallet is not found in the list, returns an empty list.
     *
     * @param wallets The list of wallets to filter
     * @param filter The wallet filter to apply
     * @return Filtered list of wallets
     */
    fun filterWalletsByWalletFilter(
        wallets: List<Wallet>,
        filter: WalletFilter
    ): List<Wallet> {
        return when (filter) {
            is WalletFilter.AllWallets -> wallets
            is WalletFilter.SpecificWallet -> {
                wallets.filter { wallet ->
                    wallet.id == filter.walletId
                }
            }
        }
    }
}

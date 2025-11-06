package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.WalletFilter
import java.util.*
import kotlin.math.abs

/**
 * Utility class for reconstructing historical wallet balances by replaying transactions.
 *
 * This class provides functions to reconstruct wallet balances as they were at a specific
 * point in time by chronologically replaying all transactions up to that date.
 *
 * ## Key Features:
 * - All wallets start at balance 0.0
 * - Transactions are replayed in chronological order by transactionDate
 * - Transactions with null transactionDate are excluded
 * - Wallets with zero balance are excluded from results
 * - For AllTime (endDate = null), returns current wallet balances
 * - **Supports wallet filtering** to reconstruct balances for specific wallets only
 *
 * ## Wallet Filter Integration:
 * All reconstruction functions support `WalletFilter` parameter for filtering:
 * - **AllWallets** (default): Reconstructs balances for all wallets
 * - **SpecificWallet**: Reconstructs balance only for the specified wallet
 *
 * This enables the Reports screen to show historical balances filtered by wallet,
 * combined with date filtering for precise portfolio analysis.
 *
 * ## Usage Example:
 * ```kotlin
 * // Reconstruct all wallets at specific date
 * val allBalances = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
 *     wallets = allWallets,
 *     transactions = allTransactions,
 *     endDate = Date(2025, 10, 31),
 *     walletFilter = WalletFilter.AllWallets
 * )
 *
 * // Reconstruct only specific wallet
 * val cashWalletBalance = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
 *     wallets = allWallets,
 *     transactions = allTransactions,
 *     endDate = Date(2025, 10, 31),
 *     walletFilter = WalletFilter.SpecificWallet("w1", "Cash Wallet")
 * )
 * ```
 *
 * @see WalletFilter for filter types
 * @see WalletFilterUtils for transaction filtering utilities
 * @see DateFilterPeriod for date filtering options
 */
object BalanceReconstructionUtils {

    /**
     * Reconstructs wallet balances as of a specific date by replaying transactions.
     *
     * This is a CUMULATIVE reconstruction - it replays ALL transactions up to the endDate
     * to show balances as they were at that point in time.
     *
     * Algorithm:
     * 1. Filter wallets by wallet filter (AllWallets or SpecificWallet)
     * 2. If endDate is null (All Time), return current wallet balances for filtered wallets
     * 3. Filter transactions with transactionDate <= endDate (exclude null dates)
     * 4. Filter transactions by wallet filter (only transactions affecting filtered wallets)
     * 5. Sort transactions chronologically by transactionDate
     * 6. Initialize filtered wallet balances to initial balance
     * 7. Replay each transaction:
     *    - INCOME: Add to affected wallets
     *    - EXPENSE: Subtract from affected wallets
     *    - TRANSFER: Subtract from source, add to destination
     * 8. Return only wallets with non-zero balances
     *
     * @param wallets List of all wallets
     * @param transactions List of all transactions
     * @param endDate The date to reconstruct balances for, or null for current balances
     * @param walletFilter The wallet filter to apply (default: AllWallets)
     * @return Map of walletId to reconstructed balance (excluding zero balances)
     */
    fun reconstructWalletBalancesAtDate(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        endDate: Date?,
        walletFilter: WalletFilter = WalletFilter.AllWallets
    ): Map<String, Double> {
        // Filter wallets by wallet filter
        val filteredWallets = WalletFilterUtils.filterWalletsByWalletFilter(wallets, walletFilter)
        val filteredWalletIds = filteredWallets.map { it.id }.toSet()

        // If endDate is null (All Time), return current wallet balances for filtered wallets
        if (endDate == null) {
            return filteredWallets.associate { it.id to it.balance }
        }

        // Filter transactions up to endDate (CUMULATIVE, not range-based)
        // Exclude transactions with null transactionDate
        // Filter transactions to only include those affecting the filtered wallets
        val relevantTransactions = transactions.filter { transaction ->
            transaction.transactionDate != null &&
            transaction.transactionDate <= endDate &&
            (
                // Include if any affected wallet is in the filtered set
                transaction.affectedWalletIds.any { it in filteredWalletIds } ||
                // Include transfers where source or destination is in the filtered set
                transaction.sourceWalletId in filteredWalletIds ||
                transaction.destinationWalletId in filteredWalletIds
            )
        }.sortedBy { it.transactionDate }

        // Initialize wallet balances with their initial balances
        // Only include filtered wallets that existed at or before the endDate
        val reconstructedBalances = mutableMapOf<String, Double>()
        filteredWallets.forEach { wallet ->
            // Only include wallet if it was created before or at the endDate
            if (wallet.createdAt == null || wallet.createdAt <= endDate) {
                reconstructedBalances[wallet.id] = wallet.initialBalance
            }
        }

        // Replay transactions chronologically
        relevantTransactions.forEach { transaction ->
            // Use absolute value to handle negative amounts in database
            val amount = abs(transaction.amount)

            when (transaction.type) {
                TransactionType.INCOME -> {
                    // Add to affected wallets (only if in filtered set)
                    transaction.affectedWalletIds.forEach { walletId ->
                        if (walletId in filteredWalletIds) {
                            reconstructedBalances[walletId] =
                                (reconstructedBalances[walletId] ?: 0.0) + amount
                        }
                    }
                }
                TransactionType.EXPENSE -> {
                    // Subtract from affected wallets (only if in filtered set)
                    transaction.affectedWalletIds.forEach { walletId ->
                        if (walletId in filteredWalletIds) {
                            reconstructedBalances[walletId] =
                                (reconstructedBalances[walletId] ?: 0.0) - amount
                        }
                    }
                }
                TransactionType.TRANSFER -> {
                    // Subtract from source (only if in filtered set)
                    if (transaction.sourceWalletId in filteredWalletIds) {
                        reconstructedBalances[transaction.sourceWalletId] =
                            (reconstructedBalances[transaction.sourceWalletId] ?: 0.0) - amount
                    }
                    // Add to destination (only if in filtered set)
                    if (transaction.destinationWalletId in filteredWalletIds) {
                        reconstructedBalances[transaction.destinationWalletId] =
                            (reconstructedBalances[transaction.destinationWalletId] ?: 0.0) + amount
                    }
                }
            }
        }

        // Return only wallets with non-zero balances
        return reconstructedBalances.filter { it.value > 0.0 }
    }

    /**
     * Reconstructs portfolio composition (balance by PhysicalForm) as of a specific date.
     *
     * This function:
     * - Reconstructs wallet balances cumulatively up to endDate
     * - Groups balances by PhysicalForm
     * - Only includes physical wallets
     * - Excludes zero and negative balances
     *
     * @param wallets List of all wallets
     * @param transactions List of all transactions
     * @param endDate The date to reconstruct balances for, or null for current balances
     * @param walletFilter The wallet filter to apply (default: AllWallets)
     * @return Map of PhysicalForm to total balance (excluding zero/negative balances)
     */
    fun reconstructPortfolioComposition(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        endDate: Date?,
        walletFilter: WalletFilter = WalletFilter.AllWallets
    ): Map<PhysicalForm, Double> {
        // Get reconstructed balances
        val walletBalances = reconstructWalletBalancesAtDate(wallets, transactions, endDate, walletFilter)

        // Create a map of walletId to wallet for quick lookup
        val walletMap = wallets.associateBy { it.id }

        // Group by PhysicalForm and sum balances
        val composition = mutableMapOf<PhysicalForm, Double>()

        walletBalances.forEach { (walletId, balance) ->
            val wallet = walletMap[walletId]
            // Only include physical wallets with positive balances
            if (wallet != null && wallet.isPhysical && balance > 0.0) {
                composition[wallet.physicalForm] =
                    (composition[wallet.physicalForm] ?: 0.0) + balance
            }
        }

        return composition
    }

    /**
     * Reconstructs physical wallet balances (name to balance) as of a specific date.
     *
     * This function:
     * - Reconstructs wallet balances cumulatively up to endDate
     * - Maps wallet names to balances
     * - Only includes physical wallets
     * - Excludes zero balances
     *
     * @param wallets List of all wallets
     * @param transactions List of all transactions
     * @param endDate The date to reconstruct balances for, or null for current balances
     * @param walletFilter The wallet filter to apply (default: AllWallets)
     * @return Map of wallet name to balance (excluding zero balances)
     */
    fun reconstructPhysicalWalletBalances(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        endDate: Date?,
        walletFilter: WalletFilter = WalletFilter.AllWallets
    ): Map<String, Double> {
        // Get reconstructed balances
        val walletBalances = reconstructWalletBalancesAtDate(wallets, transactions, endDate, walletFilter)

        // Create a map of walletId to wallet for quick lookup
        val walletMap = wallets.associateBy { it.id }

        // Map wallet names to balances (only physical wallets)
        val physicalBalances = mutableMapOf<String, Double>()

        walletBalances.forEach { (walletId, balance) ->
            val wallet = walletMap[walletId]
            // Only include physical wallets with positive balances
            if (wallet != null && wallet.isPhysical && balance > 0.0) {
                physicalBalances[wallet.name] = balance
            }
        }

        return physicalBalances
    }

    /**
     * Reconstructs logical wallet balances (name to balance) as of a specific date.
     *
     * This function:
     * - Reconstructs wallet balances cumulatively up to endDate
     * - Maps wallet names to balances
     * - Only includes logical wallets
     * - Includes negative balances (budget overspending)
     * - Excludes zero balances
     *
     * @param wallets List of all wallets
     * @param transactions List of all transactions
     * @param endDate The date to reconstruct balances for, or null for current balances
     * @param walletFilter The wallet filter to apply (default: AllWallets)
     * @return Map of wallet name to balance (including negative balances, excluding zero)
     */
    fun reconstructLogicalWalletBalances(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        endDate: Date?,
        walletFilter: WalletFilter = WalletFilter.AllWallets
    ): Map<String, Double> {
        // Filter wallets by wallet filter
        val filteredWallets = WalletFilterUtils.filterWalletsByWalletFilter(wallets, walletFilter)
        val filteredWalletIds = filteredWallets.map { it.id }.toSet()

        // Get reconstructed balances (with zero filtering for positive balances)
        val walletBalances = if (endDate == null) {
            filteredWallets.associate { it.id to it.balance }
        } else {
            val relevantTransactions = transactions.filter { transaction ->
                transaction.transactionDate != null &&
                transaction.transactionDate <= endDate &&
                (
                    // Include if any affected wallet is in the filtered set
                    transaction.affectedWalletIds.any { it in filteredWalletIds } ||
                    // Include transfers where source or destination is in the filtered set
                    transaction.sourceWalletId in filteredWalletIds ||
                    transaction.destinationWalletId in filteredWalletIds
                )
            }.sortedBy { it.transactionDate }

            val reconstructedBalances = mutableMapOf<String, Double>()
            filteredWallets.forEach { wallet ->
                // Only include wallet if it was created before or at the endDate
                if (wallet.createdAt == null || wallet.createdAt <= endDate) {
                    reconstructedBalances[wallet.id] = wallet.initialBalance
                }
            }

            relevantTransactions.forEach { transaction ->
                // Use absolute value to handle negative amounts in database
                val amount = abs(transaction.amount)

                when (transaction.type) {
                    TransactionType.INCOME -> {
                        transaction.affectedWalletIds.forEach { walletId ->
                            if (walletId in filteredWalletIds) {
                                reconstructedBalances[walletId] =
                                    (reconstructedBalances[walletId] ?: 0.0) + amount
                            }
                        }
                    }
                    TransactionType.EXPENSE -> {
                        transaction.affectedWalletIds.forEach { walletId ->
                            if (walletId in filteredWalletIds) {
                                reconstructedBalances[walletId] =
                                    (reconstructedBalances[walletId] ?: 0.0) - amount
                            }
                        }
                    }
                    TransactionType.TRANSFER -> {
                        if (transaction.sourceWalletId in filteredWalletIds) {
                            reconstructedBalances[transaction.sourceWalletId] =
                                (reconstructedBalances[transaction.sourceWalletId] ?: 0.0) - amount
                        }
                        if (transaction.destinationWalletId in filteredWalletIds) {
                            reconstructedBalances[transaction.destinationWalletId] =
                                (reconstructedBalances[transaction.destinationWalletId] ?: 0.0) + amount
                        }
                    }
                }
            }

            // For logical wallets, include both positive and negative balances
            reconstructedBalances.filter { it.value != 0.0 }
        }

        // Create a map of walletId to wallet for quick lookup
        val walletMap = wallets.associateBy { it.id }

        // Map wallet names to balances (only logical wallets)
        val logicalBalances = mutableMapOf<String, Double>()

        walletBalances.forEach { (walletId, balance) ->
            val wallet = walletMap[walletId]
            // Only include logical wallets with non-zero balances
            if (wallet != null && wallet.isLogical && balance != 0.0) {
                logicalBalances[wallet.name] = balance
            }
        }

        return logicalBalances
    }
}

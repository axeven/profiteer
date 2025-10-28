package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import java.util.*
import kotlin.math.abs

/**
 * Utility class for reconstructing historical wallet balances by replaying transactions.
 *
 * This class provides functions to reconstruct wallet balances as they were at a specific
 * point in time by chronologically replaying all transactions up to that date.
 *
 * Key features:
 * - All wallets start at balance 0.0
 * - Transactions are replayed in chronological order by transactionDate
 * - Transactions with null transactionDate are excluded
 * - Wallets with zero balance are excluded from results
 * - For AllTime (endDate = null), returns current wallet balances
 */
object BalanceReconstructionUtils {

    /**
     * Reconstructs wallet balances as of a specific date by replaying transactions.
     *
     * This is a CUMULATIVE reconstruction - it replays ALL transactions up to the endDate
     * to show balances as they were at that point in time.
     *
     * Algorithm:
     * 1. If endDate is null (All Time), return current wallet balances
     * 2. Filter transactions with transactionDate <= endDate (exclude null dates)
     * 3. Sort transactions chronologically by transactionDate
     * 4. Initialize all wallet balances to 0.0
     * 5. Replay each transaction:
     *    - INCOME: Add to affected wallets
     *    - EXPENSE: Subtract from affected wallets
     *    - TRANSFER: Subtract from source, add to destination
     * 6. Return only wallets with non-zero balances
     *
     * @param wallets List of all wallets
     * @param transactions List of all transactions
     * @param endDate The date to reconstruct balances for, or null for current balances
     * @return Map of walletId to reconstructed balance (excluding zero balances)
     */
    fun reconstructWalletBalancesAtDate(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        endDate: Date?
    ): Map<String, Double> {
        // If endDate is null (All Time), return current wallet balances
        if (endDate == null) {
            return wallets.associate { it.id to it.balance }
        }

        // Filter transactions up to endDate (CUMULATIVE, not range-based)
        // Exclude transactions with null transactionDate
        val relevantTransactions = transactions.filter { transaction ->
            transaction.transactionDate != null &&
            transaction.transactionDate <= endDate
        }.sortedBy { it.transactionDate }

        // Initialize wallet balances with their initial balances
        // Only include wallets that existed at or before the endDate
        val reconstructedBalances = mutableMapOf<String, Double>()
        wallets.forEach { wallet ->
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
                    // Add to affected wallets
                    transaction.affectedWalletIds.forEach { walletId ->
                        reconstructedBalances[walletId] =
                            (reconstructedBalances[walletId] ?: 0.0) + amount
                    }
                }
                TransactionType.EXPENSE -> {
                    // Subtract from affected wallets
                    transaction.affectedWalletIds.forEach { walletId ->
                        reconstructedBalances[walletId] =
                            (reconstructedBalances[walletId] ?: 0.0) - amount
                    }
                }
                TransactionType.TRANSFER -> {
                    // Subtract from source, add to destination
                    reconstructedBalances[transaction.sourceWalletId] =
                        (reconstructedBalances[transaction.sourceWalletId] ?: 0.0) - amount
                    reconstructedBalances[transaction.destinationWalletId] =
                        (reconstructedBalances[transaction.destinationWalletId] ?: 0.0) + amount
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
     * @return Map of PhysicalForm to total balance (excluding zero/negative balances)
     */
    fun reconstructPortfolioComposition(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        endDate: Date?
    ): Map<PhysicalForm, Double> {
        // Get reconstructed balances
        val walletBalances = reconstructWalletBalancesAtDate(wallets, transactions, endDate)

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
     * @return Map of wallet name to balance (excluding zero balances)
     */
    fun reconstructPhysicalWalletBalances(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        endDate: Date?
    ): Map<String, Double> {
        // Get reconstructed balances
        val walletBalances = reconstructWalletBalancesAtDate(wallets, transactions, endDate)

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
     * @return Map of wallet name to balance (including negative balances, excluding zero)
     */
    fun reconstructLogicalWalletBalances(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        endDate: Date?
    ): Map<String, Double> {
        // Get reconstructed balances (with zero filtering for positive balances)
        val walletBalances = if (endDate == null) {
            wallets.associate { it.id to it.balance }
        } else {
            val relevantTransactions = transactions.filter { transaction ->
                transaction.transactionDate != null &&
                transaction.transactionDate <= endDate
            }.sortedBy { it.transactionDate }

            val reconstructedBalances = mutableMapOf<String, Double>()
            wallets.forEach { wallet ->
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
                            reconstructedBalances[walletId] =
                                (reconstructedBalances[walletId] ?: 0.0) + amount
                        }
                    }
                    TransactionType.EXPENSE -> {
                        transaction.affectedWalletIds.forEach { walletId ->
                            reconstructedBalances[walletId] =
                                (reconstructedBalances[walletId] ?: 0.0) - amount
                        }
                    }
                    TransactionType.TRANSFER -> {
                        reconstructedBalances[transaction.sourceWalletId] =
                            (reconstructedBalances[transaction.sourceWalletId] ?: 0.0) - amount
                        reconstructedBalances[transaction.destinationWalletId] =
                            (reconstructedBalances[transaction.destinationWalletId] ?: 0.0) + amount
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

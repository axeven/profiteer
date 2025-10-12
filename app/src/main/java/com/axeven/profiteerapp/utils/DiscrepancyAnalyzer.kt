package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analyzes transactions to identify balance discrepancies and calculate running balances.
 *
 * This analyzer works with the BalanceDiscrepancyDetector to find the exact point
 * where Physical and Logical wallet totals diverge.
 */
@Singleton
class DiscrepancyAnalyzer @Inject constructor(
    private val balanceDetector: BalanceDiscrepancyDetector
) {

    /**
     * Finds the ID of the first transaction that caused a balance discrepancy.
     *
     * Processes transactions in chronological order (oldest first) and identifies
     * the first point where Physical and Logical wallet totals no longer match.
     *
     * @param transactions List of all transactions (will be sorted chronologically)
     * @param wallets Map of wallet ID to Wallet object (all wallets, Physical and Logical)
     * @return Transaction ID of first discrepancy, or null if no discrepancy exists
     */
    fun findFirstDiscrepancyTransaction(
        transactions: List<Transaction>,
        wallets: Map<String, Wallet>
    ): String? {
        if (transactions.isEmpty()) return null

        // Sort transactions chronologically (oldest first)
        val sortedTransactions = transactions.sortedBy { it.transactionDate ?: it.createdAt }

        // Track running balances for each wallet
        val runningBalances = wallets.mapValues { it.value.initialBalance }.toMutableMap()

        // Process each transaction and check for discrepancy
        for (transaction in sortedTransactions) {
            // Apply transaction to affected wallets
            applyTransactionToBalances(transaction, runningBalances)

            // Calculate totals for Physical and Logical wallets
            val physicalTotal = calculatePhysicalTotal(runningBalances, wallets)
            val logicalTotal = calculateLogicalTotal(runningBalances, wallets)

            // Check if this transaction created a discrepancy
            if (balanceDetector.hasDiscrepancy(physicalTotal, logicalTotal)) {
                return transaction.id
            }
        }

        return null
    }

    /**
     * Calculates running balances for each transaction in descending order.
     *
     * Shows cumulative Physical and Logical wallet totals after each transaction,
     * and marks the first transaction where a discrepancy occurred.
     *
     * @param transactions List of all transactions (will be sorted chronologically for calculation, returned in descending order)
     * @param wallets Map of wallet ID to Wallet object
     * @return List of TransactionWithBalances in descending order (newest first)
     */
    fun calculateRunningBalances(
        transactions: List<Transaction>,
        wallets: Map<String, Wallet>
    ): List<TransactionWithBalances> {
        if (transactions.isEmpty()) return emptyList()

        // Sort transactions chronologically (oldest first) for calculation
        val sortedTransactions = transactions.sortedBy { it.transactionDate ?: it.createdAt }

        // Track running balances for each wallet
        val runningBalances = wallets.mapValues { it.value.initialBalance }.toMutableMap()

        // Track if we've found the first discrepancy
        var firstDiscrepancyId: String? = null

        // Calculate running balances
        val results = sortedTransactions.map { transaction ->
            // Apply transaction to affected wallets
            applyTransactionToBalances(transaction, runningBalances)

            // Calculate totals for Physical and Logical wallets
            val physicalTotal = calculatePhysicalTotal(runningBalances, wallets)
            val logicalTotal = calculateLogicalTotal(runningBalances, wallets)

            // Check if this is the first discrepancy
            val isFirstDiscrepancy = if (firstDiscrepancyId == null &&
                balanceDetector.hasDiscrepancy(physicalTotal, logicalTotal)
            ) {
                firstDiscrepancyId = transaction.id
                true
            } else {
                false
            }

            TransactionWithBalances(
                transaction = transaction,
                physicalBalanceAfter = physicalTotal,
                logicalBalanceAfter = logicalTotal,
                isFirstDiscrepancy = isFirstDiscrepancy
            )
        }

        // Return in descending order (newest first) for UI display
        return results.reversed()
    }

    /**
     * Applies a transaction's effects to the running balance map.
     *
     * Handles INCOME, EXPENSE, and TRANSFER transaction types.
     */
    private fun applyTransactionToBalances(
        transaction: Transaction,
        runningBalances: MutableMap<String, Double>
    ) {
        when (transaction.type) {
            TransactionType.INCOME -> {
                // Add amount to all affected wallets
                transaction.affectedWalletIds.forEach { walletId ->
                    val current = runningBalances[walletId] ?: 0.0
                    runningBalances[walletId] = current + transaction.amount
                }
            }
            TransactionType.EXPENSE -> {
                // Subtract amount from all affected wallets
                transaction.affectedWalletIds.forEach { walletId ->
                    val current = runningBalances[walletId] ?: 0.0
                    runningBalances[walletId] = current - transaction.amount
                }
            }
            TransactionType.TRANSFER -> {
                // Subtract from source, add to destination
                if (transaction.sourceWalletId.isNotEmpty()) {
                    val current = runningBalances[transaction.sourceWalletId] ?: 0.0
                    runningBalances[transaction.sourceWalletId] = current - transaction.amount
                }
                if (transaction.destinationWalletId.isNotEmpty()) {
                    val current = runningBalances[transaction.destinationWalletId] ?: 0.0
                    runningBalances[transaction.destinationWalletId] = current + transaction.amount
                }
            }
        }
    }

    /**
     * Calculates total balance across all Physical wallets from running balances.
     */
    private fun calculatePhysicalTotal(
        runningBalances: Map<String, Double>,
        wallets: Map<String, Wallet>
    ): Double {
        return wallets
            .filter { it.value.walletType == "Physical" }
            .mapNotNull { runningBalances[it.key] }
            .sum()
    }

    /**
     * Calculates total balance across all Logical wallets from running balances.
     */
    private fun calculateLogicalTotal(
        runningBalances: Map<String, Double>,
        wallets: Map<String, Wallet>
    ): Double {
        return wallets
            .filter { it.value.walletType == "Logical" }
            .mapNotNull { runningBalances[it.key] }
            .sum()
    }
}

/**
 * Data class representing a transaction with its running balance state.
 *
 * @property transaction The transaction itself
 * @property physicalBalanceAfter Total Physical wallet balance after this transaction
 * @property logicalBalanceAfter Total Logical wallet balance after this transaction
 * @property isFirstDiscrepancy True if this is the first transaction causing discrepancy
 */
data class TransactionWithBalances(
    val transaction: Transaction,
    val physicalBalanceAfter: Double,
    val logicalBalanceAfter: Double,
    val isFirstDiscrepancy: Boolean
)

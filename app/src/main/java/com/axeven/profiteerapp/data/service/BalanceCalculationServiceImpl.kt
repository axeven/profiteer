package com.axeven.profiteerapp.data.service

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.utils.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Implementation of BalanceCalculationService that provides transaction-based balance calculations.
 *
 * This service handles the complex logic of calculating balances, including:
 * - Transfer direction detection
 * - Income vs expense classification
 * - Msulti-wallet transfer handling
 * - Period-based summaries
 */
@Singleton
class BalanceCalculationServiceImpl @Inject constructor(
    private val logger: Logger
) : BalanceCalculationService {

    override fun calculateTotalBalance(
        transactions: List<Transaction>,
        walletId: String,
        initialBalance: Double
    ): Double {
        logger.d("BalanceCalculationService", "Calculating total balance for wallet: $walletId")

        val netBalance = calculateNetBalance(transactions, walletId)
        val totalBalance = initialBalance + netBalance

        logger.d("BalanceCalculationService", "Balance calculation: initial=$initialBalance, net=$netBalance, total=$totalBalance")

        return totalBalance
    }

    override fun calculateNetBalance(transactions: List<Transaction>, walletId: String): Double {
        val income = calculateIncome(transactions, walletId)
        val expenses = calculateExpenses(transactions, walletId)
        return income - expenses
    }

    override fun calculateIncome(transactions: List<Transaction>, walletId: String): Double {
        return transactions
            .filter { isIncomeTransaction(it, walletId) }
            .sumOf { getPositiveAmount(it) }
    }

    override fun calculateExpenses(transactions: List<Transaction>, walletId: String): Double {
        return transactions
            .filter { isExpenseTransaction(it, walletId) }
            .sumOf { getPositiveAmount(it) }
    }

    override fun getTransferDirection(transaction: Transaction, walletId: String): TransferDirection? {
        return when {
            transaction.type != TransactionType.TRANSFER -> null
            transaction.sourceWalletId == walletId -> TransferDirection.OUTGOING
            transaction.destinationWalletId == walletId -> TransferDirection.INCOMING
            else -> null
        }
    }

    override fun getEffectiveAmount(transaction: Transaction, walletId: String): Double {
        return when {
            transaction.type == TransactionType.INCOME -> getPositiveAmount(transaction)
            transaction.type == TransactionType.EXPENSE -> -getPositiveAmount(transaction)
            transaction.type == TransactionType.TRANSFER -> {
                when (getTransferDirection(transaction, walletId)) {
                    TransferDirection.INCOMING -> getPositiveAmount(transaction)
                    TransferDirection.OUTGOING -> -getPositiveAmount(transaction)
                    null -> 0.0
                }
            }
            else -> 0.0
        }
    }

    override fun isTransferIncome(transaction: Transaction, walletId: String): Boolean {
        return transaction.type == TransactionType.TRANSFER &&
               transaction.destinationWalletId == walletId
    }

    override fun isTransferExpense(transaction: Transaction, walletId: String): Boolean {
        return transaction.type == TransactionType.TRANSFER &&
               transaction.sourceWalletId == walletId
    }

    override fun calculatePeriodSummary(transactions: List<Transaction>, walletId: String): PeriodSummary {
        val income = calculateIncome(transactions, walletId)
        val expenses = calculateExpenses(transactions, walletId)
        val netChange = income - expenses

        // Calculate transfer-specific amounts
        val transfersIn = transactions
            .filter { isTransferIncome(it, walletId) }
            .sumOf { getPositiveAmount(it) }

        val transfersOut = transactions
            .filter { isTransferExpense(it, walletId) }
            .sumOf { getPositiveAmount(it) }

        // Count different transaction types
        val incomeTransactionCount = transactions.count {
            it.type == TransactionType.INCOME
        }

        val expenseTransactionCount = transactions.count {
            it.type == TransactionType.EXPENSE
        }

        val incomingTransferCount = transactions.count {
            isTransferIncome(it, walletId)
        }

        val outgoingTransferCount = transactions.count {
            isTransferExpense(it, walletId)
        }

        return PeriodSummary(
            income = income,
            expenses = expenses,
            netChange = netChange,
            transactionCount = transactions.size,
            transfersIn = transfersIn,
            transfersOut = transfersOut,
            incomeTransactionCount = incomeTransactionCount,
            expenseTransactionCount = expenseTransactionCount,
            incomingTransferCount = incomingTransferCount,
            outgoingTransferCount = outgoingTransferCount
        )
    }

    override fun calculateDailySummary(transactions: List<Transaction>, walletId: String): DailySummary {
        val count = transactions.size
        val netAmount = transactions.sumOf { getEffectiveAmount(it, walletId) }

        return DailySummary(
            transactionCount = count,
            netAmount = netAmount
        )
    }

    // Private helper methods

    /**
     * Determines if a transaction represents income for the specified wallet.
     * Income includes:
     * - INCOME transactions belonging to this wallet
     * - TRANSFER transactions where this wallet is the destination
     */
    private fun isIncomeTransaction(transaction: Transaction, walletId: String): Boolean {
        return when (transaction.type) {
            TransactionType.INCOME -> transaction.walletId == walletId
            TransactionType.TRANSFER -> isTransferIncome(transaction, walletId)
            else -> false
        }
    }

    /**
     * Determines if a transaction represents an expense for the specified wallet.
     * Expenses include:
     * - EXPENSE transactions belonging to this wallet
     * - TRANSFER transactions where this wallet is the source
     */
    private fun isExpenseTransaction(transaction: Transaction, walletId: String): Boolean {
        return when (transaction.type) {
            TransactionType.EXPENSE -> transaction.walletId == walletId
            TransactionType.TRANSFER -> isTransferExpense(transaction, walletId)
            else -> false
        }
    }

    /**
     * Gets the absolute value of a transaction amount.
     * Transaction amounts should always be positive in storage, but this ensures consistency.
     */
    private fun getPositiveAmount(transaction: Transaction): Double {
        return abs(transaction.amount)
    }
}
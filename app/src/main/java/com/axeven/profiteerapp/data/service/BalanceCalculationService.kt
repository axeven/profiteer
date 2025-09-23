package com.axeven.profiteerapp.data.service

import com.axeven.profiteerapp.data.model.Transaction

/**
 * Service for performing balance calculations on transactions.
 *
 * This service provides reusable methods for calculating balances, income, expenses,
 * and other financial metrics based on transaction data. It handles the complex logic
 * of transfer transactions and wallet-specific calculations.
 */
interface BalanceCalculationService {

    /**
     * Calculates the total balance for a wallet including the initial balance.
     * Formula: initialBalance + netBalance
     *
     * @param transactions List of transactions to include in calculation
     * @param walletId ID of the wallet to calculate balance for
     * @param initialBalance Initial balance of the wallet
     * @return Total balance including initial balance
     */
    fun calculateTotalBalance(
        transactions: List<Transaction>,
        walletId: String,
        initialBalance: Double
    ): Double

    /**
     * Calculates the net balance change from transactions only (excluding initial balance).
     * Formula: income - expenses
     *
     * @param transactions List of transactions to include in calculation
     * @param walletId ID of the wallet to calculate net balance for
     * @return Net balance change from transactions
     */
    fun calculateNetBalance(transactions: List<Transaction>, walletId: String): Double

    /**
     * Calculates total income for a wallet.
     * Includes INCOME transactions and incoming transfers (where walletId is destination).
     *
     * @param transactions List of transactions to include in calculation
     * @param walletId ID of the wallet to calculate income for
     * @return Total income amount
     */
    fun calculateIncome(transactions: List<Transaction>, walletId: String): Double

    /**
     * Calculates total expenses for a wallet.
     * Includes EXPENSE transactions and outgoing transfers (where walletId is source).
     *
     * @param transactions List of transactions to include in calculation
     * @param walletId ID of the wallet to calculate expenses for
     * @return Total expense amount (always positive)
     */
    fun calculateExpenses(transactions: List<Transaction>, walletId: String): Double

    /**
     * Determines the transfer direction for a transaction relative to a specific wallet.
     *
     * @param transaction The transfer transaction to analyze
     * @param walletId ID of the wallet to determine direction for
     * @return Transfer direction or null if not a transfer or wallet not involved
     */
    fun getTransferDirection(transaction: Transaction, walletId: String): TransferDirection?

    /**
     * Gets the effective amount of a transaction for a specific wallet.
     * For transfers: positive if incoming, negative if outgoing
     * For other transactions: returns the original amount with appropriate sign
     *
     * @param transaction Transaction to get effective amount for
     * @param walletId ID of the wallet
     * @return Effective amount (positive for income, negative for expense)
     */
    fun getEffectiveAmount(transaction: Transaction, walletId: String): Double

    /**
     * Checks if a transaction represents income for a specific wallet.
     *
     * @param transaction Transaction to check
     * @param walletId ID of the wallet
     * @return True if transaction increases wallet balance
     */
    fun isTransferIncome(transaction: Transaction, walletId: String): Boolean

    /**
     * Checks if a transaction represents an expense for a specific wallet.
     *
     * @param transaction Transaction to check
     * @param walletId ID of the wallet
     * @return True if transaction decreases wallet balance
     */
    fun isTransferExpense(transaction: Transaction, walletId: String): Boolean

    /**
     * Calculates a comprehensive summary for a period of transactions.
     *
     * @param transactions List of transactions for the period
     * @param walletId ID of the wallet
     * @return Detailed period summary
     */
    fun calculatePeriodSummary(transactions: List<Transaction>, walletId: String): PeriodSummary

    /**
     * Calculates a daily summary for a group of transactions.
     *
     * @param transactions List of transactions for the day
     * @param walletId ID of the wallet
     * @return Daily summary with count and net amount
     */
    fun calculateDailySummary(transactions: List<Transaction>, walletId: String): DailySummary
}

/**
 * Represents the direction of a transfer relative to a specific wallet.
 */
enum class TransferDirection {
    /** Transfer coming into the wallet (destination) */
    INCOMING,
    /** Transfer going out of the wallet (source) */
    OUTGOING
}

/**
 * Comprehensive summary of transactions for a specific period.
 */
data class PeriodSummary(
    /** Total income from INCOME transactions and incoming transfers */
    val income: Double,
    /** Total expenses from EXPENSE transactions and outgoing transfers */
    val expenses: Double,
    /** Net change (income - expenses) */
    val netChange: Double,
    /** Total number of transactions */
    val transactionCount: Int,
    /** Total amount from incoming transfers only */
    val transfersIn: Double,
    /** Total amount from outgoing transfers only */
    val transfersOut: Double,
    /** Number of income transactions (excluding transfers) */
    val incomeTransactionCount: Int,
    /** Number of expense transactions (excluding transfers) */
    val expenseTransactionCount: Int,
    /** Number of incoming transfers */
    val incomingTransferCount: Int,
    /** Number of outgoing transfers */
    val outgoingTransferCount: Int
)

/**
 * Simple summary for daily transaction grouping.
 */
data class DailySummary(
    /** Number of transactions on this day */
    val transactionCount: Int,
    /** Net amount for the day (positive = net income, negative = net expense) */
    val netAmount: Double
)
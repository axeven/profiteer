package com.axeven.profiteerapp.data.ui

import com.axeven.profiteerapp.utils.TransactionWithBalances
import kotlin.math.abs

/**
 * Consolidated state for DiscrepancyDebugScreen.
 *
 * This data class consolidates all UI state for the discrepancy debug screen,
 * following the consolidated state management pattern.
 *
 * @property isLoading Whether data is currently being loaded
 * @property transactions List of transactions with running balances (descending order, newest first)
 * @property firstDiscrepancyId ID of the first transaction that caused discrepancy
 * @property currentDiscrepancy Current discrepancy amount (Physical - Logical)
 * @property totalPhysicalBalance Total balance across all Physical wallets
 * @property totalLogicalBalance Total balance across all Logical wallets
 * @property error Error message if loading failed
 */
data class DiscrepancyDebugUiState(
    val isLoading: Boolean = true,
    val transactions: List<TransactionWithBalances> = emptyList(),
    val firstDiscrepancyId: String? = null,
    val currentDiscrepancy: Double = 0.0,
    val totalPhysicalBalance: Double = 0.0,
    val totalLogicalBalance: Double = 0.0,
    val defaultCurrency: String = "USD",
    val error: String? = null
) {

    /**
     * Whether a discrepancy exists (difference exceeds tolerance).
     * Uses 0.01 tolerance for floating-point precision.
     */
    val hasDiscrepancy: Boolean
        get() = abs(currentDiscrepancy) > 0.01

    /**
     * Whether the balances are balanced (no discrepancy).
     */
    val isBalanced: Boolean
        get() = !hasDiscrepancy

    /**
     * Update state with loaded transactions and balance data.
     * Sets isLoading to false and clears any error.
     *
     * @param transactions List of transactions with running balances
     * @param firstDiscrepancyId ID of first transaction causing discrepancy
     * @param physicalTotal Total Physical wallet balance
     * @param logicalTotal Total Logical wallet balance
     * @param defaultCurrency User's default currency for display
     * @return Updated state
     */
    fun withTransactions(
        transactions: List<TransactionWithBalances>,
        firstDiscrepancyId: String?,
        physicalTotal: Double,
        logicalTotal: Double,
        defaultCurrency: String = "USD"
    ): DiscrepancyDebugUiState {
        return copy(
            isLoading = false,
            transactions = transactions,
            firstDiscrepancyId = firstDiscrepancyId,
            currentDiscrepancy = physicalTotal - logicalTotal,
            totalPhysicalBalance = physicalTotal,
            totalLogicalBalance = logicalTotal,
            defaultCurrency = defaultCurrency,
            error = null
        )
    }

    /**
     * Update state with error.
     * Sets isLoading to false and stores error message.
     *
     * @param message Error message
     * @return Updated state
     */
    fun withError(message: String): DiscrepancyDebugUiState {
        return copy(
            isLoading = false,
            error = message
        )
    }
}

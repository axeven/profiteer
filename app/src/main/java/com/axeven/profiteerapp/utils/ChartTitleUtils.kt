package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.DateFilterPeriod
import com.axeven.profiteerapp.data.model.WalletFilter
import com.axeven.profiteerapp.viewmodel.ChartDataType

/**
 * Utility object for generating chart titles with filter information.
 *
 * Provides consistent title formatting across all chart types in the Reports screen,
 * combining chart-specific labels with date and wallet filter information.
 *
 * ## Filter Suffix Format:
 * - No filters: "Total Portfolio Value: $1,000.00"
 * - Date only: "Total Portfolio Value (October 2025): $1,000.00"
 * - Wallet only: "Total Portfolio Value (Cash Wallet): $1,000.00"
 * - Both: "Total Portfolio Value (October 2025, Cash Wallet): $1,000.00"
 *
 * ## Usage:
 * ```kotlin
 * val title = ChartTitleUtils.getChartTitle(
 *     chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
 *     dateFilter = DateFilterPeriod.Month(2025, 10),
 *     walletFilter = WalletFilter.SpecificWallet("id-1", "Cash Wallet"),
 *     totalValue = 1000.0,
 *     defaultCurrency = "USD"
 * )
 * // Returns: "Total Portfolio Value (October 2025, Cash Wallet): $1,000.00"
 * ```
 *
 * @see ChartDataType for available chart types
 * @see DateFilterPeriod for date filter options
 * @see WalletFilter for wallet filter options
 */
object ChartTitleUtils {

    /**
     * Generates a formatted chart title with filter information.
     *
     * @param chartType The type of chart being displayed
     * @param dateFilter The selected date filter period
     * @param walletFilter The selected wallet filter
     * @param totalValue The total value to display (sum of chart data)
     * @param defaultCurrency The currency code for formatting the value
     * @return Formatted title string with optional filter suffix and value
     *
     * @example
     * ```kotlin
     * // No filters
     * getChartTitle(
     *     ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
     *     DateFilterPeriod.AllTime,
     *     WalletFilter.AllWallets,
     *     1000.0,
     *     "USD"
     * )
     * // Returns: "Total Portfolio Value: $1,000.00"
     *
     * // With both filters
     * getChartTitle(
     *     ChartDataType.EXPENSE_TRANSACTION_BY_TAG,
     *     DateFilterPeriod.Month(2025, 10),
     *     WalletFilter.SpecificWallet("id", "Cash Wallet"),
     *     500.0,
     *     "USD"
     * )
     * // Returns: "Total Expense Amount (October 2025, Cash Wallet): $500.00"
     * ```
     */
    fun getChartTitle(
        chartType: ChartDataType,
        dateFilter: DateFilterPeriod,
        walletFilter: WalletFilter,
        totalValue: Double,
        defaultCurrency: String
    ): String {
        // Build filter suffix from active filters
        val filterSuffix = buildFilterSuffix(dateFilter, walletFilter)

        // Get base title for chart type
        val baseTitle = when (chartType) {
            ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> "Total Portfolio Value"
            ChartDataType.PHYSICAL_WALLET_BALANCE -> "Total Physical Wallet Value"
            ChartDataType.LOGICAL_WALLET_BALANCE -> "Total Logical Wallet Value"
            ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> "Total Expense Amount"
            ChartDataType.INCOME_TRANSACTION_BY_TAG -> "Total Income Amount"
        }

        // Format total value with currency
        val formattedValue = NumberFormatter.formatCurrency(
            amount = totalValue,
            currency = defaultCurrency,
            showSymbol = true
        )

        // Combine base title + filter suffix + value
        return "$baseTitle$filterSuffix: $formattedValue"
    }

    /**
     * Builds the filter suffix for chart titles.
     *
     * Combines date and wallet filter information into a parenthesized suffix string.
     *
     * @param dateFilter The selected date filter period
     * @param walletFilter The selected wallet filter
     * @return Filter suffix string, or empty string if no filters are active
     *
     * @example
     * ```kotlin
     * buildFilterSuffix(DateFilterPeriod.AllTime, WalletFilter.AllWallets)
     * // Returns: ""
     *
     * buildFilterSuffix(DateFilterPeriod.Month(2025, 10), WalletFilter.AllWallets)
     * // Returns: " (October 2025)"
     *
     * buildFilterSuffix(
     *     DateFilterPeriod.Month(2025, 10),
     *     WalletFilter.SpecificWallet("id", "Cash Wallet")
     * )
     * // Returns: " (October 2025, Cash Wallet)"
     * ```
     */
    private fun buildFilterSuffix(
        dateFilter: DateFilterPeriod,
        walletFilter: WalletFilter
    ): String {
        val filterParts = mutableListOf<String>()

        // Add date filter if not AllTime
        if (dateFilter != DateFilterPeriod.AllTime) {
            filterParts.add(dateFilter.getDisplayText())
        }

        // Add wallet filter if specific wallet selected
        if (walletFilter is WalletFilter.SpecificWallet) {
            filterParts.add(walletFilter.walletName)
        }

        // Return parenthesized suffix, or empty string if no filters
        return if (filterParts.isNotEmpty()) {
            " (${filterParts.joinToString(", ")})"
        } else {
            ""
        }
    }
}

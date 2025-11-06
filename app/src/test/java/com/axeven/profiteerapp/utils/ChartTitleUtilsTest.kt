package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.DateFilterPeriod
import com.axeven.profiteerapp.data.model.WalletFilter
import com.axeven.profiteerapp.viewmodel.ChartDataType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for ChartTitleUtils.
 *
 * Tests the chart title generation logic with various combinations of:
 * - Chart types (Portfolio, Physical Wallet, Logical Wallet, Expense, Income)
 * - Date filters (AllTime, Month, Year)
 * - Wallet filters (AllWallets, SpecificWallet)
 */
class ChartTitleUtilsTest {

    // Test Data
    private val defaultCurrency = "USD"
    private val totalValue = 1000.0
    private val specificWallet = WalletFilter.SpecificWallet("wallet-1", "Cash Wallet")
    private val monthFilter = DateFilterPeriod.Month(2025, 10)
    private val yearFilter = DateFilterPeriod.Year(2025)

    // ========================================
    // Portfolio Chart Tests
    // ========================================

    @Test
    fun `getChartTitle - portfolio with AllWallets and AllTime shows base title`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Portfolio Value: $1,000.00", title)
    }

    @Test
    fun `getChartTitle - portfolio with SpecificWallet shows wallet name in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = specificWallet,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Portfolio Value (Cash Wallet): $1,000.00", title)
    }

    @Test
    fun `getChartTitle - portfolio with date filter shows period in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
            dateFilter = monthFilter,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Portfolio Value (October 2025): $1,000.00", title)
    }

    @Test
    fun `getChartTitle - portfolio with date and wallet filters shows both in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
            dateFilter = monthFilter,
            walletFilter = specificWallet,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Portfolio Value (October 2025, Cash Wallet): $1,000.00", title)
    }

    // ========================================
    // Physical Wallet Chart Tests
    // ========================================

    @Test
    fun `getChartTitle - physical wallet with AllWallets and AllTime shows base title`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PHYSICAL_WALLET_BALANCE,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Physical Wallet Value: $1,000.00", title)
    }

    @Test
    fun `getChartTitle - physical wallet with year filter shows year in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PHYSICAL_WALLET_BALANCE,
            dateFilter = yearFilter,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Physical Wallet Value (2025): $1,000.00", title)
    }

    // ========================================
    // Logical Wallet Chart Tests
    // ========================================

    @Test
    fun `getChartTitle - logical wallet with AllWallets and AllTime shows base title`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.LOGICAL_WALLET_BALANCE,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Logical Wallet Value: $1,000.00", title)
    }

    @Test
    fun `getChartTitle - logical wallet with SpecificWallet shows wallet name in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.LOGICAL_WALLET_BALANCE,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = specificWallet,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Logical Wallet Value (Cash Wallet): $1,000.00", title)
    }

    // ========================================
    // Expense Transaction Chart Tests
    // ========================================

    @Test
    fun `getChartTitle - expense by tag with AllWallets and AllTime shows base title`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.EXPENSE_TRANSACTION_BY_TAG,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Expense Amount: $1,000.00", title)
    }

    @Test
    fun `getChartTitle - expense by tag with SpecificWallet shows wallet name in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.EXPENSE_TRANSACTION_BY_TAG,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = specificWallet,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Expense Amount (Cash Wallet): $1,000.00", title)
    }

    @Test
    fun `getChartTitle - expense by tag with date and wallet filters shows both in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.EXPENSE_TRANSACTION_BY_TAG,
            dateFilter = monthFilter,
            walletFilter = specificWallet,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Expense Amount (October 2025, Cash Wallet): $1,000.00", title)
    }

    // ========================================
    // Income Transaction Chart Tests
    // ========================================

    @Test
    fun `getChartTitle - income by tag with AllWallets and AllTime shows base title`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.INCOME_TRANSACTION_BY_TAG,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Income Amount: $1,000.00", title)
    }

    @Test
    fun `getChartTitle - income by tag with SpecificWallet shows wallet name in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.INCOME_TRANSACTION_BY_TAG,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = specificWallet,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Income Amount (Cash Wallet): $1,000.00", title)
    }

    @Test
    fun `getChartTitle - income by tag with date filter shows period in suffix`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.INCOME_TRANSACTION_BY_TAG,
            dateFilter = yearFilter,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Income Amount (2025): $1,000.00", title)
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    fun `getChartTitle - handles zero value`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = 0.0,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Portfolio Value: $0.00", title)
    }

    @Test
    fun `getChartTitle - handles negative value`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.LOGICAL_WALLET_BALANCE,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = -500.0,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Logical Wallet Value: $-500.00", title)
    }

    @Test
    fun `getChartTitle - handles large value`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = 1234567.89,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Portfolio Value: $1,234,567.89", title)
    }

    @Test
    fun `getChartTitle - handles different currency`() {
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = WalletFilter.AllWallets,
            totalValue = totalValue,
            defaultCurrency = "EUR"
        )

        assertEquals("Total Portfolio Value: €1,000.00", title)
    }

    @Test
    fun `getChartTitle - handles wallet with special characters in name`() {
        val specialWallet = WalletFilter.SpecificWallet("wallet-2", "John's Wallet (Special)")
        val title = ChartTitleUtils.getChartTitle(
            chartType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
            dateFilter = DateFilterPeriod.AllTime,
            walletFilter = specialWallet,
            totalValue = totalValue,
            defaultCurrency = defaultCurrency
        )

        assertEquals("Total Portfolio Value (John's Wallet (Special)): $1,000.00", title)
    }
}

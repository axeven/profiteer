package com.axeven.profiteerapp.viewmodel

import java.util.Date

data class MonthlyExpense(
    val month: String, // Format: "2024-01" for Jan 2024
    val monthDisplay: String, // Format: "Jan 2024"
    val totalAmount: Double,
    val transactionCount: Int
)

data class IncomeExpenseComparison(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netAmount: Double,
    val incomePercentage: Double,
    val expensePercentage: Double
)

data class TagExpenseData(
    val tag: String,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)

data class TransactionAnalyticsUiState(
    val monthlyExpenses: List<MonthlyExpense> = emptyList(),
    val monthlyIncome: List<MonthlyExpense> = emptyList(),
    val incomeExpenseComparison: IncomeExpenseComparison = IncomeExpenseComparison(0.0, 0.0, 0.0, 0.0, 0.0),
    val expensesByTag: List<TagExpenseData> = emptyList(),
    val defaultCurrency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDateRange: DateRange = DateRange.LAST_6_MONTHS,
    val customDateStart: Date? = null,
    val customDateEnd: Date? = null,
    val totalTransactions: Int = 0,
    val averageTransactionAmount: Double = 0.0
)

enum class DateRange(val displayName: String, val months: Int) {
    LAST_3_MONTHS("Last 3 Months", 3),
    LAST_6_MONTHS("Last 6 Months", 6),
    LAST_12_MONTHS("Last 12 Months", 12),
    CUSTOM("Custom Range", 0)
}
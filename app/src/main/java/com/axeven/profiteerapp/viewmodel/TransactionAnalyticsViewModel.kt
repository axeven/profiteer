package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.CurrencyRateRepository
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TransactionAnalyticsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRateRepository: CurrencyRateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionAnalyticsUiState())
    val uiState: StateFlow<TransactionAnalyticsUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""
    
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val monthDisplayFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    init {
        if (userId.isNotEmpty()) {
            loadAnalyticsData()
        }
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getUserTransactions(userId),
                userPreferencesRepository.getUserPreferences(userId)
            ) { transactions: List<Transaction>, userPreferences: com.axeven.profiteerapp.data.model.UserPreferences? ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val defaultCurrency = userPreferences?.defaultCurrency ?: "USD"
                val filteredTransactions = filterTransactionsByDateRange(transactions, _uiState.value.selectedDateRange)
                
                val monthlyExpenses = calculateMonthlyData(filteredTransactions, TransactionType.EXPENSE)
                val monthlyIncome = calculateMonthlyData(filteredTransactions, TransactionType.INCOME)
                val incomeExpenseComparison = calculateIncomeExpenseComparison(filteredTransactions)
                val expensesByTag = calculateExpensesByTag(filteredTransactions)
                val totalTransactions = filteredTransactions.size
                val averageTransactionAmount = if (filteredTransactions.isNotEmpty()) {
                    filteredTransactions.sumOf { kotlin.math.abs(it.amount) } / filteredTransactions.size
                } else 0.0
                
                _uiState.value = _uiState.value.copy(
                    monthlyExpenses = monthlyExpenses,
                    monthlyIncome = monthlyIncome,
                    incomeExpenseComparison = incomeExpenseComparison,
                    expensesByTag = expensesByTag,
                    defaultCurrency = defaultCurrency,
                    isLoading = false,
                    error = null,
                    totalTransactions = totalTransactions,
                    averageTransactionAmount = averageTransactionAmount
                )
            }.catch { error: Throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load analytics data: ${error.message}"
                )
            }.collect()
        }
    }

    fun updateDateRange(dateRange: DateRange) {
        _uiState.value = _uiState.value.copy(selectedDateRange = dateRange)
        loadAnalyticsData()
    }

    fun updateCustomDateRange(startDate: Date, endDate: Date) {
        _uiState.value = _uiState.value.copy(
            selectedDateRange = DateRange.CUSTOM,
            customDateStart = startDate,
            customDateEnd = endDate
        )
        loadAnalyticsData()
    }

    private fun filterTransactionsByDateRange(transactions: List<Transaction>, dateRange: DateRange): List<Transaction> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        val startDate = when (dateRange) {
            DateRange.LAST_3_MONTHS -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.time
            }
            DateRange.LAST_6_MONTHS -> {
                calendar.add(Calendar.MONTH, -6)
                calendar.time
            }
            DateRange.LAST_12_MONTHS -> {
                calendar.add(Calendar.MONTH, -12)
                calendar.time
            }
            DateRange.CUSTOM -> {
                _uiState.value.customDateStart ?: Date(0)
            }
        }
        
        val actualEndDate = if (dateRange == DateRange.CUSTOM) {
            _uiState.value.customDateEnd ?: endDate
        } else endDate

        return transactions.filter { transaction ->
            val transactionDate = transaction.transactionDate ?: transaction.createdAt
            transactionDate != null && 
            transactionDate >= startDate && 
            transactionDate <= actualEndDate
        }
    }

    private fun calculateMonthlyData(transactions: List<Transaction>, type: TransactionType): List<MonthlyExpense> {
        return transactions
            .filter { it.type == type }
            .groupBy { transaction ->
                val date = transaction.transactionDate ?: transaction.createdAt
                date?.let { monthFormat.format(it) } ?: ""
            }
            .filter { it.key.isNotEmpty() }
            .map { (monthKey, monthTransactions) ->
                val totalAmount = monthTransactions.sumOf { kotlin.math.abs(it.amount) }
                val displayMonth = try {
                    val date = monthFormat.parse(monthKey)
                    date?.let { monthDisplayFormat.format(it) } ?: monthKey
                } catch (e: Exception) {
                    monthKey
                }
                
                MonthlyExpense(
                    month = monthKey,
                    monthDisplay = displayMonth,
                    totalAmount = totalAmount,
                    transactionCount = monthTransactions.size
                )
            }
            .sortedBy { it.month }
    }

    private fun calculateIncomeExpenseComparison(transactions: List<Transaction>): IncomeExpenseComparison {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { kotlin.math.abs(it.amount) }
        val total = income + expenses
        
        return IncomeExpenseComparison(
            totalIncome = income,
            totalExpenses = expenses,
            netAmount = income - expenses,
            incomePercentage = if (total > 0) (income / total * 100) else 0.0,
            expensePercentage = if (total > 0) (expenses / total * 100) else 0.0
        )
    }

    private fun calculateExpensesByTag(transactions: List<Transaction>): List<TagExpenseData> {
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpenses = expenseTransactions.sumOf { kotlin.math.abs(it.amount) }
        
        val tagExpenses = mutableMapOf<String, Pair<Double, Int>>()
        
        expenseTransactions.forEach { transaction ->
            val tags = if (transaction.tags.isNotEmpty()) transaction.tags else listOf(transaction.category)
            
            tags.forEach { tag ->
                val amount = kotlin.math.abs(transaction.amount)
                val currentData = tagExpenses[tag] ?: Pair(0.0, 0)
                tagExpenses[tag] = Pair(currentData.first + amount, currentData.second + 1)
            }
        }
        
        return tagExpenses.map { (tag, data) ->
            TagExpenseData(
                tag = tag,
                amount = data.first,
                percentage = if (totalExpenses > 0) (data.first / totalExpenses * 100) else 0.0,
                transactionCount = data.second
            )
        }.sortedByDescending { it.amount }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
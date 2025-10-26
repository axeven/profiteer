package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.DateFilterPeriod
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import com.axeven.profiteerapp.utils.BalanceReconstructionUtils
import com.axeven.profiteerapp.utils.DateFilterUtils
import com.axeven.profiteerapp.utils.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class ChartDataType {
    PORTFOLIO_ASSET_COMPOSITION,
    PHYSICAL_WALLET_BALANCE,
    LOGICAL_WALLET_BALANCE,
    EXPENSE_TRANSACTION_BY_TAG,
    INCOME_TRANSACTION_BY_TAG
}

data class ReportUiState(
    val portfolioComposition: Map<PhysicalForm, Double> = emptyMap(),
    val physicalWalletBalances: Map<String, Double> = emptyMap(), // wallet name to balance
    val logicalWalletBalances: Map<String, Double> = emptyMap(), // wallet name to balance
    val expenseTransactionsByTag: Map<String, Double> = emptyMap(), // tag to total expense amount
    val incomeTransactionsByTag: Map<String, Double> = emptyMap(), // tag to total income amount
    val totalPortfolioValue: Double = 0.0,
    val totalPhysicalWalletValue: Double = 0.0,
    val totalLogicalWalletValue: Double = 0.0,
    val totalExpensesByTag: Double = 0.0,
    val totalIncomeByTag: Double = 0.0,
    val wallets: List<Wallet> = emptyList(),
    val defaultCurrency: String = "USD",
    val selectedChartDataType: ChartDataType = ChartDataType.PORTFOLIO_ASSET_COMPOSITION,
    val selectedDateFilter: DateFilterPeriod = DateFilterPeriod.AllTime,
    val availableMonths: List<Pair<Int, Int>> = emptyList(), // (year, month) pairs
    val availableYears: List<Int> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""

    fun loadPortfolioData() {
        if (userId.isEmpty()) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "User not authenticated"
                ) 
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            logger.d("ReportViewModel", "Loading portfolio data for user: $userId")

            try {
                combine(
                    walletRepository.getUserWallets(userId),
                    transactionRepository.getUserTransactionsForCalculations(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { wallets, transactions, preferences ->
                    Triple(wallets, transactions, preferences)
                }.collect { (wallets, transactions, preferences) ->
                    logger.d("ReportViewModel", "Data received - wallets: ${wallets.size}, transactions: ${transactions.size}")

                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    val currentDateFilter = _uiState.value.selectedDateFilter

                    // Calculate available months and years from transactions
                    val availableMonths = getAvailableMonths(transactions)
                    val availableYears = getAvailableYears(transactions)

                    logger.d("ReportViewModel", "Applying date filter: ${currentDateFilter.getDisplayText()}")

                    // Calculate portfolio composition by PhysicalForm (using historical reconstruction)
                    val portfolioComposition = calculatePortfolioComposition(wallets, transactions, currentDateFilter)
                    val totalPortfolioValue = portfolioComposition.values.sum()

                    // Calculate physical wallet balances (using historical reconstruction)
                    val physicalWalletBalances = calculatePhysicalWalletBalances(wallets, transactions, currentDateFilter)
                    val totalPhysicalWalletValue = physicalWalletBalances.values.sum()

                    // Calculate logical wallet balances (using historical reconstruction)
                    val logicalWalletBalances = calculateLogicalWalletBalances(wallets, transactions, currentDateFilter)
                    val totalLogicalWalletValue = logicalWalletBalances.values.sum()

                    // Calculate expense and income transactions by tag (using simple filtering)
                    val expenseTransactionsByTag = calculateExpenseTransactionsByTag(transactions, currentDateFilter)
                    val totalExpensesByTag = expenseTransactionsByTag.values.sum()
                    val incomeTransactionsByTag = calculateIncomeTransactionsByTag(transactions, currentDateFilter)
                    val totalIncomeByTag = incomeTransactionsByTag.values.sum()

                    logger.d("ReportViewModel", "Portfolio composition: $portfolioComposition, total: $totalPortfolioValue")
                    logger.d("ReportViewModel", "Physical wallet balances: $physicalWalletBalances, total: $totalPhysicalWalletValue")
                    logger.d("ReportViewModel", "Logical wallet balances: $logicalWalletBalances, total: $totalLogicalWalletValue")
                    logger.d("ReportViewModel", "Expense transactions by tag: $expenseTransactionsByTag, total: $totalExpensesByTag")
                    logger.d("ReportViewModel", "Income transactions by tag: $incomeTransactionsByTag, total: $totalIncomeByTag")

                    _uiState.update {
                        it.copy(
                            portfolioComposition = portfolioComposition,
                            physicalWalletBalances = physicalWalletBalances,
                            logicalWalletBalances = logicalWalletBalances,
                            expenseTransactionsByTag = expenseTransactionsByTag,
                            incomeTransactionsByTag = incomeTransactionsByTag,
                            totalPortfolioValue = totalPortfolioValue,
                            totalPhysicalWalletValue = totalPhysicalWalletValue,
                            totalLogicalWalletValue = totalLogicalWalletValue,
                            totalExpensesByTag = totalExpensesByTag,
                            totalIncomeByTag = totalIncomeByTag,
                            wallets = wallets,
                            defaultCurrency = defaultCurrency,
                            selectedDateFilter = currentDateFilter,
                            availableMonths = availableMonths,
                            availableYears = availableYears,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                logger.e("ReportViewModel", "Error loading portfolio data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load portfolio data"
                    )
                }
            }
        }
    }
    
    private fun calculatePortfolioComposition(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        dateFilter: DateFilterPeriod
    ): Map<PhysicalForm, Double> {
        // Use historical reconstruction to get balances for the period
        val (startDate, endDate) = dateFilter.getDateRange()
        return BalanceReconstructionUtils.reconstructPortfolioComposition(wallets, transactions, startDate, endDate)
    }
    
    private fun calculatePhysicalWalletBalances(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        dateFilter: DateFilterPeriod
    ): Map<String, Double> {
        // Use historical reconstruction to get balances for the period
        val (startDate, endDate) = dateFilter.getDateRange()
        return BalanceReconstructionUtils.reconstructPhysicalWalletBalances(wallets, transactions, startDate, endDate)
    }
    
    private fun calculateLogicalWalletBalances(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        dateFilter: DateFilterPeriod
    ): Map<String, Double> {
        // Use historical reconstruction to get balances for the period
        val (startDate, endDate) = dateFilter.getDateRange()
        return BalanceReconstructionUtils.reconstructLogicalWalletBalances(wallets, transactions, startDate, endDate)
    }
    
    private fun calculateExpenseTransactionsByTag(
        transactions: List<Transaction>,
        dateFilter: DateFilterPeriod
    ): Map<String, Double> {
        // Use simple filtering to get transactions in the period
        val filteredTransactions = DateFilterUtils.filterTransactionsByDate(transactions, dateFilter)
        val tagAmounts = mutableMapOf<String, Double>()

        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { transaction ->
            // Use the tags field, fallback to category for backward compatibility
            val tags = if (transaction.tags.isNotEmpty()) {
                transaction.tags
            } else {
                listOf(transaction.category)
            }

            // For each tag, add the expense transaction amount (absolute value)
            tags.forEach { tag ->
                val currentAmount = tagAmounts[tag] ?: 0.0
                tagAmounts[tag] = currentAmount + kotlin.math.abs(transaction.amount)

                logger.d("ReportViewModel",
                    "Adding expense transaction '${transaction.title}': tag=$tag, amount=${kotlin.math.abs(transaction.amount)}")
            }
        }

        logger.d("ReportViewModel", "Final expense tag amounts: $tagAmounts")
        return tagAmounts.toMap()
    }
    
    private fun calculateIncomeTransactionsByTag(
        transactions: List<Transaction>,
        dateFilter: DateFilterPeriod
    ): Map<String, Double> {
        // Use simple filtering to get transactions in the period
        val filteredTransactions = DateFilterUtils.filterTransactionsByDate(transactions, dateFilter)
        val tagAmounts = mutableMapOf<String, Double>()

        filteredTransactions.filter { it.type == TransactionType.INCOME }.forEach { transaction ->
            // Use the tags field, fallback to category for backward compatibility
            val tags = if (transaction.tags.isNotEmpty()) {
                transaction.tags
            } else {
                listOf(transaction.category)
            }

            // For each tag, add the income transaction amount (absolute value)
            tags.forEach { tag ->
                val currentAmount = tagAmounts[tag] ?: 0.0
                tagAmounts[tag] = currentAmount + kotlin.math.abs(transaction.amount)

                logger.d("ReportViewModel",
                    "Adding income transaction '${transaction.title}': tag=$tag, amount=${kotlin.math.abs(transaction.amount)}")
            }
        }

        logger.d("ReportViewModel", "Final income tag amounts: $tagAmounts")
        return tagAmounts.toMap()
    }
    
    fun refreshData() {
        logger.d("ReportViewModel", "Refreshing portfolio data")
        loadPortfolioData()
    }
    
    fun selectChartDataType(dataType: ChartDataType) {
        _uiState.update { it.copy(selectedChartDataType = dataType) }
    }

    fun selectDateFilter(period: DateFilterPeriod) {
        logger.d("ReportViewModel", "Selecting date filter: ${period.getDisplayText()}")
        _uiState.update { it.copy(selectedDateFilter = period) }
        // Reload data with new filter
        loadPortfolioData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun getAvailableMonths(transactions: List<Transaction>): List<Pair<Int, Int>> {
        val months = mutableSetOf<Pair<Int, Int>>()

        transactions.forEach { transaction ->
            transaction.transactionDate?.let { date ->
                val calendar = Calendar.getInstance().apply { time = date }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
                months.add(Pair(year, month))
            }
        }

        return months.sortedWith(compareByDescending<Pair<Int, Int>> { it.first }.thenByDescending { it.second })
    }

    private fun getAvailableYears(transactions: List<Transaction>): List<Int> {
        val years = mutableSetOf<Int>()

        transactions.forEach { transaction ->
            transaction.transactionDate?.let { date ->
                val calendar = Calendar.getInstance().apply { time = date }
                years.add(calendar.get(Calendar.YEAR))
            }
        }

        return years.sorted()
    }

    // Helper function to get wallets by physical form
    fun getWalletsByPhysicalForm(physicalForm: PhysicalForm): List<Wallet> {
        return _uiState.value.wallets.filter { 
            it.physicalForm == physicalForm && it.balance > 0 
        }
    }
    
    // Helper function to calculate percentage for a physical form
    fun getPhysicalFormPercentage(physicalForm: PhysicalForm): Double {
        val total = _uiState.value.totalPortfolioValue
        val amount = _uiState.value.portfolioComposition[physicalForm] ?: 0.0
        return if (total > 0) (amount / total) * 100 else 0.0
    }
}
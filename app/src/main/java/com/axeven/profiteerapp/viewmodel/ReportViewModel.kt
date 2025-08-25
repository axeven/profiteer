package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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
            
            android.util.Log.d("ReportViewModel", "Loading portfolio data for user: $userId")

            try {
                combine(
                    walletRepository.getUserWallets(userId),
                    transactionRepository.getUserTransactionsForCalculations(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { wallets, transactions, preferences ->
                    Triple(wallets, transactions, preferences)
                }.collect { (wallets, transactions, preferences) ->
                    android.util.Log.d("ReportViewModel", "Data received - wallets: ${wallets.size}, transactions: ${transactions.size}")
                    
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    
                    // Calculate portfolio composition by PhysicalForm
                    val portfolioComposition = calculatePortfolioComposition(wallets)
                    val totalPortfolioValue = portfolioComposition.values.sum()
                    
                    // Calculate physical wallet balances
                    val physicalWalletBalances = calculatePhysicalWalletBalances(wallets)
                    val totalPhysicalWalletValue = physicalWalletBalances.values.sum()
                    
                    // Calculate logical wallet balances
                    val logicalWalletBalances = calculateLogicalWalletBalances(wallets)
                    val totalLogicalWalletValue = logicalWalletBalances.values.sum()
                    
                    // Calculate expense and income transactions by tag
                    val expenseTransactionsByTag = calculateExpenseTransactionsByTag(transactions)
                    val totalExpensesByTag = expenseTransactionsByTag.values.sum()
                    val incomeTransactionsByTag = calculateIncomeTransactionsByTag(transactions)
                    val totalIncomeByTag = incomeTransactionsByTag.values.sum()
                    
                    android.util.Log.d("ReportViewModel", "Portfolio composition: $portfolioComposition, total: $totalPortfolioValue")
                    android.util.Log.d("ReportViewModel", "Physical wallet balances: $physicalWalletBalances, total: $totalPhysicalWalletValue")
                    android.util.Log.d("ReportViewModel", "Logical wallet balances: $logicalWalletBalances, total: $totalLogicalWalletValue")
                    android.util.Log.d("ReportViewModel", "Expense transactions by tag: $expenseTransactionsByTag, total: $totalExpensesByTag")
                    android.util.Log.d("ReportViewModel", "Income transactions by tag: $incomeTransactionsByTag, total: $totalIncomeByTag")
                    
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
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ReportViewModel", "Error loading portfolio data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load portfolio data"
                    )
                }
            }
        }
    }
    
    private fun calculatePortfolioComposition(wallets: List<Wallet>): Map<PhysicalForm, Double> {
        // Group PHYSICAL wallets by their physical form and sum their balances
        val composition = mutableMapOf<PhysicalForm, Double>()
        
        wallets.forEach { wallet ->
            // Only include physical wallets with positive balances
            if (wallet.walletType == "Physical" && wallet.balance > 0) {
                val currentAmount = composition[wallet.physicalForm] ?: 0.0
                composition[wallet.physicalForm] = currentAmount + wallet.balance
                
                android.util.Log.d("ReportViewModel", 
                    "Adding physical wallet '${wallet.name}': ${wallet.physicalForm} = ${wallet.balance}")
            }
        }
        
        return composition.toMap()
    }
    
    private fun calculatePhysicalWalletBalances(wallets: List<Wallet>): Map<String, Double> {
        // Get only physical wallets with positive balances
        return wallets
            .filter { it.walletType == "Physical" && it.balance > 0 }
            .associate { wallet -> wallet.name to wallet.balance }
    }
    
    private fun calculateLogicalWalletBalances(wallets: List<Wallet>): Map<String, Double> {
        // Get only logical wallets with non-zero balances (including negative)
        return wallets
            .filter { it.walletType == "Logical" && it.balance != 0.0 }
            .associate { wallet -> wallet.name to wallet.balance }
    }
    
    private fun calculateExpenseTransactionsByTag(transactions: List<Transaction>): Map<String, Double> {
        val tagAmounts = mutableMapOf<String, Double>()
        
        transactions.filter { it.type == TransactionType.EXPENSE }.forEach { transaction ->
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
                
                android.util.Log.d("ReportViewModel", 
                    "Adding expense transaction '${transaction.title}': tag=$tag, amount=${kotlin.math.abs(transaction.amount)}")
            }
        }
        
        android.util.Log.d("ReportViewModel", "Final expense tag amounts: $tagAmounts")
        return tagAmounts.toMap()
    }
    
    private fun calculateIncomeTransactionsByTag(transactions: List<Transaction>): Map<String, Double> {
        val tagAmounts = mutableMapOf<String, Double>()
        
        transactions.filter { it.type == TransactionType.INCOME }.forEach { transaction ->
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
                
                android.util.Log.d("ReportViewModel", 
                    "Adding income transaction '${transaction.title}': tag=$tag, amount=${kotlin.math.abs(transaction.amount)}")
            }
        }
        
        android.util.Log.d("ReportViewModel", "Final income tag amounts: $tagAmounts")
        return tagAmounts.toMap()
    }
    
    fun refreshData() {
        android.util.Log.d("ReportViewModel", "Refreshing portfolio data")
        loadPortfolioData()
    }
    
    fun selectChartDataType(dataType: ChartDataType) {
        _uiState.update { it.copy(selectedChartDataType = dataType) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
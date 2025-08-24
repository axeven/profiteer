package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.CurrencyRateRepository
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import java.text.SimpleDateFormat

enum class TransferDirection {
    INCOMING, OUTGOING
}

data class WalletDetailUiState(
    val wallet: Wallet? = null,
    val transactions: List<Transaction> = emptyList(),
    val allWallets: List<Wallet> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val displayCurrency: String = "USD",
    val defaultCurrency: String = "USD",
    val displayRate: Double = 1.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRecalculatingBalance: Boolean = false,
    val recalculationError: String? = null,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val filteredTransactions: List<Transaction> = emptyList(),
    val transactionCount: Int = 0,
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val expandedDates: Set<String> = emptySet()
)

@HiltViewModel
class WalletDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRateRepository: CurrencyRateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletDetailUiState())
    val uiState: StateFlow<WalletDetailUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""
    private var currentWalletId: String = ""
    
    // Date formatters for transaction grouping
    private val dateGroupingFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateDisplayFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    fun loadWalletDetails(walletId: String) {
        if (walletId.isEmpty() || userId.isEmpty()) return
        
        currentWalletId = walletId
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            android.util.Log.d("WalletDetailViewModel", "Loading wallet details for: $walletId")

            try {
                combine(
                    transactionRepository.getWalletTransactions(walletId),
                    walletRepository.getUserWallets(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { transactions, wallets, preferences ->
                    Triple(transactions, wallets, preferences)
                }.collect { (transactions, wallets, preferences) ->
                    android.util.Log.d("WalletDetailViewModel", "Data update received - transactions: ${transactions.size}, wallets: ${wallets.size}")
                    
                    val wallet = wallets.find { it.id == walletId }
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    val displayCurrency = preferences?.displayCurrency ?: defaultCurrency
                    
                    // Get display rate for UI conversion
                    val displayRate = if (defaultCurrency != displayCurrency) {
                        currencyRateRepository.getDisplayRate(userId, defaultCurrency, displayCurrency).getOrElse { 1.0 }
                    } else {
                        1.0
                    }
                    
                    // Use selected month for filtering (defaults to current month)
                    val currentState = _uiState.value
                    val selectedMonth = currentState.selectedMonth
                    val selectedYear = currentState.selectedYear
                    
                    val filteredTransactions = filterTransactionsByMonth(transactions, selectedMonth, selectedYear)
                    
                    // Group transactions by date
                    val groupedTransactions = groupTransactionsByDate(filteredTransactions, walletId)
                    
                    val monthlyIncome = filteredTransactions
                        .filter { 
                            it.type == TransactionType.INCOME || 
                            (it.type == TransactionType.TRANSFER && isTransferIncome(it, walletId))
                        }
                        .sumOf { 
                            val amount = if (it.type == TransactionType.TRANSFER) abs(it.amount) else it.amount
                            amount // All transactions now use default currency
                        }
                    
                    val monthlyExpenses = filteredTransactions
                        .filter { 
                            it.type == TransactionType.EXPENSE || 
                            (it.type == TransactionType.TRANSFER && isTransferExpense(it, walletId))
                        }
                        .sumOf { 
                            val amount = if (it.type == TransactionType.TRANSFER) abs(it.amount) else abs(it.amount)
                            amount // All transactions now use default currency
                        }
                    
                    // All wallets now use default currency - no conversion needed for storage
                    // Display conversion is handled by displayRate for UI presentation

                    _uiState.update {
                        it.copy(
                            wallet = wallet,
                            transactions = transactions,
                            allWallets = wallets,
                            monthlyIncome = monthlyIncome,
                            monthlyExpenses = monthlyExpenses,
                            displayCurrency = displayCurrency,
                            defaultCurrency = defaultCurrency,
                            displayRate = displayRate,
                            isLoading = false,
                            error = null,
                            filteredTransactions = filteredTransactions,
                            transactionCount = filteredTransactions.size,
                            groupedTransactions = groupedTransactions,
                            expandedDates = currentState.expandedDates
                        )
                    }
                    
                    android.util.Log.d("WalletDetailViewModel", "UI state updated - balance: ${wallet?.balance}, monthly income: $monthlyIncome, monthly expenses: $monthlyExpenses")
                }
            } catch (e: Exception) {
                android.util.Log.e("WalletDetailViewModel", "Error loading wallet details", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    
    fun refreshData() {
        android.util.Log.d("WalletDetailViewModel", "refreshData called for wallet: $currentWalletId")
        if (currentWalletId.isNotEmpty()) {
            loadWalletDetails(currentWalletId)
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearRecalculationError() {
        _uiState.update { it.copy(recalculationError = null) }
    }
    
    fun recalculateBalance() {
        android.util.Log.d("WalletDetailViewModel", "recalculateBalance called for wallet: $currentWalletId")
        
        if (currentWalletId.isEmpty() || userId.isEmpty()) {
            _uiState.update { it.copy(recalculationError = "Unable to recalculate: wallet or user not found") }
            return
        }
        
        val currentWallet = _uiState.value.wallet
        if (currentWallet == null) {
            _uiState.update { it.copy(recalculationError = "Unable to recalculate: wallet data not available") }
            return
        }
        
        // Prevent multiple simultaneous recalculations
        if (_uiState.value.isRecalculatingBalance) {
            android.util.Log.d("WalletDetailViewModel", "Recalculation already in progress, ignoring request")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isRecalculatingBalance = true, recalculationError = null) }
                
                android.util.Log.d("WalletDetailViewModel", "Starting balance recalculation for wallet: ${currentWallet.name}")
                
                // Get all transactions for this wallet (not just current month)
                val allTransactions = _uiState.value.transactions
                
                // Calculate balance using same logic as existing monthly calculations
                val income = allTransactions
                    .filter { 
                        it.type == TransactionType.INCOME || 
                        (it.type == TransactionType.TRANSFER && isTransferIncome(it, currentWalletId))
                    }
                    .sumOf { 
                        val amount = if (it.type == TransactionType.TRANSFER) abs(it.amount) else it.amount
                        amount
                    }
                
                val expenses = allTransactions
                    .filter { 
                        it.type == TransactionType.EXPENSE || 
                        (it.type == TransactionType.TRANSFER && isTransferExpense(it, currentWalletId))
                    }
                    .sumOf { 
                        val amount = if (it.type == TransactionType.TRANSFER) abs(it.amount) else abs(it.amount)
                        amount
                    }
                
                // Calculate new balance: initial balance + income - expenses
                val newBalance = currentWallet.initialBalance + income - expenses
                
                android.util.Log.d("WalletDetailViewModel", "Balance calculation: initial=${currentWallet.initialBalance}, income=$income, expenses=$expenses, new=$newBalance")
                
                // Update wallet in Firestore
                val updatedWallet = currentWallet.copy(balance = newBalance)
                val updateResult = walletRepository.updateWallet(updatedWallet)
                
                updateResult.fold(
                    onSuccess = {
                        android.util.Log.d("WalletDetailViewModel", "Balance recalculation successful: $newBalance")
                        _uiState.update { 
                            it.copy(
                                isRecalculatingBalance = false, 
                                recalculationError = null,
                                wallet = updatedWallet
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        android.util.Log.e("WalletDetailViewModel", "Error updating wallet balance", exception)
                        _uiState.update { 
                            it.copy(
                                isRecalculatingBalance = false,
                                recalculationError = "Failed to update balance: ${exception.message}"
                            ) 
                        }
                    }
                )
                
            } catch (e: Exception) {
                android.util.Log.e("WalletDetailViewModel", "Error during balance recalculation", e)
                _uiState.update { 
                    it.copy(
                        isRecalculatingBalance = false,
                        recalculationError = "Recalculation failed: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun isTransferIncome(transaction: Transaction, walletId: String): Boolean {
        return transaction.type == TransactionType.TRANSFER && transaction.destinationWalletId == walletId
    }
    
    private fun isTransferExpense(transaction: Transaction, walletId: String): Boolean {
        return transaction.type == TransactionType.TRANSFER && transaction.sourceWalletId == walletId
    }
    
    fun getTransferDirection(transaction: Transaction, walletId: String): TransferDirection? {
        return when {
            transaction.type != TransactionType.TRANSFER -> null
            transaction.sourceWalletId == walletId -> TransferDirection.OUTGOING
            transaction.destinationWalletId == walletId -> TransferDirection.INCOMING
            else -> null
        }
    }
    
    fun getEffectiveTransactionType(transaction: Transaction, walletId: String): TransactionType {
        return when {
            transaction.type != TransactionType.TRANSFER -> transaction.type
            transaction.sourceWalletId == walletId -> TransactionType.EXPENSE
            transaction.destinationWalletId == walletId -> TransactionType.INCOME
            else -> transaction.type
        }
    }
    
    fun getTransferDisplayAmount(transaction: Transaction, walletId: String): Double {
        return when {
            transaction.type != TransactionType.TRANSFER -> transaction.amount
            transaction.sourceWalletId == walletId -> -abs(transaction.amount) // Outgoing: negative
            transaction.destinationWalletId == walletId -> abs(transaction.amount) // Incoming: positive
            else -> transaction.amount
        }
    }
    
    fun getTransferCounterpartWallet(transaction: Transaction, walletId: String, allWallets: List<Wallet>): Wallet? {
        return when {
            transaction.type != TransactionType.TRANSFER -> null
            transaction.sourceWalletId == walletId -> allWallets.find { it.id == transaction.destinationWalletId }
            transaction.destinationWalletId == walletId -> allWallets.find { it.id == transaction.sourceWalletId }
            else -> null
        }
    }
    
    fun toggleDateExpansion(dateKey: String) {
        _uiState.update { currentState ->
            val expandedDates = if (currentState.expandedDates.contains(dateKey)) {
                currentState.expandedDates - dateKey
            } else {
                currentState.expandedDates + dateKey
            }
            currentState.copy(expandedDates = expandedDates)
        }
    }
    
    private fun groupTransactionsByDate(transactions: List<Transaction>, walletId: String): Map<String, List<Transaction>> {
        return transactions
            .sortedByDescending { it.transactionDate ?: it.createdAt }
            .groupBy { transaction ->
                val date = transaction.transactionDate ?: transaction.createdAt
                date?.let { dateGroupingFormat.format(it) } ?: ""
            }
            .filter { it.key.isNotEmpty() }
            .toSortedMap(compareByDescending { it })
    }
    
    fun getDateDisplayString(dateKey: String): String {
        return try {
            val date = dateGroupingFormat.parse(dateKey)
            date?.let { dateDisplayFormat.format(it) } ?: dateKey
        } catch (e: Exception) {
            dateKey
        }
    }
    
    fun calculateDailySummary(transactions: List<Transaction>, walletId: String): Pair<Int, Double> {
        val count = transactions.size
        val netAmount = transactions.sumOf { transaction ->
            when {
                transaction.type == TransactionType.INCOME -> transaction.amount
                transaction.type == TransactionType.EXPENSE -> -abs(transaction.amount)
                transaction.type == TransactionType.TRANSFER && transaction.sourceWalletId == walletId -> -abs(transaction.amount)
                transaction.type == TransactionType.TRANSFER && transaction.destinationWalletId == walletId -> abs(transaction.amount)
                else -> 0.0
            }
        }
        return Pair(count, netAmount)
    }
    
    fun setSelectedMonth(month: Int, year: Int) {
        android.util.Log.d("WalletDetailViewModel", "Setting selected month: $month/$year")
        _uiState.update { 
            it.copy(selectedMonth = month, selectedYear = year) 
        }
        
        // Recompute filtered data with new month selection
        if (currentWalletId.isNotEmpty()) {
            recomputeFilteredData()
        }
    }
    
    fun navigateToPreviousMonth() {
        val currentState = _uiState.value
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, currentState.selectedMonth)
            set(Calendar.YEAR, currentState.selectedYear)
            add(Calendar.MONTH, -1)
        }
        setSelectedMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
    }
    
    fun navigateToNextMonth() {
        val currentState = _uiState.value
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, currentState.selectedMonth)
            set(Calendar.YEAR, currentState.selectedYear)
            add(Calendar.MONTH, 1)
        }
        setSelectedMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
    }
    
    fun canNavigateToPrevious(): Boolean {
        // Check if there are any transactions before the selected month
        val currentState = _uiState.value
        val selectedMonthStart = getMonthStart(currentState.selectedMonth, currentState.selectedYear)
        return currentState.transactions.any { transaction ->
            val transactionDate = transaction.transactionDate ?: transaction.createdAt
            transactionDate != null && transactionDate.before(selectedMonthStart)
        }
    }
    
    fun canNavigateToNext(): Boolean {
        // Don't allow navigation to future months
        val currentState = _uiState.value
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return currentState.selectedMonth < currentMonth || currentState.selectedYear < currentYear
    }
    
    private fun getMonthStart(month: Int, year: Int): Date {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
    
    private fun getMonthEnd(month: Int, year: Int): Date {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }
    
    private fun filterTransactionsByMonth(transactions: List<Transaction>, month: Int, year: Int): List<Transaction> {
        val monthStart = getMonthStart(month, year)
        val monthEnd = getMonthEnd(month, year)
        
        return transactions.filter { transaction ->
            val transactionDate = transaction.transactionDate ?: transaction.createdAt
            transactionDate != null && 
            !transactionDate.before(monthStart) && 
            !transactionDate.after(monthEnd)
        }
    }
    
    private fun recomputeFilteredData() {
        // This will be called when month selection changes
        // The main data loading logic will handle filtering and calculations
        if (currentWalletId.isNotEmpty()) {
            loadWalletDetails(currentWalletId)
        }
    }
}



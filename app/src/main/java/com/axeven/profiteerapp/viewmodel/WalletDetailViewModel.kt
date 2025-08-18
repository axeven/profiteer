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
    val useWalletCurrency: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversionWarning: String? = null,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val filteredTransactions: List<Transaction> = emptyList(),
    val transactionCount: Int = 0
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
                    userPreferencesRepository.getUserPreferences(userId),
                    currencyRateRepository.getUserCurrencyRates(userId)
                ) { transactions, wallets, preferences, currencyRates ->
                    WalletDetailQuadruple(transactions, wallets, preferences, currencyRates)
                }.collect { (transactions, wallets, preferences, currencyRates) ->
                    android.util.Log.d("WalletDetailViewModel", "Data update received - transactions: ${transactions.size}, wallets: ${wallets.size}")
                    
                    val wallet = wallets.find { it.id == walletId }
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    val currentState = _uiState.value
                    val useWalletCurrency = currentState.useWalletCurrency
                    val displayCurrency = if (useWalletCurrency && wallet?.currency?.isNotBlank() == true) {
                        wallet.currency
                    } else {
                        defaultCurrency
                    }
                    
                    // Use selected month for filtering (defaults to current month)
                    val selectedMonth = currentState.selectedMonth
                    val selectedYear = currentState.selectedYear
                    
                    val filteredTransactions = filterTransactionsByMonth(transactions, selectedMonth, selectedYear)
                    
                    val monthlyIncome = filteredTransactions
                        .filter { 
                            it.type == TransactionType.INCOME || 
                            (it.type == TransactionType.TRANSFER && isTransferIncome(it, walletId))
                        }
                        .sumOf { 
                            val amount = if (it.type == TransactionType.TRANSFER) abs(it.amount) else it.amount
                            convertAmount(amount, getTransactionCurrency(it, wallets), displayCurrency, currencyRates) 
                        }
                    
                    val monthlyExpenses = filteredTransactions
                        .filter { 
                            it.type == TransactionType.EXPENSE || 
                            (it.type == TransactionType.TRANSFER && isTransferExpense(it, walletId))
                        }
                        .sumOf { 
                            val amount = if (it.type == TransactionType.TRANSFER) abs(it.amount) else abs(it.amount)
                            convertAmount(amount, getTransactionCurrency(it, wallets), displayCurrency, currencyRates) 
                        }
                    
                    // Convert wallet balance if needed
                    val convertedBalance = if (wallet != null) {
                        convertAmount(wallet.balance, wallet.currency, displayCurrency, currencyRates)
                    } else 0.0
                    
                    val conversionWarning = if (wallet != null && wallet.currency != displayCurrency) {
                        val conversionRate = findConversionRate(wallet.currency, displayCurrency, currencyRates)
                        if (conversionRate == null) {
                            "Conversion rate from ${wallet.currency} to $displayCurrency is not available. Balance shown in original currency."
                        } else null
                    } else null

                    _uiState.update {
                        it.copy(
                            wallet = wallet?.copy(balance = convertedBalance),
                            transactions = transactions,
                            allWallets = wallets,
                            monthlyIncome = monthlyIncome,
                            monthlyExpenses = monthlyExpenses,
                            displayCurrency = displayCurrency,
                            defaultCurrency = defaultCurrency,
                            isLoading = false,
                            error = null,
                            conversionWarning = conversionWarning,
                            filteredTransactions = filteredTransactions,
                            transactionCount = filteredTransactions.size
                        )
                    }
                    
                    android.util.Log.d("WalletDetailViewModel", "UI state updated - balance: $convertedBalance, monthly income: $monthlyIncome, monthly expenses: $monthlyExpenses")
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
    
    fun toggleCurrency() {
        val currentState = _uiState.value
        val newUseWalletCurrency = !currentState.useWalletCurrency
        
        _uiState.update { it.copy(useWalletCurrency = newUseWalletCurrency) }
        
        // Reload data with new currency setting
        if (currentWalletId.isNotEmpty()) {
            loadWalletDetails(currentWalletId)
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
    
    fun clearConversionWarning() {
        _uiState.update { it.copy(conversionWarning = null) }
    }
    
    private fun convertAmount(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        currencyRates: List<com.axeven.profiteerapp.data.model.CurrencyRate>
    ): Double {
        if (fromCurrency == toCurrency || fromCurrency.isBlank()) {
            return amount
        }
        
        val conversionRate = findConversionRate(fromCurrency, toCurrency, currencyRates)
        return if (conversionRate != null) {
            amount * conversionRate
        } else {
            amount // Return original amount if no conversion rate available
        }
    }
    
    private fun getTransactionCurrency(transaction: Transaction, wallets: List<Wallet>): String {
        return when (transaction.type) {
            TransactionType.TRANSFER -> {
                wallets.find { it.id == transaction.sourceWalletId }?.currency ?: "USD"
            }
            else -> {
                // For Income/Expense, try to find from affected wallets or fallback to primary wallet
                val affectedWallets = if (transaction.affectedWalletIds.isNotEmpty()) {
                    wallets.filter { it.id in transaction.affectedWalletIds }
                } else {
                    wallets.filter { it.id == transaction.walletId }
                }
                affectedWallets.firstOrNull()?.currency ?: "USD"
            }
        }
    }
    
    private fun findConversionRate(
        fromCurrency: String,
        toCurrency: String,
        currencyRates: List<com.axeven.profiteerapp.data.model.CurrencyRate>
    ): Double? {
        // 1. Try to find direct conversion rate with default month (from -> to)
        val directDefaultRate = currencyRates.find { 
            it.fromCurrency == fromCurrency && it.toCurrency == toCurrency && it.month == null 
        }?.rate
        
        if (directDefaultRate != null) {
            return directDefaultRate
        }
        
        // 2. Try to find reverse conversion rate with default month (to -> from) and invert it
        val reverseDefaultRate = currencyRates.find { 
            it.fromCurrency == toCurrency && it.toCurrency == fromCurrency && it.month == null 
        }?.rate
        
        if (reverseDefaultRate != null && reverseDefaultRate != 0.0) {
            return 1.0 / reverseDefaultRate
        }
        
        // 3. Fallback to monthly rates: try direct conversion (from -> to)
        val directMonthlyRate = currencyRates.find { 
            it.fromCurrency == fromCurrency && it.toCurrency == toCurrency && it.month != null 
        }?.rate
        
        if (directMonthlyRate != null) {
            android.util.Log.d("WalletDetailViewModel", "Using monthly rate for $fromCurrency -> $toCurrency: $directMonthlyRate")
            return directMonthlyRate
        }
        
        // 4. Fallback to monthly rates: try reverse conversion (to -> from) and invert it
        val reverseMonthlyRate = currencyRates.find { 
            it.fromCurrency == toCurrency && it.toCurrency == fromCurrency && it.month != null 
        }?.rate
        
        if (reverseMonthlyRate != null && reverseMonthlyRate != 0.0) {
            android.util.Log.d("WalletDetailViewModel", "Using inverted monthly rate for $fromCurrency -> $toCurrency: ${1.0 / reverseMonthlyRate}")
            return 1.0 / reverseMonthlyRate
        }
        
        return null
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

private data class WalletDetailQuadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)


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
import javax.inject.Inject
import kotlin.math.abs

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val defaultCurrency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversionWarning: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRateRepository: CurrencyRateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""

    init {
        if (userId.isNotEmpty()) {
            loadUserData()
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            android.util.Log.d("HomeViewModel", "Starting loadUserData for user: $userId")

            try {
                combine(
                    transactionRepository.getUserTransactions(userId),
                    walletRepository.getUserWallets(userId),
                    userPreferencesRepository.getUserPreferences(userId),
                    currencyRateRepository.getUserCurrencyRates(userId)
                ) { transactions, wallets, preferences, currencyRates ->
                    Quadruple(transactions, wallets, preferences, currencyRates)
                }.collect { (transactions, wallets, preferences, currencyRates) ->
                    android.util.Log.d("HomeViewModel", "Data update received - transactions: ${transactions.size}, wallets: ${wallets.size}")
                    
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    
                    val totalIncome = transactions
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                    
                    val totalExpenses = transactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { abs(it.amount) }
                    
                    // Calculate total balance with currency conversion
                    val physicalWallets = wallets.filter { it.walletType == "Physical" }
                    val (totalBalance, conversionWarning) = calculateConvertedBalance(
                        physicalWallets, 
                        defaultCurrency, 
                        currencyRates
                    )

                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            wallets = wallets,
                            totalBalance = totalBalance,
                            totalIncome = totalIncome,
                            totalExpenses = totalExpenses,
                            defaultCurrency = defaultCurrency,
                            isLoading = false,
                            error = null,
                            conversionWarning = conversionWarning
                        )
                    }
                    
                    android.util.Log.d("HomeViewModel", "UI state updated - balance: $totalBalance, income: $totalIncome, expenses: $totalExpenses")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun createTransaction(
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        walletId: String
    ) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val transaction = Transaction(
                title = title,
                amount = if (type == TransactionType.EXPENSE) -abs(amount) else abs(amount),
                category = category,
                type = type,
                walletId = walletId,
                userId = userId
            )

            transactionRepository.createTransaction(transaction)
                .onSuccess {
                    // Update wallet balance if needed
                    updateWalletBalance(walletId, transaction.amount)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    private suspend fun updateWalletBalance(walletId: String, amount: Double) {
        walletRepository.getWalletById(walletId)
            .onSuccess { wallet ->
                wallet?.let {
                    val updatedWallet = it.copy(balance = it.balance + amount)
                    walletRepository.updateWallet(updatedWallet)
                        .onSuccess {
                            _uiState.update { state ->
                                state.copy(isLoading = false, error = null)
                            }
                        }
                        .onFailure { error ->
                            _uiState.update { state ->
                                state.copy(isLoading = false, error = error.message)
                            }
                        }
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message)
                }
            }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            transactionRepository.deleteTransaction(transactionId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun refreshData() {
        android.util.Log.d("HomeViewModel", "refreshData called for user: $userId")
        if (userId.isNotEmpty()) {
            loadUserData()
        } else {
            android.util.Log.w("HomeViewModel", "refreshData called but userId is empty")
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearConversionWarning() {
        _uiState.update { it.copy(conversionWarning = null) }
    }
    
    private fun calculateConvertedBalance(
        wallets: List<Wallet>,
        defaultCurrency: String,
        currencyRates: List<com.axeven.profiteerapp.data.model.CurrencyRate>
    ): Pair<Double, String?> {
        var convertedBalance = 0.0
        val missingRates = mutableSetOf<String>()
        
        for (wallet in wallets) {
            if (wallet.currency == defaultCurrency || wallet.currency.isBlank()) {
                // Same currency as default or no currency specified, add directly
                convertedBalance += wallet.balance
            } else {
                // Need conversion
                val conversionRate = findConversionRate(wallet.currency, defaultCurrency, currencyRates)
                if (conversionRate != null) {
                    convertedBalance += wallet.balance * conversionRate
                } else {
                    // Missing conversion rate
                    missingRates.add(wallet.currency)
                    android.util.Log.w("HomeViewModel", "Missing conversion rate from ${wallet.currency} to $defaultCurrency for wallet: ${wallet.name}")
                }
            }
        }
        
        val warning = if (missingRates.isNotEmpty()) {
            val currenciesList = missingRates.joinToString(", ")
            "Some wallets with currencies ($currenciesList) are not included in the total because conversion rates are not set."
        } else null
        
        return Pair(convertedBalance, warning)
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
            android.util.Log.d("HomeViewModel", "Using monthly rate for $fromCurrency -> $toCurrency: $directMonthlyRate")
            return directMonthlyRate
        }
        
        // 4. Fallback to monthly rates: try reverse conversion (to -> from) and invert it
        val reverseMonthlyRate = currencyRates.find { 
            it.fromCurrency == toCurrency && it.toCurrency == fromCurrency && it.month != null 
        }?.rate
        
        if (reverseMonthlyRate != null && reverseMonthlyRate != 0.0) {
            android.util.Log.d("HomeViewModel", "Using inverted monthly rate for $fromCurrency -> $toCurrency: ${1.0 / reverseMonthlyRate}")
            return 1.0 / reverseMonthlyRate
        }
        
        return null
    }
}
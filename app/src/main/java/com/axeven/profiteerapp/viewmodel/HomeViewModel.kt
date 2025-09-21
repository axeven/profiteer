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
import com.axeven.profiteerapp.utils.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
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
    val displayCurrency: String = "USD",
    val displayRate: Double = 1.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRateRepository: CurrencyRateRepository,
    private val logger: Logger
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
            
            logger.d("HomeViewModel", "Starting loadUserData for user: $userId")

            try {
                // Get current month start and end dates for filtering calculations
                val calendar = Calendar.getInstance()
                val currentMonthStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val currentMonthEnd = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                combine(
                    transactionRepository.getUserTransactions(userId), // Limited for display
                    transactionRepository.getUserTransactionsForCalculations(userId, currentMonthStart, currentMonthEnd), // Unlimited for calculations
                    walletRepository.getUserWallets(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { displayTransactions, calculationTransactions, wallets, preferences ->
                    Quadruple(displayTransactions, calculationTransactions, wallets, preferences)
                }.collect { (displayTransactions, calculationTransactions, wallets, preferences) ->
                    logger.d("HomeViewModel", "Data update received - display: ${displayTransactions.size}, calculation: ${calculationTransactions.size}, wallets: ${wallets.size}")
                    
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    val displayCurrency = preferences?.displayCurrency ?: defaultCurrency
                    
                    // Get display rate for UI conversion
                    val displayRate = if (defaultCurrency != displayCurrency) {
                        currencyRateRepository.getDisplayRate(userId, defaultCurrency, displayCurrency).getOrElse { 1.0 }
                    } else {
                        1.0
                    }
                    
                    // Use calculation transactions for accurate totals (current month only)
                    // All transactions now use default currency - no conversion needed
                    val totalIncome = calculationTransactions
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                    
                    val totalExpenses = calculationTransactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { abs(it.amount) }
                    
                    // Calculate total balance - all wallets now use default currency
                    val physicalWallets = wallets.filter { it.walletType == "Physical" }
                    val totalBalance = physicalWallets.sumOf { it.balance }

                    _uiState.update {
                        it.copy(
                            transactions = displayTransactions, // Use limited transactions for UI display
                            wallets = wallets,
                            totalBalance = totalBalance,
                            totalIncome = totalIncome, // Calculated from all current month transactions
                            totalExpenses = totalExpenses, // Calculated from all current month transactions
                            defaultCurrency = defaultCurrency,
                            displayCurrency = displayCurrency,
                            displayRate = displayRate,
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    logger.d("HomeViewModel", "UI state updated - balance: $totalBalance, income: $totalIncome, expenses: $totalExpenses")
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
        logger.d("HomeViewModel", "refreshData called for user: $userId")
        if (userId.isNotEmpty()) {
            loadUserData()
        } else {
            logger.w("HomeViewModel", "refreshData called but userId is empty")
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    
    
    
    
}


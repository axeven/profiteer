package com.axeven.profiteer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteer.data.model.Transaction
import com.axeven.profiteer.data.model.TransactionType
import com.axeven.profiteer.data.model.UserPreferences
import com.axeven.profiteer.data.model.Wallet
import com.axeven.profiteer.data.repository.AuthRepository
import com.axeven.profiteer.data.repository.TransactionRepository
import com.axeven.profiteer.data.repository.UserPreferencesRepository
import com.axeven.profiteer.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val defaultCurrency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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

            try {
                combine(
                    transactionRepository.getUserTransactions(userId),
                    walletRepository.getUserWallets(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { transactions, wallets, preferences ->
                    Triple(transactions, wallets, preferences)
                }.collect { (transactions, wallets, preferences) ->
                    
                    val totalIncome = transactions
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                    
                    val totalExpenses = transactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { abs(it.amount) }
                    
                    val totalBalance = totalIncome - totalExpenses

                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            wallets = wallets,
                            totalBalance = totalBalance,
                            totalIncome = totalIncome,
                            totalExpenses = totalExpenses,
                            defaultCurrency = preferences?.defaultCurrency ?: "USD",
                            isLoading = false,
                            error = null
                        )
                    }
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
        if (userId.isNotEmpty()) {
            loadUserData()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
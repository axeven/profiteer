package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.constants.UIConstants
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import com.axeven.profiteerapp.utils.TagNormalizer
import com.axeven.profiteerapp.utils.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val defaultCurrency: String = "USD",
    val displayCurrency: String = "USD",
    val displayRate: Double = 1.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableTags: List<String> = emptyList()
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""

    init {
        if (userId.isNotEmpty()) {
            loadData()
        }
    }

    private fun loadData() {
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
                    // Normalize tags: trim, lowercase, deduplicate, filter out "Untagged" and blanks
                    // Using normalizeTags() which handles all normalization including filtering "untagged"
                    val allTags = transactions.flatMap { it.tags }
                    val uniqueTags = TagNormalizer.normalizeTags(allTags).sorted()

                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    val displayCurrency = preferences?.displayCurrency ?: defaultCurrency
                    
                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            wallets = wallets,
                            defaultCurrency = defaultCurrency,
                            displayCurrency = displayCurrency,
                            displayRate = 1.0, // Display rate will be handled at UI level
                            availableTags = uniqueTags,
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
        affectedWalletIds: List<String>,
        tags: List<String> = emptyList(),
        transactionDate: Date
    ) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val transaction = Transaction(
                title = title,
                amount = if (type == TransactionType.EXPENSE) -abs(amount) else abs(amount),
                category = category,
                type = type,
                walletId = affectedWalletIds.firstOrNull() ?: "", // For backward compatibility
                affectedWalletIds = affectedWalletIds,
                tags = tags,
                transactionDate = transactionDate,
                userId = userId
            )

            transactionRepository.createTransaction(transaction)
                .onSuccess {
                    // Update all affected wallet balances
                    affectedWalletIds.forEach { walletId ->
                        updateWalletBalance(walletId, transaction.amount)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    // Keep the old method for backward compatibility
    fun createTransaction(
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        walletId: String,
        transactionDate: Date
    ) {
        createTransaction(
            title = title,
            amount = amount,
            category = category,
            type = type,
            affectedWalletIds = listOf(walletId),
            tags = emptyList(),
            transactionDate = transactionDate
        )
    }

    fun createTransferTransaction(
        title: String,
        amount: Double,
        sourceWalletId: String,
        destinationWalletId: String,
        transactionDate: Date
    ) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val transaction = Transaction(
                title = title,
                amount = amount,
                category = "Transfer",
                type = TransactionType.TRANSFER,
                sourceWalletId = sourceWalletId,
                destinationWalletId = destinationWalletId,
                transactionDate = transactionDate,
                userId = userId
            )

            transactionRepository.createTransaction(transaction)
                .onSuccess {
                    // Update both wallet balances
                    updateWalletBalance(sourceWalletId, -abs(amount)) // Subtract from source
                    updateWalletBalance(destinationWalletId, abs(amount)) // Add to destination
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun updateTransactionWithMultipleWallets(
        transactionId: String,
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        affectedWalletIds: List<String>,
        tags: List<String> = emptyList(),
        transactionDate: Date
    ) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // First get the original transaction to reverse its wallet effects
            transactionRepository.getTransactionById(transactionId)
                .onSuccess { originalTransaction ->
                    originalTransaction?.let { original ->
                        // Reverse the original transaction's wallet effects
                        when (original.type) {
                            TransactionType.INCOME, TransactionType.EXPENSE -> {
                                // Handle both old single wallet and new multiple wallets format
                                val walletsToReverse = if (original.affectedWalletIds.isNotEmpty()) {
                                    original.affectedWalletIds
                                } else {
                                    listOf(original.walletId)
                                }
                                
                                walletsToReverse.forEach { walletId ->
                                    updateWalletBalance(walletId, -original.amount)
                                }
                            }
                            TransactionType.TRANSFER -> {
                                updateWalletBalance(original.sourceWalletId, abs(original.amount)) // Add back to source
                                updateWalletBalance(original.destinationWalletId, -abs(original.amount)) // Remove from destination
                            }
                        }

                        // Create updated transaction
                        val updatedTransaction = original.copy(
                            title = title,
                            amount = if (type == TransactionType.EXPENSE) -abs(amount) else abs(amount),
                            category = category,
                            type = type,
                            walletId = affectedWalletIds.firstOrNull() ?: "", // For backward compatibility
                            affectedWalletIds = affectedWalletIds,
                            tags = tags,
                            sourceWalletId = "",
                            destinationWalletId = "",
                            transactionDate = transactionDate
                        )

                        transactionRepository.updateTransaction(updatedTransaction)
                            .onSuccess {
                                // Apply new transaction's wallet effects
                                affectedWalletIds.forEach { walletId ->
                                    updateWalletBalance(walletId, updatedTransaction.amount)
                                }
                            }
                            .onFailure { error ->
                                _uiState.update {
                                    it.copy(isLoading = false, error = error.message)
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
    }

    fun updateTransaction(
        transactionId: String,
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        walletId: String? = null,
        sourceWalletId: String? = null,
        destinationWalletId: String? = null,
        transactionDate: Date
    ) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // First get the original transaction to reverse its wallet effects
            transactionRepository.getTransactionById(transactionId)
                .onSuccess { originalTransaction ->
                    originalTransaction?.let { original ->
                        // Reverse the original transaction's wallet effects
                        when (original.type) {
                            TransactionType.INCOME, TransactionType.EXPENSE -> {
                                // Handle both old single wallet and new multiple wallets format
                                val walletsToReverse = if (original.affectedWalletIds.isNotEmpty()) {
                                    original.affectedWalletIds
                                } else {
                                    listOf(original.walletId)
                                }
                                
                                walletsToReverse.forEach { walletId ->
                                    updateWalletBalance(walletId, -original.amount)
                                }
                            }
                            TransactionType.TRANSFER -> {
                                updateWalletBalance(original.sourceWalletId, abs(original.amount)) // Add back to source
                                updateWalletBalance(original.destinationWalletId, -abs(original.amount)) // Remove from destination
                            }
                        }

                        // Create updated transaction
                        val updatedTransaction = when (type) {
                            TransactionType.INCOME, TransactionType.EXPENSE -> {
                                original.copy(
                                    title = title,
                                    amount = if (type == TransactionType.EXPENSE) -abs(amount) else abs(amount),
                                    category = category,
                                    type = type,
                                    walletId = walletId ?: "",
                                    sourceWalletId = "",
                                    destinationWalletId = "",
                                    transactionDate = transactionDate
                                )
                            }
                            TransactionType.TRANSFER -> {
                                original.copy(
                                    title = title,
                                    amount = amount,
                                    category = "Transfer",
                                    type = type,
                                    walletId = "",
                                    sourceWalletId = sourceWalletId ?: "",
                                    destinationWalletId = destinationWalletId ?: "",
                                    transactionDate = transactionDate
                                )
                            }
                        }

                        transactionRepository.updateTransaction(updatedTransaction)
                            .onSuccess {
                                // Apply new transaction's wallet effects
                                when (type) {
                                    TransactionType.INCOME, TransactionType.EXPENSE -> {
                                        walletId?.let { updateWalletBalance(it, updatedTransaction.amount) }
                                    }
                                    TransactionType.TRANSFER -> {
                                        sourceWalletId?.let { updateWalletBalance(it, -abs(amount)) }
                                        destinationWalletId?.let { updateWalletBalance(it, abs(amount)) }
                                    }
                                }
                            }
                            .onFailure { error ->
                                _uiState.update {
                                    it.copy(isLoading = false, error = error.message)
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
    }

    fun deleteTransaction(transactionId: String) {
        if (userId.isEmpty()) {
            _uiState.update { it.copy(error = "User not authenticated") }
            return
        }
        
        if (transactionId.isBlank()) {
            _uiState.update { it.copy(error = "Invalid transaction ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Get the transaction first to reverse wallet effects
                val transactionResult = transactionRepository.getTransactionById(transactionId)
                if (transactionResult.isFailure) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to find transaction: ${transactionResult.exceptionOrNull()?.message}")
                    }
                    return@launch
                }

                val transaction = transactionResult.getOrNull()
                if (transaction == null) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Transaction not found")
                    }
                    return@launch
                }

                // Reverse wallet effects
                when (transaction.type) {
                    TransactionType.INCOME, TransactionType.EXPENSE -> {
                        // Handle both old single wallet and new multiple wallets format
                        val walletsToReverse = if (transaction.affectedWalletIds.isNotEmpty()) {
                            transaction.affectedWalletIds
                        } else {
                            listOf(transaction.walletId)
                        }
                        
                        for (walletId in walletsToReverse) {
                            val result = reverseWalletBalance(walletId, -transaction.amount)
                            if (result.isFailure) {
                                _uiState.update {
                                    it.copy(isLoading = false, error = "Failed to update wallet balance: ${result.exceptionOrNull()?.message}")
                                }
                                return@launch
                            }
                        }
                    }
                    TransactionType.TRANSFER -> {
                        val sourceResult = reverseWalletBalance(transaction.sourceWalletId, abs(transaction.amount))
                        val destResult = reverseWalletBalance(transaction.destinationWalletId, -abs(transaction.amount))
                        if (sourceResult.isFailure || destResult.isFailure) {
                            _uiState.update {
                                it.copy(isLoading = false, error = "Failed to update wallet balances")
                            }
                            return@launch
                        }
                    }
                }

                // Delete the transaction
                transactionRepository.deleteTransaction(transactionId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(isLoading = false, error = null)
                        }
                        
                        // Force a refresh to ensure UI updates
                        refreshData()
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(isLoading = false, error = "Failed to delete transaction: ${error.message}")
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Delete failed: ${e.message}")
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

    private suspend fun reverseWalletBalance(walletId: String, amount: Double): Result<Unit> {
        return try {
            val walletResult = walletRepository.getWalletById(walletId)
            if (walletResult.isFailure) {
                return Result.failure(walletResult.exceptionOrNull() ?: Exception("Failed to get wallet"))
            }

            val wallet = walletResult.getOrNull()
            if (wallet == null) {
                return Result.failure(Exception("Wallet not found"))
            }

            val updatedWallet = wallet.copy(balance = wallet.balance + amount)
            val updateResult = walletRepository.updateWallet(updatedWallet)
            
            if (updateResult.isFailure) {
                Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update wallet"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refreshData() {
        if (userId.isNotEmpty()) {
            loadData()
        }
    }
    
    fun getTagSuggestions(input: String): List<String> {
        if (input.length < UIConstants.TAG_AUTOCOMPLETE_MIN_CHARS) return emptyList()

        return uiState.value.availableTags.filter {
            it.contains(input, ignoreCase = true)
        }.take(UIConstants.TAG_SUGGESTION_LIMIT)
    }
}
package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import com.axeven.profiteerapp.utils.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""

    init {
        if (userId.isNotEmpty()) {
            loadTransactionData()
        }
    }

    private fun loadTransactionData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            logger.d("TransactionListViewModel", "Starting loadTransactionData for user: $userId")

            try {
                combine(
                    transactionRepository.getUserTransactionsForCalculations(userId), // Get all transactions without date filters
                    walletRepository.getUserWallets(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { transactions, wallets, preferences ->
                    Triple(transactions, wallets, preferences)
                }.collect { (transactions, wallets, preferences) ->
                    logger.d("TransactionListViewModel", "Data received - transactions: ${transactions.size}, wallets: ${wallets.size}")
                    
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    
                    // Sort transactions by date descending (newest first)
                    val sortedTransactions = transactions.sortedByDescending { 
                        it.transactionDate ?: it.createdAt ?: Date(0) 
                    }
                    
                    // Extract all unique tags from transactions
                    val availableTags = extractAvailableTags(transactions)
                    
                    // Group transactions by date
                    val groupedTransactions = groupTransactionsByDate(sortedTransactions)
                    
                    _uiState.update {
                        it.copy(
                            transactions = sortedTransactions,
                            groupedTransactions = groupedTransactions,
                            wallets = wallets,
                            availableTags = availableTags,
                            defaultCurrency = defaultCurrency,
                            isLoading = false,
                            error = null,
                            // Reset expanded groups to show first few groups by default
                            expandedGroups = groupedTransactions.keys.take(3).toSet()
                        )
                    }
                    
                    logger.d("TransactionListViewModel", "UI state updated - transactions: ${sortedTransactions.size}")
                }
            } catch (e: Exception) {
                logger.e("TransactionListViewModel", "Error loading transaction data", e)
                
                val errorMessage = when {
                    e.message?.contains("index", ignoreCase = true) == true -> 
                        "Database setup issue. Please try again later."
                    e.message?.contains("FAILED_PRECONDITION", ignoreCase = true) == true ->
                        "Database configuration issue. Please restart the app."
                    else -> e.message ?: "Failed to load transactions"
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    private fun extractAvailableTags(transactions: List<Transaction>): List<String> {
        val allTags = mutableSetOf<String>()
        
        transactions.forEach { transaction ->
            // Use tags field, fallback to category for backward compatibility
            val tags = if (transaction.tags.isNotEmpty()) {
                transaction.tags
            } else if (transaction.category.isNotBlank() && transaction.category != "Untagged") {
                listOf(transaction.category)
            } else {
                emptyList()
            }
            
            allTags.addAll(tags)
        }
        
        return allTags.sorted()
    }

    private fun groupTransactionsByDate(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormatter = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        
        return transactions.groupBy { transaction ->
            val date = transaction.transactionDate ?: transaction.createdAt ?: Date(0)
            dateFormatter.format(date)
        }.mapKeys { (dateKey, _) ->
            try {
                val date = dateFormatter.parse(dateKey) ?: Date(0)
                displayDateFormatter.format(date)
            } catch (e: Exception) {
                "Unknown Date"
            }
        }.toSortedMap { date1, date2 ->
            // Sort by date descending (newest first)
            try {
                val d1 = displayDateFormatter.parse(date1) ?: Date(0)
                val d2 = displayDateFormatter.parse(date2) ?: Date(0)
                d2.compareTo(d1)
            } catch (e: Exception) {
                date2.compareTo(date1)
            }
        }
    }

    fun toggleGroupExpansion(groupKey: String) {
        val currentExpandedGroups = _uiState.value.expandedGroups
        val newExpandedGroups = if (currentExpandedGroups.contains(groupKey)) {
            currentExpandedGroups - groupKey
        } else {
            currentExpandedGroups + groupKey
        }
        
        _uiState.update { it.copy(expandedGroups = newExpandedGroups) }
    }

    fun setDateRangeFilter(startDate: Date?, endDate: Date?) {
        _uiState.update { 
            it.copy(selectedDateRange = Pair(startDate, endDate))
        }
        applyFilters()
    }

    fun togglePhysicalWalletFilter(walletId: String) {
        val currentSelection = _uiState.value.selectedPhysicalWallets
        val newSelection = if (currentSelection.contains(walletId)) {
            currentSelection - walletId
        } else {
            currentSelection + walletId
        }
        
        _uiState.update { it.copy(selectedPhysicalWallets = newSelection) }
        applyFilters()
    }

    fun toggleLogicalWalletFilter(walletId: String) {
        val currentSelection = _uiState.value.selectedLogicalWallets
        val newSelection = if (currentSelection.contains(walletId)) {
            currentSelection - walletId
        } else {
            currentSelection + walletId
        }
        
        _uiState.update { it.copy(selectedLogicalWallets = newSelection) }
        applyFilters()
    }

    fun toggleTagFilter(tag: String) {
        val currentSelection = _uiState.value.selectedTags
        val newSelection = if (currentSelection.contains(tag)) {
            currentSelection - tag
        } else {
            currentSelection + tag
        }
        
        _uiState.update { it.copy(selectedTags = newSelection) }
        applyFilters()
    }

    private fun applyFilters() {
        // This will be called whenever filters change to update the displayed transactions
        viewModelScope.launch {
            val currentState = _uiState.value
            val allTransactions = currentState.transactions
            
            // Apply all active filters
            val filteredTransactions = allTransactions.filter { transaction ->
                // Date range filter
                val dateInRange = if (currentState.selectedDateRange.first != null || currentState.selectedDateRange.second != null) {
                    val transactionDate = transaction.transactionDate ?: transaction.createdAt ?: Date(0)
                    val startDate = currentState.selectedDateRange.first
                    val endDate = currentState.selectedDateRange.second
                    
                    val afterStart = startDate == null || transactionDate.time >= startDate.time
                    val beforeEnd = endDate == null || transactionDate.time <= endDate.time
                    
                    afterStart && beforeEnd
                } else {
                    true
                }
                
                // Physical wallet filter
                val physicalWalletMatch = if (currentState.selectedPhysicalWallets.isNotEmpty()) {
                    val walletIds = transaction.affectedWalletIds.ifEmpty { listOf(transaction.walletId) } +
                            listOfNotNull(transaction.sourceWalletId, transaction.destinationWalletId).filter { it.isNotBlank() }
                    
                    val physicalWallets = currentState.wallets.filter { it.walletType == "Physical" }
                    walletIds.any { walletId ->
                        val wallet = physicalWallets.find { it.id == walletId }
                        wallet != null && currentState.selectedPhysicalWallets.contains(wallet.id)
                    }
                } else {
                    true
                }
                
                // Logical wallet filter
                val logicalWalletMatch = if (currentState.selectedLogicalWallets.isNotEmpty()) {
                    val walletIds = transaction.affectedWalletIds.ifEmpty { listOf(transaction.walletId) } +
                            listOfNotNull(transaction.sourceWalletId, transaction.destinationWalletId).filter { it.isNotBlank() }
                    
                    val logicalWallets = currentState.wallets.filter { it.walletType == "Logical" }
                    walletIds.any { walletId ->
                        val wallet = logicalWallets.find { it.id == walletId }
                        wallet != null && currentState.selectedLogicalWallets.contains(wallet.id)
                    }
                } else {
                    true
                }
                
                // Tag filter
                val tagMatch = if (currentState.selectedTags.isNotEmpty()) {
                    val transactionTags = if (transaction.tags.isNotEmpty()) {
                        transaction.tags
                    } else if (transaction.category.isNotBlank() && transaction.category != "Untagged") {
                        listOf(transaction.category)
                    } else {
                        emptyList()
                    }
                    
                    transactionTags.any { currentState.selectedTags.contains(it) }
                } else {
                    true
                }
                
                dateInRange && physicalWalletMatch && logicalWalletMatch && tagMatch
            }
            
            // Group filtered transactions
            val groupedFiltered = groupTransactionsByDate(filteredTransactions)
            
            _uiState.update {
                it.copy(groupedTransactions = groupedFiltered)
            }
        }
    }

    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                selectedDateRange = Pair(null, null),
                selectedPhysicalWallets = emptySet(),
                selectedLogicalWallets = emptySet(),
                selectedTags = emptySet()
            )
        }
        applyFilters()
    }

    fun refreshData() {
        if (userId.isNotEmpty()) {
            loadTransactionData()
        }
    }
}
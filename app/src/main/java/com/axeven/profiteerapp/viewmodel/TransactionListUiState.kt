package com.axeven.profiteerapp.viewmodel

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.Wallet
import java.util.Date

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val expandedGroups: Set<String> = emptySet(),
    val wallets: List<Wallet> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedDateRange: Pair<Date?, Date?> = Pair(null, null),
    val selectedPhysicalWallets: Set<String> = emptySet(),
    val selectedLogicalWallets: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val defaultCurrency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null
)
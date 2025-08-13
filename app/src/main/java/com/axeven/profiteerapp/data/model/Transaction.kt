package com.axeven.profiteerapp.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

data class Transaction(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Untagged",
    val type: TransactionType = TransactionType.EXPENSE,
    val walletId: String = "", // For backward compatibility - primary wallet for INCOME/EXPENSE
    val affectedWalletIds: List<String> = emptyList(), // Multiple wallets affected (physical + logical)
    val sourceWalletId: String = "", // For TRANSFER transactions (source)
    val destinationWalletId: String = "", // For TRANSFER transactions (destination)
    val tags: List<String> = emptyList(), // Transaction tags
    val transactionDate: Date? = null, // User-specified transaction date
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", 0.0, "Untagged", TransactionType.EXPENSE, "", emptyList(), "", "", emptyList(), null, "", null, null)
}
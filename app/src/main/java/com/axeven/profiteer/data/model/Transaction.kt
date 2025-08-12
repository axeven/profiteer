package com.axeven.profiteer.data.model

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
    val category: String = "Uncategorized",
    val type: TransactionType = TransactionType.EXPENSE,
    val walletId: String = "", // For INCOME/EXPENSE transactions
    val sourceWalletId: String = "", // For TRANSFER transactions (source)
    val destinationWalletId: String = "", // For TRANSFER transactions (destination)
    val transactionDate: Date? = null, // User-specified transaction date
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", 0.0, "Uncategorized", TransactionType.EXPENSE, "", "", "", null, "", null, null)
}
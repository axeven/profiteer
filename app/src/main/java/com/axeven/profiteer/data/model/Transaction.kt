package com.axeven.profiteer.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val walletId: String = "",
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", 0.0, "", TransactionType.EXPENSE, "", "", null, null)
}
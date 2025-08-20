package com.axeven.profiteerapp.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Wallet(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val balance: Double = 0.0,
    val initialBalance: Double = 0.0,
    val walletType: String = "Physical",
    val physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY,
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", 0.0, 0.0, "Physical", PhysicalForm.FIAT_CURRENCY, "", null, null)
    
    /**
     * Returns the transaction-based balance (current balance minus initial balance).
     * This represents the net change from transactions only and is used for analytics
     * to exclude the initial balance setup.
     */
    @get:Exclude
    val transactionBalance: Double
        get() = balance - initialBalance
}
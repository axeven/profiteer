package com.axeven.profiteer.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Wallet(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val currency: String = "",
    val balance: Double = 0.0,
    val walletType: String = "Physical",
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", "", 0.0, "Physical", "", null, null)
}
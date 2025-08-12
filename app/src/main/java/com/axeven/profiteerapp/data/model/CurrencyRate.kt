package com.axeven.profiteerapp.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CurrencyRate(
    @DocumentId
    val id: String = "",
    val fromCurrency: String = "",
    val toCurrency: String = "",
    val rate: Double = 0.0,
    val month: String? = null, // null means default rate for all times
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", "", 0.0, null, "", null, null)
}
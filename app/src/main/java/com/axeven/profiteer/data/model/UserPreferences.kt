package com.axeven.profiteer.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserPreferences(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val defaultCurrency: String = "USD",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", "USD", null, null)
}
package com.axeven.profiteerapp.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserPreferences(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val defaultCurrency: String = "USD", // Used for all wallet operations
    val displayCurrency: String = "USD", // Used for balance display conversion
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", "USD", "USD", null, null)
}
package com.axeven.profiteerapp.data.model

import com.axeven.profiteerapp.data.constants.RepositoryConstants
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserPreferences(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val defaultCurrency: String = RepositoryConstants.DEFAULT_CURRENCY, // Used for all wallet operations
    val displayCurrency: String = RepositoryConstants.DEFAULT_CURRENCY, // Used for balance display conversion
    val tagsMigrationCompleted: Boolean = false, // Flag to track if tag normalization migration has been run
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", RepositoryConstants.DEFAULT_CURRENCY, RepositoryConstants.DEFAULT_CURRENCY, false, null, null)
}
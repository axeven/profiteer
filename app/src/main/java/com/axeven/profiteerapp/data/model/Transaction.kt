package com.axeven.profiteerapp.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

/**
 * Transaction data model representing income, expense, or transfer transactions.
 *
 * Tags Normalization (Implemented 2025-10-19):
 * - All tags are automatically normalized to lowercase, trimmed, and deduplicated
 * - Tags are normalized at input time (CreateTransactionUiState, EditTransactionUiState)
 * - Existing Firestore data can be migrated using TagMigration utility
 * - Reserved keyword "Untagged" is filtered out during normalization
 * - See: TagNormalizer.kt for normalization implementation
 */
data class Transaction(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Untagged", // Legacy field, use tags instead
    val type: TransactionType = TransactionType.EXPENSE,
    val walletId: String = "", // For backward compatibility - primary wallet for INCOME/EXPENSE
    val affectedWalletIds: List<String> = emptyList(), // Multiple wallets affected (physical + logical)
    val sourceWalletId: String = "", // For TRANSFER transactions (source)
    val destinationWalletId: String = "", // For TRANSFER transactions (destination)

    /**
     * Transaction tags - Automatically normalized (lowercase, trimmed, deduplicated).
     *
     * Normalization is applied:
     * - On transaction creation (CreateTransactionUiState)
     * - On transaction editing (EditTransactionUiState)
     * - When loading from Firestore (fromExistingTransaction)
     * - During data migration (TagMigration)
     *
     * Reserved keyword "Untagged" is filtered out.
     * See: TagNormalizer.kt for implementation details.
     */
    val tags: List<String> = emptyList(),

    val transactionDate: Date? = null, // User-specified transaction date
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", 0.0, "Untagged", TransactionType.EXPENSE, "", emptyList(), "", "", emptyList(), null, "", null, null)
}
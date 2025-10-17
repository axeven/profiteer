package com.axeven.profiteerapp.data.model

import com.axeven.profiteerapp.data.constants.WalletType
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
    val walletType: String = WalletType.PHYSICAL.displayName, // Keep String for Firebase compatibility
    val physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY,
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", 0.0, 0.0, WalletType.PHYSICAL.displayName, PhysicalForm.FIAT_CURRENCY, "", null, null)

    /**
     * Returns the transaction-based balance (current balance minus initial balance).
     * This represents the net change from transactions only and is used for analytics
     * to exclude the initial balance setup.
     */
    @get:Exclude
    val transactionBalance: Double
        get() = balance - initialBalance

    /**
     * Type-safe enum property for wallet type.
     * Provides backward-compatible access to walletType string via enum.
     */
    @get:Exclude
    val type: WalletType
        get() = WalletType.fromString(walletType) ?: WalletType.PHYSICAL

    /**
     * Convenience check for physical wallet type.
     */
    @get:Exclude
    val isPhysical: Boolean
        get() = type == WalletType.PHYSICAL

    /**
     * Convenience check for logical wallet type.
     */
    @get:Exclude
    val isLogical: Boolean
        get() = type == WalletType.LOGICAL
}
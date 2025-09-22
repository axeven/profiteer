package com.axeven.profiteerapp.data.ui

import com.axeven.profiteerapp.data.model.Wallet

/**
 * Manages wallet selection state for transaction creation.
 *
 * This consolidates the scattered wallet selection state into a single object
 * with convenience methods for validation and management.
 *
 * Supports both regular transactions (physical + logical wallets) and
 * transfer transactions (source + destination wallets).
 */
data class SelectedWallets(
    val physical: Wallet? = null,
    val logical: Wallet? = null,
    val source: Wallet? = null,
    val destination: Wallet? = null
) {

    /**
     * Gets all selected wallets for regular transactions (income/expense).
     * Returns list of non-null physical and logical wallets.
     */
    val allSelected: List<Wallet>
        get() = listOfNotNull(physical, logical)

    /**
     * Gets all affected wallets for transfer transactions.
     * Returns list of non-null source and destination wallets.
     */
    val transferWallets: List<Wallet>
        get() = listOfNotNull(source, destination)

    /**
     * Gets all wallets regardless of transaction type.
     * Useful for comprehensive validation and display.
     */
    val allWallets: List<Wallet>
        get() = listOfNotNull(physical, logical, source, destination).distinct()

    /**
     * Checks if wallet selection is valid for regular transactions.
     * At least one wallet (physical or logical) must be selected.
     */
    val isValidForTransaction: Boolean
        get() = allSelected.isNotEmpty()

    /**
     * Checks if wallet selection is valid for transfer transactions.
     * Both source and destination wallets must be selected and different.
     */
    val isValidForTransfer: Boolean
        get() = source != null && destination != null && source != destination

    /**
     * Gets the total balance of all selected wallets.
     * Useful for validation and display purposes.
     */
    val totalBalance: Double
        get() = allWallets.sumOf { it.balance }

    /**
     * Gets all unique currencies from selected wallets.
     * Useful for currency validation and conversion logic.
     * TODO: Implement when currency support is added to Wallet model
     */
    val currencies: Set<String>
        get() = emptySet() // allWallets.map { it.currency }.toSet()

    /**
     * Checks if all selected wallets use the same currency.
     * Required for transfer transactions.
     * TODO: Implement when currency support is added to Wallet model
     */
    val hasSameCurrency: Boolean
        get() = true // currencies.size <= 1

    /**
     * Updates physical wallet selection.
     */
    fun updatePhysical(wallet: Wallet?): SelectedWallets {
        return copy(physical = wallet)
    }

    /**
     * Updates logical wallet selection.
     */
    fun updateLogical(wallet: Wallet?): SelectedWallets {
        return copy(logical = wallet)
    }

    /**
     * Updates source wallet for transfers.
     */
    fun updateSource(wallet: Wallet?): SelectedWallets {
        return copy(source = wallet)
    }

    /**
     * Updates destination wallet for transfers.
     */
    fun updateDestination(wallet: Wallet?): SelectedWallets {
        return copy(destination = wallet)
    }

    /**
     * Clears all wallet selections.
     */
    fun clearAll(): SelectedWallets {
        return SelectedWallets()
    }

    /**
     * Clears only transfer-related wallets (source and destination).
     * Keeps physical and logical wallet selections for when switching
     * from transfer back to income/expense.
     */
    fun clearTransferWallets(): SelectedWallets {
        return copy(source = null, destination = null)
    }

    /**
     * Clears only regular transaction wallets (physical and logical).
     * Keeps transfer wallets for when switching from income/expense to transfer.
     */
    fun clearRegularWallets(): SelectedWallets {
        return copy(physical = null, logical = null)
    }

    /**
     * Validates transfer wallet compatibility.
     * Returns validation message if invalid, null if valid.
     */
    fun validateTransferCompatibility(): String? {
        return when {
            source == null -> "Source wallet is required"
            destination == null -> "Destination wallet is required"
            source == destination -> "Source and destination wallets must be different"
            source!!.walletType != destination!!.walletType ->
                "Source and destination wallets must be the same type (both Physical or both Logical)"
            // TODO: Add currency validation when currency support is added
            false -> "Source and destination wallets must use the same currency"
            else -> null
        }
    }

    /**
     * Gets wallet by type for convenience.
     */
    fun getWalletByType(walletType: String): Wallet? {
        return allWallets.firstOrNull { it.walletType == walletType }
    }

    /**
     * Checks if a specific wallet is selected in any capacity.
     */
    fun isWalletSelected(wallet: Wallet): Boolean {
        return allWallets.any { it.id == wallet.id }
    }

    /**
     * Gets the role of a selected wallet (physical, logical, source, destination).
     * Returns list because a wallet could theoretically serve multiple roles.
     */
    fun getWalletRoles(wallet: Wallet): List<WalletRole> {
        val roles = mutableListOf<WalletRole>()

        if (physical?.id == wallet.id) roles.add(WalletRole.PHYSICAL)
        if (logical?.id == wallet.id) roles.add(WalletRole.LOGICAL)
        if (source?.id == wallet.id) roles.add(WalletRole.SOURCE)
        if (destination?.id == wallet.id) roles.add(WalletRole.DESTINATION)

        return roles
    }

    /**
     * Creates a copy with pre-selected wallet based on wallet type.
     * Useful for handling pre-selection from navigation parameters.
     */
    fun withPreSelected(wallet: Wallet): SelectedWallets {
        return when (wallet.walletType) {
            "Physical" -> copy(physical = wallet)
            "Logical" -> copy(logical = wallet)
            else -> this
        }
    }
}

/**
 * Enum representing the different roles a wallet can have in a transaction.
 */
enum class WalletRole {
    PHYSICAL,
    LOGICAL,
    SOURCE,
    DESTINATION
}

/**
 * Extension function to get wallet display name with role context.
 */
fun Wallet.displayNameWithRole(role: WalletRole): String {
    val roleText = when (role) {
        WalletRole.PHYSICAL -> "Physical"
        WalletRole.LOGICAL -> "Logical"
        WalletRole.SOURCE -> "Source"
        WalletRole.DESTINATION -> "Destination"
    }
    return "$name ($roleText)"
}
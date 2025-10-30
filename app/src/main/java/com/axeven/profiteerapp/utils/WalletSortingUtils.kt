package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.Wallet

/**
 * Utility object for sorting wallet lists in various ways.
 *
 * Provides two main sorting strategies:
 * 1. Alphabetical sorting by wallet name (case-insensitive)
 * 2. Type-based grouping with alphabetical sorting within each group
 *
 * All sorting methods return new lists and do not modify the original list (immutable).
 *
 * Usage:
 * ```
 * // Sort wallets alphabetically
 * val sortedWallets = WalletSortingUtils.sortAlphabetically(wallets)
 *
 * // Sort by type (Physical first), then alphabetically
 * val groupedWallets = WalletSortingUtils.sortByTypeAndName(wallets)
 *
 * // Sort by type (Logical first), then alphabetically
 * val groupedWallets = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = false)
 * ```
 */
object WalletSortingUtils {

    /**
     * Sorts wallets alphabetically by name (case-insensitive).
     *
     * This method performs a stable sort, meaning that wallets with identical names
     * (when compared case-insensitively) will maintain their relative order from the
     * original list.
     *
     * Special characters and numbers sort according to Unicode ordering:
     * - Numbers (0-9) sort before letters
     * - Special characters (!@#$%) sort before letters
     * - Letters are sorted alphabetically, case-insensitively
     *
     * Examples:
     * - Input: ["Zebra", "Apple", "Mango", "Banana"]
     * - Output: ["Apple", "Banana", "Mango", "Zebra"]
     *
     * - Input: ["apple", "Banana", "CHERRY"]
     * - Output: ["apple", "Banana", "CHERRY"] (case-insensitive: a < b < c)
     *
     * - Input: ["Zoo", "1stWallet", "!Special"]
     * - Output: ["!Special", "1stWallet", "Zoo"] (special chars and numbers before letters)
     *
     * @param wallets The list of wallets to sort
     * @return A new list containing the same wallets sorted alphabetically by name.
     *         The original list is not modified.
     *
     * @see sortByTypeAndName for sorting with type grouping
     */
    fun sortAlphabetically(wallets: List<Wallet>): List<Wallet> {
        return wallets.sortedBy { it.name.lowercase() }
    }

    /**
     * Sorts wallets by type first, then alphabetically by name within each type group.
     *
     * This method groups wallets into three categories:
     * 1. Physical wallets - walletType == "Physical"
     * 2. Logical wallets - walletType == "Logical"
     * 3. Unknown types - any other walletType value
     *
     * Within each group, wallets are sorted alphabetically by name (case-insensitive).
     *
     * The order of groups depends on the `physicalFirst` parameter:
     * - If `physicalFirst = true` (default): Physical → Logical → Unknown
     * - If `physicalFirst = false`: Logical → Physical → Unknown
     *
     * Unknown types always appear last, regardless of the `physicalFirst` parameter.
     *
     * Examples:
     * - Input: ["Yellow (Logical)", "Apple (Physical)", "Zebra (Physical)", "Blue (Logical)"]
     * - Output (physicalFirst = true): ["Apple (Physical)", "Zebra (Physical)", "Blue (Logical)", "Yellow (Logical)"]
     * - Output (physicalFirst = false): ["Blue (Logical)", "Yellow (Logical)", "Apple (Physical)", "Zebra (Physical)"]
     *
     * Use Cases:
     * - Transfer transaction wallet selectors (show all wallets grouped by type)
     * - Wallet management screens where type distinction is important
     * - Any UI where users need to see Physical and Logical wallets separately
     *
     * @param wallets The list of wallets to sort
     * @param physicalFirst If true, Physical wallets appear before Logical wallets.
     *                      If false, Logical wallets appear before Physical wallets.
     *                      Defaults to true.
     * @return A new list containing the same wallets sorted by type then name.
     *         The original list is not modified.
     *
     * @see sortAlphabetically for simple alphabetical sorting without type grouping
     */
    fun sortByTypeAndName(
        wallets: List<Wallet>,
        physicalFirst: Boolean = true
    ): List<Wallet> {
        return wallets.sortedWith(
            compareBy<Wallet> { wallet ->
                // First, sort by wallet type (Physical/Logical/Unknown)
                when (wallet.walletType) {
                    "Physical" -> if (physicalFirst) 0 else 1
                    "Logical" -> if (physicalFirst) 1 else 0
                    else -> 2 // Unknown types always appear last
                }
            }.thenBy { wallet ->
                // Then, sort alphabetically by name (case-insensitive) within each type group
                wallet.name.lowercase()
            }
        )
    }
}

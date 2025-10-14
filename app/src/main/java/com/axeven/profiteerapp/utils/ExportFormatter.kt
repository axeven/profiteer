package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Formats transaction data for export to Google Sheets.
 *
 * This utility class transforms Transaction objects into spreadsheet-friendly format
 * with proper column ordering, wallet name resolution, and data sanitization.
 *
 * @property wallets Map of wallet IDs to Wallet objects for name resolution
 * @property currency The currency code to use for exported transactions (e.g., "USD")
 */
class ExportFormatter(
    private val wallets: Map<String, Wallet>,
    private val currency: String
) {

    /**
     * Date format for export consistency (ISO 8601 date format).
     */
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Cache for wallet name lookups to improve performance.
     * Key: Wallet ID, Value: Wallet Name
     */
    private val walletNameCache = mutableMapOf<String, String>()

    /**
     * Creates the header row for the export spreadsheet.
     *
     * Column order matches the specification in the transaction export plan:
     * 1. Date - Transaction date
     * 2. Title - Transaction title/description
     * 3. Type - INCOME/EXPENSE/TRANSFER
     * 4. Amount - Transaction amount
     * 5. Currency - Currency code
     * 6. Physical Wallet - Physical wallet name (for INCOME/EXPENSE)
     * 7. Logical Wallet - Logical wallet name (for INCOME/EXPENSE)
     * 8. Tags - Comma-separated tag list
     * 9. Notes - Additional notes/description
     * 10. Source Wallet - Source wallet name (for TRANSFER only)
     * 11. Destination Wallet - Destination wallet name (for TRANSFER only)
     *
     * @return List of column header strings
     */
    fun createHeaderRow(): List<String> {
        return listOf(
            "Date",
            "Title",
            "Type",
            "Amount",
            "Currency",
            "Physical Wallet",
            "Logical Wallet",
            "Tags",
            "Notes",
            "Source Wallet",
            "Destination Wallet"
        )
    }

    /**
     * Formats a single transaction into a spreadsheet row.
     *
     * Handles three transaction types differently:
     * - INCOME/EXPENSE: Uses Physical and Logical wallet columns, leaves Source/Destination empty
     * - TRANSFER: Uses Source and Destination wallet columns, leaves Physical/Logical empty
     *
     * @param transaction The transaction to format
     * @return List of strings representing the row data (11 columns)
     */
    fun formatTransactionRow(transaction: Transaction): List<String> {
        // Format date (ISO 8601 format: yyyy-MM-dd)
        val dateStr = transaction.transactionDate?.let { dateFormat.format(it) } ?: ""

        // Format tags (comma-separated, or "Untagged" if empty)
        val tagsStr = if (transaction.tags.isEmpty()) {
            "Untagged"
        } else {
            transaction.tags.joinToString(", ")
        }

        // Determine wallet information based on transaction type
        val (physicalWallet, logicalWallet, sourceWallet, destinationWallet) = when (transaction.type) {
            TransactionType.INCOME, TransactionType.EXPENSE -> {
                // For INCOME/EXPENSE: Use affected wallets to find Physical and Logical
                val physicalWalletId = transaction.affectedWalletIds.firstOrNull { walletId ->
                    wallets[walletId]?.walletType == "Physical"
                } ?: transaction.walletId

                val logicalWalletId = transaction.affectedWalletIds.firstOrNull { walletId ->
                    wallets[walletId]?.walletType == "Logical"
                }

                val physical = resolveWalletName(physicalWalletId)
                val logical = logicalWalletId?.let { resolveWalletName(it) } ?: ""

                // Source and Destination are empty for non-transfer transactions
                Tuple4(physical, logical, "", "")
            }

            TransactionType.TRANSFER -> {
                // For TRANSFER: Use source and destination wallets
                val source = resolveWalletName(transaction.sourceWalletId)
                val destination = resolveWalletName(transaction.destinationWalletId)

                // Physical and Logical are empty for transfer transactions
                Tuple4("", "", source, destination)
            }
        }

        return listOf(
            dateStr,                          // 0. Date
            transaction.title,                // 1. Title
            transaction.type.name,            // 2. Type (INCOME/EXPENSE/TRANSFER)
            transaction.amount.toString(),    // 3. Amount
            currency,                         // 4. Currency
            physicalWallet,                   // 5. Physical Wallet
            logicalWallet,                    // 6. Logical Wallet
            tagsStr,                          // 7. Tags
            "",                               // 8. Notes (not currently stored in Transaction model)
            sourceWallet,                     // 9. Source Wallet (for transfers)
            destinationWallet                 // 10. Destination Wallet (for transfers)
        )
    }

    /**
     * Formats a list of transactions into a complete spreadsheet data structure.
     *
     * The first row will always be the header row, followed by one row per transaction.
     * Transaction order is preserved from the input list.
     *
     * @param transactions List of transactions to format
     * @return List of rows, where each row is a list of strings (columns)
     */
    fun formatTransactionList(transactions: List<Transaction>): List<List<String>> {
        val result = mutableListOf<List<String>>()

        // Add header row first
        result.add(createHeaderRow())

        // Add data rows for each transaction
        transactions.forEach { transaction ->
            result.add(formatTransactionRow(transaction))
        }

        return result
    }

    /**
     * Resolves a wallet ID to its display name.
     *
     * Uses caching to improve performance for repeated lookups.
     * Returns empty string if wallet ID is invalid or not found.
     *
     * @param walletId The wallet ID to resolve
     * @return The wallet name, or empty string if not found
     */
    fun resolveWalletName(walletId: String): String {
        if (walletId.isEmpty()) {
            return ""
        }

        // Check cache first
        walletNameCache[walletId]?.let { return it }

        // Look up wallet name
        val walletName = wallets[walletId]?.name ?: ""

        // Cache the result (including empty results to avoid repeated lookups)
        walletNameCache[walletId] = walletName

        return walletName
    }

    /**
     * Helper data class to hold four wallet-related values.
     * Used internally for tuple-like return values.
     */
    private data class Tuple4<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}

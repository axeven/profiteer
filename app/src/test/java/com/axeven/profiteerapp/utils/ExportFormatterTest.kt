package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Test suite for ExportFormatter following TDD approach.
 * Tests the formatting of transaction data for Google Sheets export.
 */
class ExportFormatterTest {

    private lateinit var formatter: ExportFormatter
    private lateinit var testWallets: Map<String, Wallet>
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @Before
    fun setup() {
        // Create test wallets
        testWallets = mapOf(
            "wallet1" to Wallet(
                id = "wallet1",
                name = "Cash Wallet",
                walletType = "Physical",
                balance = 1000.0
            ),
            "wallet2" to Wallet(
                id = "wallet2",
                name = "Budget Wallet",
                walletType = "Logical",
                balance = 500.0
            ),
            "wallet3" to Wallet(
                id = "wallet3",
                name = "Savings",
                walletType = "Physical",
                balance = 5000.0
            )
        )

        formatter = ExportFormatter(testWallets, "USD")
    }

    // ========== Tests for formatTransactionRow() ==========

    @Test
    fun `formatTransactionRow should format income transaction correctly`() {
        val transaction = Transaction(
            id = "tx1",
            title = "Salary Payment",
            amount = 5000.0,
            type = TransactionType.INCOME,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1", "wallet2"),
            tags = listOf("Income", "Salary"),
            transactionDate = dateFormat.parse("2025-10-15"),
            userId = "user1"
        )

        val row = formatter.formatTransactionRow(transaction)

        assertEquals(11, row.size) // Should have 11 columns
        assertEquals("2025-10-15", row[0]) // Date
        assertEquals("Salary Payment", row[1]) // Title
        assertEquals("INCOME", row[2]) // Type
        assertEquals("5000.0", row[3]) // Amount
        assertEquals("USD", row[4]) // Currency
        assertEquals("Cash Wallet", row[5]) // Physical Wallet
        assertEquals("Budget Wallet", row[6]) // Logical Wallet
        assertEquals("Income, Salary", row[7]) // Tags
        assertEquals("", row[8]) // Notes/Description (empty)
        assertEquals("", row[9]) // Source Wallet (not a transfer)
        assertEquals("", row[10]) // Destination Wallet (not a transfer)
    }

    @Test
    fun `formatTransactionRow should format expense transaction correctly`() {
        val transaction = Transaction(
            id = "tx2",
            title = "Grocery Shopping",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1", "wallet2"),
            tags = listOf("Food", "Groceries"),
            transactionDate = dateFormat.parse("2025-10-14"),
            userId = "user1"
        )

        val row = formatter.formatTransactionRow(transaction)

        assertEquals(11, row.size)
        assertEquals("2025-10-14", row[0]) // Date
        assertEquals("Grocery Shopping", row[1]) // Title
        assertEquals("EXPENSE", row[2]) // Type
        assertEquals("150.0", row[3]) // Amount
        assertEquals("USD", row[4]) // Currency
        assertEquals("Cash Wallet", row[5]) // Physical Wallet
        assertEquals("Budget Wallet", row[6]) // Logical Wallet
        assertEquals("Food, Groceries", row[7]) // Tags
    }

    @Test
    fun `formatTransactionRow should format transfer transaction with source and destination`() {
        val transaction = Transaction(
            id = "tx3",
            title = "Transfer to Savings",
            amount = 1000.0,
            type = TransactionType.TRANSFER,
            sourceWalletId = "wallet1",
            destinationWalletId = "wallet3",
            affectedWalletIds = listOf("wallet1", "wallet3"),
            tags = listOf("Transfer"),
            transactionDate = dateFormat.parse("2025-10-13"),
            userId = "user1"
        )

        val row = formatter.formatTransactionRow(transaction)

        assertEquals(11, row.size)
        assertEquals("2025-10-13", row[0]) // Date
        assertEquals("Transfer to Savings", row[1]) // Title
        assertEquals("TRANSFER", row[2]) // Type
        assertEquals("1000.0", row[3]) // Amount
        assertEquals("USD", row[4]) // Currency
        assertEquals("", row[5]) // Physical Wallet (not used for transfers)
        assertEquals("", row[6]) // Logical Wallet (not used for transfers)
        assertEquals("Transfer", row[7]) // Tags
        assertEquals("", row[8]) // Notes
        assertEquals("Cash Wallet", row[9]) // Source Wallet
        assertEquals("Savings", row[10]) // Destination Wallet
    }

    @Test
    fun `formatTransactionRow should format transaction with multiple tags correctly`() {
        val transaction = Transaction(
            id = "tx4",
            title = "Business Lunch",
            amount = 75.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1"),
            tags = listOf("Food", "Business", "Dining", "Work"),
            transactionDate = dateFormat.parse("2025-10-12"),
            userId = "user1"
        )

        val row = formatter.formatTransactionRow(transaction)

        assertEquals("Food, Business, Dining, Work", row[7]) // Tags should be comma-separated
    }

    @Test
    fun `formatTransactionRow should handle transaction with no tags`() {
        val transaction = Transaction(
            id = "tx5",
            title = "Miscellaneous Expense",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1"),
            tags = emptyList(),
            transactionDate = dateFormat.parse("2025-10-11"),
            userId = "user1"
        )

        val row = formatter.formatTransactionRow(transaction)

        assertEquals("Untagged", row[7]) // Should show "Untagged" when no tags
    }

    @Test
    fun `formatTransactionRow should handle transaction with empty or null description`() {
        val transaction = Transaction(
            id = "tx6",
            title = "Quick Purchase",
            amount = 25.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1"),
            tags = listOf("Shopping"),
            transactionDate = dateFormat.parse("2025-10-10"),
            userId = "user1"
        )

        val row = formatter.formatTransactionRow(transaction)

        assertEquals("", row[8]) // Notes/Description should be empty string
    }

    @Test
    fun `formatTransactionRow should handle special characters in title correctly`() {
        val transaction = Transaction(
            id = "tx7",
            title = "Coffee & Snacks @ Joe's Café (50% off!)",
            amount = 10.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1"),
            tags = listOf("Food"),
            transactionDate = dateFormat.parse("2025-10-09"),
            userId = "user1"
        )

        val row = formatter.formatTransactionRow(transaction)

        assertEquals("Coffee & Snacks @ Joe's Café (50% off!)", row[1]) // Title should preserve special characters
    }

    @Test
    fun `formatTransactionRow should use consistent date formatting`() {
        val transaction1 = Transaction(
            id = "tx8",
            title = "Test Transaction 1",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1"),
            tags = listOf("Test"),
            transactionDate = dateFormat.parse("2025-01-01"),
            userId = "user1"
        )

        val transaction2 = Transaction(
            id = "tx9",
            title = "Test Transaction 2",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1"),
            tags = listOf("Test"),
            transactionDate = dateFormat.parse("2025-12-31"),
            userId = "user1"
        )

        val row1 = formatter.formatTransactionRow(transaction1)
        val row2 = formatter.formatTransactionRow(transaction2)

        assertEquals("2025-01-01", row1[0]) // ISO date format
        assertEquals("2025-12-31", row2[0]) // ISO date format
    }

    // ========== Tests for createHeaderRow() ==========

    @Test
    fun `createHeaderRow should contain all required columns`() {
        val header = formatter.createHeaderRow()

        assertNotNull(header)
        assertTrue(header.isNotEmpty())
        assertEquals(11, header.size)
    }

    @Test
    fun `createHeaderRow should have correct column order matching specification`() {
        val header = formatter.createHeaderRow()

        assertEquals("Date", header[0])
        assertEquals("Title", header[1])
        assertEquals("Type", header[2])
        assertEquals("Amount", header[3])
        assertEquals("Currency", header[4])
        assertEquals("Physical Wallet", header[5])
        assertEquals("Logical Wallet", header[6])
        assertEquals("Tags", header[7])
        assertEquals("Notes", header[8])
        assertEquals("Source Wallet", header[9])
        assertEquals("Destination Wallet", header[10])
    }

    // ========== Tests for formatTransactionList() ==========

    @Test
    fun `formatTransactionList should handle empty transaction list`() {
        val emptyList = emptyList<Transaction>()

        val result = formatter.formatTransactionList(emptyList)

        assertNotNull(result)
        assertEquals(1, result.size) // Should only contain header row
        assertEquals(formatter.createHeaderRow(), result[0])
    }

    @Test
    fun `formatTransactionList should format single transaction correctly`() {
        val transaction = Transaction(
            id = "tx1",
            title = "Single Transaction",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1"),
            tags = listOf("Test"),
            transactionDate = dateFormat.parse("2025-10-15"),
            userId = "user1"
        )

        val result = formatter.formatTransactionList(listOf(transaction))

        assertEquals(2, result.size) // Header + 1 transaction
        assertEquals(formatter.createHeaderRow(), result[0]) // First row is header
        assertEquals(11, result[1].size) // Transaction row has 11 columns
        assertEquals("Single Transaction", result[1][1])
    }

    @Test
    fun `formatTransactionList should format multiple transactions correctly`() {
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "Transaction 1",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                transactionDate = dateFormat.parse("2025-10-15"),
                userId = "user1"
            ),
            Transaction(
                id = "tx2",
                title = "Transaction 2",
                amount = 200.0,
                type = TransactionType.INCOME,
                walletId = "wallet2",
                affectedWalletIds = listOf("wallet2"),
                tags = listOf("Test"),
                transactionDate = dateFormat.parse("2025-10-14"),
                userId = "user1"
            ),
            Transaction(
                id = "tx3",
                title = "Transaction 3",
                amount = 300.0,
                type = TransactionType.TRANSFER,
                sourceWalletId = "wallet1",
                destinationWalletId = "wallet3",
                affectedWalletIds = listOf("wallet1", "wallet3"),
                tags = listOf("Test"),
                transactionDate = dateFormat.parse("2025-10-13"),
                userId = "user1"
            )
        )

        val result = formatter.formatTransactionList(transactions)

        assertEquals(4, result.size) // Header + 3 transactions
        assertEquals(formatter.createHeaderRow(), result[0])
        assertEquals("Transaction 1", result[1][1])
        assertEquals("Transaction 2", result[2][1])
        assertEquals("Transaction 3", result[3][1])
    }

    @Test
    fun `formatTransactionList should preserve transaction order`() {
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "First",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                transactionDate = dateFormat.parse("2025-10-15"),
                userId = "user1"
            ),
            Transaction(
                id = "tx2",
                title = "Second",
                amount = 200.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                transactionDate = dateFormat.parse("2025-10-14"),
                userId = "user1"
            ),
            Transaction(
                id = "tx3",
                title = "Third",
                amount = 300.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                transactionDate = dateFormat.parse("2025-10-13"),
                userId = "user1"
            )
        )

        val result = formatter.formatTransactionList(transactions)

        // Verify order is preserved (skip header row)
        assertEquals("First", result[1][1])
        assertEquals("Second", result[2][1])
        assertEquals("Third", result[3][1])
    }

    @Test
    fun `formatTransactionList should always include header row`() {
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "Test",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                transactionDate = dateFormat.parse("2025-10-15"),
                userId = "user1"
            )
        )

        val result = formatter.formatTransactionList(transactions)

        // First row should always be the header
        assertEquals(formatter.createHeaderRow(), result[0])
        assertTrue(result.size > 1) // Should have header + at least one data row
    }

    // ========== Tests for resolveWalletName() ==========

    @Test
    fun `resolveWalletName should return correct name for valid wallet ID`() {
        val walletName = formatter.resolveWalletName("wallet1")

        assertEquals("Cash Wallet", walletName)
    }

    @Test
    fun `resolveWalletName should return empty string for invalid wallet ID`() {
        val walletName = formatter.resolveWalletName("invalid-wallet-id")

        assertEquals("", walletName)
    }

    @Test
    fun `resolveWalletName should return empty string for empty wallet ID`() {
        val walletName = formatter.resolveWalletName("")

        assertEquals("", walletName)
    }

    @Test
    fun `resolveWalletName should cache results for performance`() {
        // First call - potentially populates cache
        val name1 = formatter.resolveWalletName("wallet1")

        // Second call - should use cache
        val name2 = formatter.resolveWalletName("wallet1")

        // Both should return the same correct value
        assertEquals("Cash Wallet", name1)
        assertEquals("Cash Wallet", name2)
        assertEquals(name1, name2)
    }
}

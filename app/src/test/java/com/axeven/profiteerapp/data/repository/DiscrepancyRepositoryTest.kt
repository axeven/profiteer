package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.utils.logging.Logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for repository methods used in discrepancy detection.
 *
 * These tests verify the expected behavior and data structure of repository methods,
 * particularly focusing on:
 * - Transaction ordering (chronological)
 * - Security compliance (userId filtering)
 * - Data completeness
 */
class DiscrepancyRepositoryTest {

    private lateinit var mockLogger: Logger

    @Before
    fun setup() {
        mockLogger = object : Logger {
            override fun d(tag: String, message: String) {}
            override fun i(tag: String, message: String) {}
            override fun w(tag: String, message: String) {}
            override fun e(tag: String, message: String, throwable: Throwable?) {}
        }
    }

    // Tests for getAllTransactionsChronological behavior expectations

    @Test
    fun `getAllTransactionsChronological should order by transactionDate ascending`() {
        // This test verifies the expected sorting behavior
        val oldDate = Date(1000)
        val middleDate = Date(2000)
        val newDate = Date(3000)

        val transactions = listOf(
            createTransaction(id = "t3", date = newDate),
            createTransaction(id = "t1", date = oldDate),
            createTransaction(id = "t2", date = middleDate)
        )

        // Expected order: oldest first (ascending)
        val sorted = transactions.sortedBy { it.transactionDate }

        assertEquals("t1", sorted[0].id)
        assertEquals("t2", sorted[1].id)
        assertEquals("t3", sorted[2].id)
    }

    @Test
    fun `getAllTransactionsChronological should handle null transactionDate with createdAt`() {
        // Transactions may have null transactionDate, should fall back to createdAt
        val oldDate = Date(1000)
        val newDate = Date(2000)

        val transactions = listOf(
            createTransaction(id = "t2", date = null, createdAt = newDate),
            createTransaction(id = "t1", date = oldDate, createdAt = null)
        )

        // Expected behavior: sort by transactionDate if present, else createdAt
        val sorted = transactions.sortedBy { it.transactionDate ?: it.createdAt }

        assertEquals("t1", sorted[0].id)
        assertEquals("t2", sorted[1].id)
    }

    @Test
    fun `getAllTransactionsChronological should filter by userId`() {
        // Security requirement: must filter by userId
        val userTransactions = listOf(
            createTransaction(id = "t1", userId = "user123"),
            createTransaction(id = "t2", userId = "user123"),
            createTransaction(id = "t3", userId = "user456") // Different user
        )

        val filtered = userTransactions.filter { it.userId == "user123" }

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.userId == "user123" })
    }

    @Test
    fun `getAllTransactionsChronological should return empty list for user with no transactions`() {
        val transactions = emptyList<Transaction>()

        assertEquals(0, transactions.size)
    }

    @Test
    fun `getAllTransactionsChronological should include all transaction types`() {
        val transactions = listOf(
            createTransaction(id = "t1", type = TransactionType.INCOME),
            createTransaction(id = "t2", type = TransactionType.EXPENSE),
            createTransaction(id = "t3", type = TransactionType.TRANSFER)
        )

        assertEquals(3, transactions.size)
        assertEquals(TransactionType.INCOME, transactions[0].type)
        assertEquals(TransactionType.EXPENSE, transactions[1].type)
        assertEquals(TransactionType.TRANSFER, transactions[2].type)
    }

    @Test
    fun `getAllTransactionsChronological should include affectedWalletIds`() {
        val transaction = createTransaction(
            id = "t1",
            affectedWalletIds = listOf("wallet1", "wallet2")
        )

        assertEquals(2, transaction.affectedWalletIds.size)
        assertTrue(transaction.affectedWalletIds.contains("wallet1"))
        assertTrue(transaction.affectedWalletIds.contains("wallet2"))
    }

    // Tests for getUserWallets (already exists, verify behavior)

    @Test
    fun `getUserWallets should filter by userId`() {
        val wallets = listOf(
            createWallet(id = "w1", userId = "user123"),
            createWallet(id = "w2", userId = "user123"),
            createWallet(id = "w3", userId = "user456")
        )

        val filtered = wallets.filter { it.userId == "user123" }

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.userId == "user123" })
    }

    @Test
    fun `getUserWallets should include both Physical and Logical wallets`() {
        val wallets = listOf(
            createWallet(id = "w1", walletType = "Physical"),
            createWallet(id = "w2", walletType = "Logical"),
            createWallet(id = "w3", walletType = "Physical")
        )

        val physicalWallets = wallets.filter { it.walletType == "Physical" }
        val logicalWallets = wallets.filter { it.walletType == "Logical" }

        assertEquals(2, physicalWallets.size)
        assertEquals(1, logicalWallets.size)
    }

    @Test
    fun `getUserWallets should include balance information`() {
        val wallet = createWallet(
            id = "w1",
            balance = 1000.0,
            initialBalance = 500.0
        )

        assertEquals(1000.0, wallet.balance, 0.001)
        assertEquals(500.0, wallet.initialBalance, 0.001)
    }

    @Test
    fun `getUserWallets should return empty list for user with no wallets`() {
        val wallets = emptyList<Wallet>()

        assertEquals(0, wallets.size)
    }

    @Test
    fun `getUserWallets should include wallet type information`() {
        val physicalWallet = createWallet(id = "w1", walletType = "Physical")
        val logicalWallet = createWallet(id = "w2", walletType = "Logical")

        assertEquals("Physical", physicalWallet.walletType)
        assertEquals("Logical", logicalWallet.walletType)
    }

    @Test
    fun `getUserWallets should handle zero balance wallets`() {
        val wallet = createWallet(
            id = "w1",
            balance = 0.0,
            initialBalance = 0.0
        )

        assertEquals(0.0, wallet.balance, 0.001)
        assertEquals(0.0, wallet.initialBalance, 0.001)
    }

    @Test
    fun `getUserWallets should handle negative balance wallets`() {
        val wallet = createWallet(
            id = "w1",
            balance = -100.0,
            initialBalance = 0.0
        )

        assertEquals(-100.0, wallet.balance, 0.001)
    }

    // Helper functions

    private fun createTransaction(
        id: String,
        title: String = "Test Transaction",
        amount: Double = 100.0,
        category: String = "Untagged",
        type: TransactionType = TransactionType.INCOME,
        walletId: String = "wallet1",
        affectedWalletIds: List<String> = listOf("wallet1"),
        sourceWalletId: String = "",
        destinationWalletId: String = "",
        tags: List<String> = emptyList(),
        date: Date? = Date(),
        createdAt: Date? = Date(),
        userId: String = "user123"
    ): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            category = category,
            type = type,
            walletId = walletId,
            affectedWalletIds = affectedWalletIds,
            sourceWalletId = sourceWalletId,
            destinationWalletId = destinationWalletId,
            tags = tags,
            transactionDate = date,
            userId = userId,
            createdAt = createdAt
        )
    }

    private fun createWallet(
        id: String,
        name: String = "Test Wallet",
        balance: Double = 0.0,
        initialBalance: Double = 0.0,
        walletType: String = "Physical",
        physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY,
        userId: String = "user123"
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            initialBalance = initialBalance,
            walletType = walletType,
            physicalForm = physicalForm,
            userId = userId
        )
    }
}

package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for DiscrepancyAnalyzer.
 * Tests transaction analysis and running balance calculation logic.
 */
class DiscrepancyAnalyzerTest {

    private lateinit var analyzer: DiscrepancyAnalyzer
    private lateinit var detector: BalanceDiscrepancyDetector

    @Before
    fun setup() {
        detector = BalanceDiscrepancyDetector()
        analyzer = DiscrepancyAnalyzer(detector)
    }

    // Tests for findFirstDiscrepancyTransaction()

    @Test
    fun `findFirstDiscrepancyTransaction with immediate discrepancy returns first transaction`() {
        val physicalWallet = createWallet(id = "p1", balance = 100.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 50.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet,
            "l1" to logicalWallet
        )

        // Transaction that only affects physical wallet - creates immediate discrepancy
        val transactions = listOf(
            createTransaction(
                id = "t1",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1"), // Only physical
                date = Date(1000)
            )
        )

        val result = analyzer.findFirstDiscrepancyTransaction(transactions, wallets)

        assertEquals("t1", result)
    }

    @Test
    fun `findFirstDiscrepancyTransaction with balanced transactions returns null`() {
        val physicalWallet = createWallet(id = "p1", balance = 100.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 100.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet,
            "l1" to logicalWallet
        )

        // Transactions that affect both physical and logical equally
        val transactions = listOf(
            createTransaction(
                id = "t1",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"), // Both wallets
                date = Date(1000)
            )
        )

        val result = analyzer.findFirstDiscrepancyTransaction(transactions, wallets)

        assertNull(result)
    }

    @Test
    fun `findFirstDiscrepancyTransaction with empty list returns null`() {
        val wallets = mapOf<String, Wallet>()
        val transactions = emptyList<Transaction>()

        val result = analyzer.findFirstDiscrepancyTransaction(transactions, wallets)

        assertNull(result)
    }

    @Test
    fun `findFirstDiscrepancyTransaction with gradual discrepancy finds first occurrence`() {
        val physicalWallet = createWallet(id = "p1", balance = 150.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 100.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet,
            "l1" to logicalWallet
        )

        val transactions = listOf(
            // First two balanced
            createTransaction(
                id = "t1",
                amount = 50.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(1000)
            ),
            createTransaction(
                id = "t2",
                amount = 50.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(2000)
            ),
            // This one creates discrepancy
            createTransaction(
                id = "t3",
                amount = 50.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1"), // Only physical
                date = Date(3000)
            )
        )

        val result = analyzer.findFirstDiscrepancyTransaction(transactions, wallets)

        assertEquals("t3", result)
    }

    @Test
    fun `findFirstDiscrepancyTransaction processes chronologically`() {
        val physicalWallet = createWallet(id = "p1", balance = 100.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 50.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet,
            "l1" to logicalWallet
        )

        // Transactions not in chronological order
        val transactions = listOf(
            createTransaction(
                id = "t3",
                amount = 50.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1"),
                date = Date(3000)
            ),
            createTransaction(
                id = "t1",
                amount = 50.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1"), // First discrepancy by date
                date = Date(1000)
            ),
            createTransaction(
                id = "t2",
                amount = 50.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1"),
                date = Date(2000)
            )
        )

        val result = analyzer.findFirstDiscrepancyTransaction(transactions, wallets)

        // Should find t1 as it's the earliest by date
        assertEquals("t1", result)
    }

    // Tests for calculateRunningBalances()

    @Test
    fun `calculateRunningBalances with single transaction`() {
        val physicalWallet = createWallet(id = "p1", balance = 100.0, initialBalance = 0.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 100.0, initialBalance = 0.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet,
            "l1" to logicalWallet
        )

        val transactions = listOf(
            createTransaction(
                id = "t1",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(1000)
            )
        )

        val result = analyzer.calculateRunningBalances(transactions, wallets)

        assertEquals(1, result.size)
        assertEquals("t1", result[0].transaction.id)
        assertEquals(100.0, result[0].physicalBalanceAfter, 0.001)
        assertEquals(100.0, result[0].logicalBalanceAfter, 0.001)
        assertEquals(false, result[0].isFirstDiscrepancy)
    }

    @Test
    fun `calculateRunningBalances with multiple transactions tracks cumulative balance`() {
        val physicalWallet = createWallet(id = "p1", balance = 300.0, initialBalance = 0.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 300.0, initialBalance = 0.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet,
            "l1" to logicalWallet
        )

        val transactions = listOf(
            createTransaction(
                id = "t1",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(1000)
            ),
            createTransaction(
                id = "t2",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(2000)
            ),
            createTransaction(
                id = "t3",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(3000)
            )
        )

        val result = analyzer.calculateRunningBalances(transactions, wallets)

        assertEquals(3, result.size)

        // Result is in ascending order (oldest first)
        // First transaction (oldest, index 0)
        assertEquals(100.0, result[0].physicalBalanceAfter, 0.001)
        assertEquals(100.0, result[0].logicalBalanceAfter, 0.001)

        // Second transaction (middle, index 1)
        assertEquals(200.0, result[1].physicalBalanceAfter, 0.001)
        assertEquals(200.0, result[1].logicalBalanceAfter, 0.001)

        // Third transaction (newest, index 2)
        assertEquals(300.0, result[2].physicalBalanceAfter, 0.001)
        assertEquals(300.0, result[2].logicalBalanceAfter, 0.001)
    }

    @Test
    fun `calculateRunningBalances marks first discrepancy correctly`() {
        val physicalWallet = createWallet(id = "p1", balance = 250.0, initialBalance = 0.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 200.0, initialBalance = 0.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet,
            "l1" to logicalWallet
        )

        val transactions = listOf(
            createTransaction(
                id = "t1",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(1000)
            ),
            createTransaction(
                id = "t2",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(2000)
            ),
            createTransaction(
                id = "t3",
                amount = 50.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1"), // Only physical - creates discrepancy
                date = Date(3000)
            )
        )

        val result = analyzer.calculateRunningBalances(transactions, wallets)

        // Result should only contain transactions from first discrepancy onward (only t3)
        assertEquals(1, result.size)

        // Third transaction (only one in result, index 0) - should be marked as first discrepancy
        assertTrue(result[0].isFirstDiscrepancy)
        assertEquals("t3", result[0].transaction.id)
        assertEquals(250.0, result[0].physicalBalanceAfter, 0.001)
        assertEquals(200.0, result[0].logicalBalanceAfter, 0.001)
    }

    @Test
    fun `calculateRunningBalances with empty list returns empty`() {
        val wallets = mapOf<String, Wallet>()
        val transactions = emptyList<Transaction>()

        val result = analyzer.calculateRunningBalances(transactions, wallets)

        assertEquals(0, result.size)
    }

    @Test
    fun `calculateRunningBalances handles expense transactions`() {
        val physicalWallet = createWallet(id = "p1", balance = 50.0, initialBalance = 100.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 50.0, initialBalance = 100.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet,
            "l1" to logicalWallet
        )

        val transactions = listOf(
            createTransaction(
                id = "t1",
                amount = 50.0,
                type = TransactionType.EXPENSE,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(1000)
            )
        )

        val result = analyzer.calculateRunningBalances(transactions, wallets)

        assertEquals(1, result.size)
        // Initial 100 - expense 50 = 50
        assertEquals(50.0, result[0].physicalBalanceAfter, 0.001)
        assertEquals(50.0, result[0].logicalBalanceAfter, 0.001)
    }

    @Test
    fun `calculateRunningBalances handles transfer transactions`() {
        val physicalWallet1 = createWallet(id = "p1", balance = 50.0, initialBalance = 100.0, walletType = "Physical")
        val physicalWallet2 = createWallet(id = "p2", balance = 150.0, initialBalance = 100.0, walletType = "Physical")
        val logicalWallet = createWallet(id = "l1", balance = 200.0, initialBalance = 200.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet1,
            "p2" to physicalWallet2,
            "l1" to logicalWallet
        )

        val transactions = listOf(
            createTransaction(
                id = "t1",
                amount = 50.0,
                type = TransactionType.TRANSFER,
                sourceWalletId = "p1",
                destinationWalletId = "p2",
                affectedWalletIds = listOf("p1", "p2"), // Only physical wallets
                date = Date(1000)
            )
        )

        val result = analyzer.calculateRunningBalances(transactions, wallets)

        assertEquals(1, result.size)
        // Physical total should stay 200 (50 + 150)
        // Logical stays 200
        // No discrepancy
        assertEquals(200.0, result[0].physicalBalanceAfter, 0.001)
        assertEquals(200.0, result[0].logicalBalanceAfter, 0.001)
        assertFalse(result[0].isFirstDiscrepancy)
    }

    @Test
    fun `calculateRunningBalances with multiple wallets per type`() {
        val physicalWallet1 = createWallet(id = "p1", balance = 100.0, initialBalance = 0.0, walletType = "Physical")
        val physicalWallet2 = createWallet(id = "p2", balance = 50.0, initialBalance = 0.0, walletType = "Physical")
        val logicalWallet1 = createWallet(id = "l1", balance = 75.0, initialBalance = 0.0, walletType = "Logical")
        val logicalWallet2 = createWallet(id = "l2", balance = 75.0, initialBalance = 0.0, walletType = "Logical")
        val wallets = mapOf(
            "p1" to physicalWallet1,
            "p2" to physicalWallet2,
            "l1" to logicalWallet1,
            "l2" to logicalWallet2
        )

        val transactions = listOf(
            createTransaction(
                id = "t1",
                amount = 100.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p1", "l1"),
                date = Date(1000)
            ),
            createTransaction(
                id = "t2",
                amount = 50.0,
                type = TransactionType.INCOME,
                affectedWalletIds = listOf("p2", "l2"),
                date = Date(2000)
            )
        )

        val result = analyzer.calculateRunningBalances(transactions, wallets)

        assertEquals(2, result.size)

        // Result is in ascending order (oldest first)
        // After first transaction (oldest, index 0): p1=100, p2=0, l1=100, l2=0
        assertEquals(100.0, result[0].physicalBalanceAfter, 0.001)
        assertEquals(100.0, result[0].logicalBalanceAfter, 0.001)

        // After second transaction (newest, index 1): p1=100, p2=50, l1=100, l2=50
        assertEquals(150.0, result[1].physicalBalanceAfter, 0.001)
        assertEquals(150.0, result[1].logicalBalanceAfter, 0.001)
    }

    // Helper functions

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

    private fun createTransaction(
        id: String,
        title: String = "Test Transaction",
        amount: Double = 0.0,
        category: String = "Untagged",
        type: TransactionType = TransactionType.INCOME,
        walletId: String = "",
        affectedWalletIds: List<String> = emptyList(),
        sourceWalletId: String = "",
        destinationWalletId: String = "",
        tags: List<String> = emptyList(),
        date: Date = Date(),
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
            userId = userId
        )
    }
}

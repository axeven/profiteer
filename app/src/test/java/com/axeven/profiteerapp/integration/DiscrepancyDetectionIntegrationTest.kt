package com.axeven.profiteerapp.integration

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.utils.BalanceDiscrepancyDetector
import com.axeven.profiteerapp.utils.DiscrepancyAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Integration test for end-to-end discrepancy detection flow.
 *
 * Tests the complete flow from creating transactions that cause discrepancies,
 * detecting the discrepancy, identifying the problematic transaction,
 * and verifying the fix.
 */
class DiscrepancyDetectionIntegrationTest {

    private lateinit var balanceDetector: BalanceDiscrepancyDetector
    private lateinit var discrepancyAnalyzer: DiscrepancyAnalyzer

    @Before
    fun setup() {
        balanceDetector = BalanceDiscrepancyDetector()
        discrepancyAnalyzer = DiscrepancyAnalyzer(balanceDetector)
    }

    @Test
    fun `end-to-end flow - detect discrepancy and identify problematic transaction`() {
        // Setup: Final wallet state with discrepancy
        val physicalWallet = Wallet(
            id = "physical1",
            name = "Cash",
            walletType = "Physical",
            balance = 1350.0, // Physical total
            initialBalance = 0.0,
            userId = "user1"
        )

        val logicalWallet = Wallet(
            id = "logical1",
            name = "Savings",
            walletType = "Logical",
            balance = 1450.0, // Logical total (100 more than physical!)
            initialBalance = 0.0,
            userId = "user1"
        )

        val wallets: List<Wallet> = listOf(physicalWallet, logicalWallet)

        // Step 1: Detect the discrepancy
        val physicalTotal = balanceDetector.calculateTotalPhysicalBalance(wallets)
        val logicalTotal = balanceDetector.calculateTotalLogicalBalance(wallets)

        assertEquals(1350.0, physicalTotal, 0.01)
        assertEquals(1450.0, logicalTotal, 0.01)
        assertTrue("Should detect discrepancy",
            balanceDetector.hasDiscrepancy(physicalTotal, logicalTotal))

        val discrepancyAmount = balanceDetector.getDiscrepancyAmount(physicalTotal, logicalTotal)
        assertEquals(-100.0, discrepancyAmount, 0.01) // Physical is 100 less than logical

        // Step 2: Historical transactions
        val transaction1 = Transaction(
            id = "t1",
            userId = "user1",
            title = "Salary",
            amount = 500.0,
            type = TransactionType.INCOME,
            walletId = "physical1",
            affectedWalletIds = listOf("physical1", "logical1"),
            transactionDate = Date(1000),
            createdAt = Date(1000)
        )

        val transaction2 = Transaction(
            id = "t2",
            userId = "user1",
            title = "Groceries",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            walletId = "physical1",
            affectedWalletIds = listOf("physical1"), // BUG: Missing logical1!
            transactionDate = Date(2000),
            createdAt = Date(2000)
        )

        val transaction3 = Transaction(
            id = "t3",
            userId = "user1",
            title = "Coffee",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            walletId = "physical1",
            affectedWalletIds = listOf("physical1", "logical1"),
            transactionDate = Date(3000),
            createdAt = Date(3000)
        )

        val transactions: List<Transaction> = listOf(transaction1, transaction2, transaction3)

        // Step 3: Identify the first transaction that caused the discrepancy
        val walletMap: Map<String, Wallet> = wallets.associateBy { it.id }
        val firstDiscrepancyId = discrepancyAnalyzer.findFirstDiscrepancyTransaction(
            transactions = transactions,
            wallets = walletMap
        )

        assertNotNull("Should identify discrepancy transaction", firstDiscrepancyId)
        assertEquals("t2", firstDiscrepancyId) // Transaction 2 is the culprit

        // Step 4: Get running balances from first discrepancy onward
        val transactionsWithBalances = discrepancyAnalyzer.calculateRunningBalances(
            transactions = transactions,
            wallets = walletMap
        )

        // Should only return transactions from first discrepancy (t2) onward (t2 and t3)
        assertEquals(2, transactionsWithBalances.size)

        // Verify the first discrepancy is flagged correctly
        val firstDiscrepancyTxn = transactionsWithBalances.find { it.isFirstDiscrepancy }
        assertNotNull("Should have a transaction marked as first discrepancy", firstDiscrepancyTxn)
        assertEquals("t2", firstDiscrepancyTxn!!.transaction.id)

        // Verify transactions are in ascending order (t2 first, then t3)
        assertEquals("t2", transactionsWithBalances[0].transaction.id)
        assertEquals("t3", transactionsWithBalances[1].transaction.id)

        // Step 5: Fix scenario - update transaction2 to include both wallets
        val fixedTransaction2 = transaction2.copy(
            affectedWalletIds = listOf("physical1", "logical1") // Now correct!
        )
        val fixedTransactions: List<Transaction> = listOf(transaction1, fixedTransaction2, transaction3)

        // Fixed wallet state (both now 1350)
        val fixedPhysicalWallet: Wallet = physicalWallet.copy(balance = 1350.0)
        val fixedLogicalWallet: Wallet = logicalWallet.copy(balance = 1350.0)
        val fixedWallets: List<Wallet> = listOf(fixedPhysicalWallet, fixedLogicalWallet)

        // Verify discrepancy is resolved
        val fixedPhysicalTotal = balanceDetector.calculateTotalPhysicalBalance(fixedWallets)
        val fixedLogicalTotal = balanceDetector.calculateTotalLogicalBalance(fixedWallets)

        assertEquals(1350.0, fixedPhysicalTotal, 0.01)
        assertEquals(1350.0, fixedLogicalTotal, 0.01)
        assertFalse("Should be balanced after fix",
            balanceDetector.hasDiscrepancy(fixedPhysicalTotal, fixedLogicalTotal))

        // Verify no discrepancy transaction is identified after fix
        val fixedWalletMap: Map<String, Wallet> = fixedWallets.associateBy { it.id }
        val noDiscrepancy = discrepancyAnalyzer.findFirstDiscrepancyTransaction(
            transactions = fixedTransactions,
            wallets = fixedWalletMap
        )

        assertNull("Should not find any discrepancy after fix", noDiscrepancy)
    }

    @Test
    fun `integration - multiple discrepancies, identify first one only`() {
        val physicalWallet = Wallet(
            id = "physical1",
            name = "Cash",
            walletType = "Physical",
            balance = 500.0,
            initialBalance = 0.0,
            userId = "user1"
        )

        val logicalWallet = Wallet(
            id = "logical1",
            name = "Savings",
            walletType = "Logical",
            balance = 700.0, // Already discrepancy of 200
            initialBalance = 0.0,
            userId = "user1"
        )

        // Transaction 1: Another discrepancy (oldest)
        val transaction1 = Transaction(
            id = "t1",
            userId = "user1",
            title = "First Problem",
            amount = 100.0,
            type = TransactionType.INCOME,
            walletId = "physical1",
            affectedWalletIds = listOf("physical1"), // Missing logical1
            transactionDate = Date(1000),
            createdAt = Date(1000)
        )

        // Transaction 2: Another discrepancy (newer)
        val transaction2 = Transaction(
            id = "t2",
            userId = "user1",
            title = "Second Problem",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            walletId = "physical1",
            affectedWalletIds = listOf("physical1"), // Missing logical1
            transactionDate = Date(2000),
            createdAt = Date(2000)
        )

        val transactions: List<Transaction> = listOf(transaction1, transaction2)
        val wallets: List<Wallet> = listOf(physicalWallet, logicalWallet)
        val walletMap: Map<String, Wallet> = wallets.associateBy { it.id }

        // Should identify the FIRST (chronologically oldest) discrepancy
        val firstDiscrepancyId = discrepancyAnalyzer.findFirstDiscrepancyTransaction(
            transactions = transactions,
            wallets = walletMap
        )

        assertEquals("t1", firstDiscrepancyId) // First one chronologically
    }

    @Test
    fun `integration - discrepancy detection with multiple wallet types`() {
        // Scenario: Multiple physical and logical wallets with a discrepancy
        val physicalWallet1 = Wallet(
            id = "physical1",
            name = "Cash",
            walletType = "Physical",
            balance = 500.0,
            initialBalance = 0.0,
            userId = "user1"
        )

        val physicalWallet2 = Wallet(
            id = "physical2",
            name = "Bank",
            walletType = "Physical",
            balance = 500.0,
            initialBalance = 0.0,
            userId = "user1"
        )

        val logicalWallet = Wallet(
            id = "logical1",
            name = "Savings",
            walletType = "Logical",
            balance = 900.0, // Should be 1000!
            initialBalance = 0.0,
            userId = "user1"
        )

        val wallets: List<Wallet> = listOf(physicalWallet1, physicalWallet2, logicalWallet)

        val physicalTotal = balanceDetector.calculateTotalPhysicalBalance(wallets)
        val logicalTotal = balanceDetector.calculateTotalLogicalBalance(wallets)

        // Physical: 500 + 500 = 1000
        // Logical: 900
        assertEquals(1000.0, physicalTotal, 0.01)
        assertEquals(900.0, logicalTotal, 0.01)
        assertTrue("Should detect discrepancy",
            balanceDetector.hasDiscrepancy(physicalTotal, logicalTotal))

        val discrepancyAmount = balanceDetector.getDiscrepancyAmount(physicalTotal, logicalTotal)
        assertEquals(100.0, discrepancyAmount, 0.01) // Physical is 100 more than logical
    }

    @Test
    fun `integration - empty transaction list, no discrepancy`() {
        val physicalWallet = Wallet(
            id = "physical1",
            name = "Cash",
            walletType = "Physical",
            balance = 1000.0,
            initialBalance = 0.0,
            userId = "user1"
        )

        val logicalWallet = Wallet(
            id = "logical1",
            name = "Savings",
            walletType = "Logical",
            balance = 1000.0,
            initialBalance = 0.0,
            userId = "user1"
        )

        val wallets: List<Wallet> = listOf(physicalWallet, logicalWallet)
        val transactions: List<Transaction> = emptyList()

        val physicalTotal = balanceDetector.calculateTotalPhysicalBalance(wallets)
        val logicalTotal = balanceDetector.calculateTotalLogicalBalance(wallets)

        assertFalse("Should be balanced with no transactions",
            balanceDetector.hasDiscrepancy(physicalTotal, logicalTotal))

        val walletMap: Map<String, Wallet> = wallets.associateBy { it.id }
        val firstDiscrepancyId = discrepancyAnalyzer.findFirstDiscrepancyTransaction(
            transactions = transactions,
            wallets = walletMap
        )

        assertNull("Should not find discrepancy with no transactions", firstDiscrepancyId)
    }
}

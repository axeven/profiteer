package com.axeven.profiteerapp.data.service

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.utils.logging.Logger
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * Tests for BalanceCalculationServiceImpl specifically for logical wallet scenarios.
 *
 * This test verifies the fix for the wallet aggregation bug where logical wallets
 * were showing zero income/expenses despite having transactions.
 *
 * Bug: The service only checked transaction.walletId == walletId, missing transactions
 * where the logical wallet was in affectedWalletIds but not the primary wallet.
 */
class BalanceCalculationLogicalWalletTest {

    private lateinit var balanceCalculationService: BalanceCalculationService

    @Mock
    private lateinit var mockLogger: Logger

    // Mock wallet IDs
    private val physicalWalletId = "physical-cash-123"
    private val logicalWalletId = "logical-investment-123"
    private val userId = "user-123"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        balanceCalculationService = BalanceCalculationServiceImpl(mockLogger)
    }

    @Test
    fun `should calculate income for logical wallet using affectedWalletIds`() {
        // Create an income transaction that affects both physical and logical wallets
        val incomeTransaction = Transaction(
            id = "trans-1",
            title = "Investment Income",
            amount = 1000.0,
            type = TransactionType.INCOME,
            walletId = physicalWalletId, // Primary wallet (physical)
            affectedWalletIds = listOf(physicalWalletId, logicalWalletId), // Both wallets affected
            userId = userId,
            transactionDate = Date()
        )

        val transactions = listOf(incomeTransaction)

        // Test: Logical wallet should recognize this income transaction
        val logicalIncome = balanceCalculationService.calculateIncome(transactions, logicalWalletId)
        val logicalExpenses = balanceCalculationService.calculateExpenses(transactions, logicalWalletId)

        // Assert: Logical wallet should show the income
        assertEquals("Logical wallet should show income from affectedWalletIds", 1000.0, logicalIncome, 0.01)
        assertEquals("Logical wallet should have zero expenses", 0.0, logicalExpenses, 0.01)

        // Test: Physical wallet should also recognize this income transaction
        val physicalIncome = balanceCalculationService.calculateIncome(transactions, physicalWalletId)
        val physicalExpenses = balanceCalculationService.calculateExpenses(transactions, physicalWalletId)

        // Assert: Physical wallet should also show the income
        assertEquals("Physical wallet should show income from walletId", 1000.0, physicalIncome, 0.01)
        assertEquals("Physical wallet should have zero expenses", 0.0, physicalExpenses, 0.01)
    }

    @Test
    fun `should calculate expenses for logical wallet using affectedWalletIds`() {
        // Create an expense transaction that affects both physical and logical wallets
        val expenseTransaction = Transaction(
            id = "trans-2",
            title = "Investment Expense",
            amount = 500.0,
            type = TransactionType.EXPENSE,
            walletId = physicalWalletId, // Primary wallet (physical)
            affectedWalletIds = listOf(physicalWalletId, logicalWalletId), // Both wallets affected
            userId = userId,
            transactionDate = Date()
        )

        val transactions = listOf(expenseTransaction)

        // Test: Logical wallet should recognize this expense transaction
        val logicalIncome = balanceCalculationService.calculateIncome(transactions, logicalWalletId)
        val logicalExpenses = balanceCalculationService.calculateExpenses(transactions, logicalWalletId)

        // Assert: Logical wallet should show the expense
        assertEquals("Logical wallet should have zero income", 0.0, logicalIncome, 0.01)
        assertEquals("Logical wallet should show expense from affectedWalletIds", 500.0, logicalExpenses, 0.01)

        // Test: Physical wallet should also recognize this expense transaction
        val physicalIncome = balanceCalculationService.calculateIncome(transactions, physicalWalletId)
        val physicalExpenses = balanceCalculationService.calculateExpenses(transactions, physicalWalletId)

        // Assert: Physical wallet should also show the expense
        assertEquals("Physical wallet should have zero income", 0.0, physicalIncome, 0.01)
        assertEquals("Physical wallet should show expense from walletId", 500.0, physicalExpenses, 0.01)
    }

    @Test
    fun `should handle mixed income and expense transactions for logical wallet`() {
        // Create multiple transactions affecting the logical wallet
        val transactions = listOf(
            Transaction(
                id = "trans-income-1",
                title = "Investment Dividend",
                amount = 1200.0,
                type = TransactionType.INCOME,
                walletId = physicalWalletId,
                affectedWalletIds = listOf(physicalWalletId, logicalWalletId),
                userId = userId,
                transactionDate = Date()
            ),
            Transaction(
                id = "trans-income-2",
                title = "Investment Gain",
                amount = 800.0,
                type = TransactionType.INCOME,
                walletId = physicalWalletId,
                affectedWalletIds = listOf(physicalWalletId, logicalWalletId),
                userId = userId,
                transactionDate = Date()
            ),
            Transaction(
                id = "trans-expense-1",
                title = "Investment Fee",
                amount = 150.0,
                type = TransactionType.EXPENSE,
                walletId = physicalWalletId,
                affectedWalletIds = listOf(physicalWalletId, logicalWalletId),
                userId = userId,
                transactionDate = Date()
            ),
            Transaction(
                id = "trans-expense-2",
                title = "Investment Loss",
                amount = 350.0,
                type = TransactionType.EXPENSE,
                walletId = physicalWalletId,
                affectedWalletIds = listOf(physicalWalletId, logicalWalletId),
                userId = userId,
                transactionDate = Date()
            )
        )

        // Test: Logical wallet aggregation
        val logicalIncome = balanceCalculationService.calculateIncome(transactions, logicalWalletId)
        val logicalExpenses = balanceCalculationService.calculateExpenses(transactions, logicalWalletId)
        val logicalNetBalance = balanceCalculationService.calculateNetBalance(transactions, logicalWalletId)

        // Assert: Logical wallet should show correct aggregations
        assertEquals("Logical wallet income should be sum of income transactions", 2000.0, logicalIncome, 0.01) // 1200 + 800
        assertEquals("Logical wallet expenses should be sum of expense transactions", 500.0, logicalExpenses, 0.01) // 150 + 350
        assertEquals("Logical wallet net balance should be income - expenses", 1500.0, logicalNetBalance, 0.01) // 2000 - 500
    }

    @Test
    fun `should not include transactions that don't affect the logical wallet`() {
        // Create transactions where logical wallet is NOT in affectedWalletIds
        val transactions = listOf(
            Transaction(
                id = "trans-unrelated",
                title = "Cash Transaction",
                amount = 100.0,
                type = TransactionType.INCOME,
                walletId = physicalWalletId,
                affectedWalletIds = listOf(physicalWalletId), // Only physical wallet affected
                userId = userId,
                transactionDate = Date()
            ),
            Transaction(
                id = "trans-affecting-logical",
                title = "Investment Transaction",
                amount = 200.0,
                type = TransactionType.INCOME,
                walletId = physicalWalletId,
                affectedWalletIds = listOf(physicalWalletId, logicalWalletId), // Both wallets affected
                userId = userId,
                transactionDate = Date()
            )
        )

        // Test: Logical wallet should only see transactions that affect it
        val logicalIncome = balanceCalculationService.calculateIncome(transactions, logicalWalletId)
        val logicalExpenses = balanceCalculationService.calculateExpenses(transactions, logicalWalletId)

        // Assert: Only the transaction with logical wallet in affectedWalletIds should be included
        assertEquals("Logical wallet should only include transactions affecting it", 200.0, logicalIncome, 0.01)
        assertEquals("Logical wallet should have zero expenses", 0.0, logicalExpenses, 0.01)

        // Test: Physical wallet should see both transactions
        val physicalIncome = balanceCalculationService.calculateIncome(transactions, physicalWalletId)

        // Assert: Physical wallet should see all transactions with its walletId
        assertEquals("Physical wallet should see both transactions", 300.0, physicalIncome, 0.01) // 100 + 200
    }

    @Test
    fun `should handle transfer transactions correctly for logical wallets`() {
        // Create a transfer transaction involving logical wallet
        val transferTransaction = Transaction(
            id = "trans-transfer",
            title = "Investment Transfer",
            amount = 750.0,
            type = TransactionType.TRANSFER,
            walletId = "", // Not used for transfers
            affectedWalletIds = emptyList(), // Not used for transfers
            sourceWalletId = physicalWalletId,
            destinationWalletId = logicalWalletId,
            userId = userId,
            transactionDate = Date()
        )

        val transactions = listOf(transferTransaction)

        // Test: Source wallet (physical) should see this as expense
        val sourceIncome = balanceCalculationService.calculateIncome(transactions, physicalWalletId)
        val sourceExpenses = balanceCalculationService.calculateExpenses(transactions, physicalWalletId)

        // Assert: Source wallet should show transfer as expense
        assertEquals("Source wallet should have zero income from transfer", 0.0, sourceIncome, 0.01)
        assertEquals("Source wallet should show transfer as expense", 750.0, sourceExpenses, 0.01)

        // Test: Destination wallet (logical) should see this as income
        val destIncome = balanceCalculationService.calculateIncome(transactions, logicalWalletId)
        val destExpenses = balanceCalculationService.calculateExpenses(transactions, logicalWalletId)

        // Assert: Destination wallet should show transfer as income
        assertEquals("Destination wallet should show transfer as income", 750.0, destIncome, 0.01)
        assertEquals("Destination wallet should have zero expenses from transfer", 0.0, destExpenses, 0.01)
    }
}
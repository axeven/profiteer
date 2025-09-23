package com.axeven.profiteerapp.data.service

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.utils.logging.Logger
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class BalanceCalculationServiceTest {

    private lateinit var service: BalanceCalculationService
    private lateinit var mockLogger: Logger

    // Test wallet IDs
    private val walletA = "wallet-a"
    private val walletB = "wallet-b"
    private val walletC = "wallet-c"

    @Before
    fun setup() {
        // Create a simple mock logger that does nothing
        mockLogger = object : Logger {
            override fun d(tag: String, message: String) {}
            override fun i(tag: String, message: String) {}
            override fun w(tag: String, message: String) {}
            override fun e(tag: String, message: String, throwable: Throwable?) {}
        }
        service = BalanceCalculationServiceImpl(mockLogger)
    }

    @Test
    fun shouldCalculateIncomeOnlyTransactions() {
        val transactions = listOf(
            createIncomeTransaction(100.0),
            createIncomeTransaction(50.0),
            createIncomeTransaction(25.0)
        )

        val result = service.calculateIncome(transactions, walletA)

        assertEquals(175.0, result, 0.01)
    }

    @Test
    fun shouldCalculateExpenseOnlyTransactions() {
        val transactions = listOf(
            createExpenseTransaction(100.0),
            createExpenseTransaction(50.0),
            createExpenseTransaction(25.0)
        )

        val result = service.calculateExpenses(transactions, walletA)

        assertEquals(175.0, result, 0.01)
    }

    @Test
    fun shouldCalculateMixedTransactions() {
        val transactions = listOf(
            createIncomeTransaction(200.0),
            createExpenseTransaction(75.0),
            createIncomeTransaction(50.0),
            createExpenseTransaction(25.0)
        )

        val income = service.calculateIncome(transactions, walletA)
        val expenses = service.calculateExpenses(transactions, walletA)
        val netBalance = service.calculateNetBalance(transactions, walletA)

        assertEquals(250.0, income, 0.01)
        assertEquals(100.0, expenses, 0.01)
        assertEquals(150.0, netBalance, 0.01)
    }

    @Test
    fun shouldHandleEmptyTransactionList() {
        val transactions = emptyList<Transaction>()

        val income = service.calculateIncome(transactions, walletA)
        val expenses = service.calculateExpenses(transactions, walletA)
        val netBalance = service.calculateNetBalance(transactions, walletA)

        assertEquals(0.0, income, 0.01)
        assertEquals(0.0, expenses, 0.01)
        assertEquals(0.0, netBalance, 0.01)
    }

    @Test
    fun shouldCalculateTotalBalanceWithInitialBalance() {
        val transactions = listOf(
            createIncomeTransaction(100.0),
            createExpenseTransaction(30.0)
        )
        val initialBalance = 500.0

        val totalBalance = service.calculateTotalBalance(transactions, walletA, initialBalance)

        assertEquals(570.0, totalBalance, 0.01) // 500 + 100 - 30
    }

    @Test
    fun shouldDetectIncomingTransfer() {
        val transfer = createTransferTransaction(100.0, walletB, walletA)

        val direction = service.getTransferDirection(transfer, walletA)
        val isIncome = service.isTransferIncome(transfer, walletA)
        val isExpense = service.isTransferExpense(transfer, walletA)

        assertEquals(TransferDirection.INCOMING, direction)
        assertTrue(isIncome)
        assertFalse(isExpense)
    }

    @Test
    fun shouldDetectOutgoingTransfer() {
        val transfer = createTransferTransaction(100.0, walletA, walletB)

        val direction = service.getTransferDirection(transfer, walletA)
        val isIncome = service.isTransferIncome(transfer, walletA)
        val isExpense = service.isTransferExpense(transfer, walletA)

        assertEquals(TransferDirection.OUTGOING, direction)
        assertFalse(isIncome)
        assertTrue(isExpense)
    }

    @Test
    fun shouldReturnNullForUnrelatedTransfer() {
        val transfer = createTransferTransaction(100.0, walletB, walletC)

        val direction = service.getTransferDirection(transfer, walletA)
        val isIncome = service.isTransferIncome(transfer, walletA)
        val isExpense = service.isTransferExpense(transfer, walletA)

        assertNull(direction)
        assertFalse(isIncome)
        assertFalse(isExpense)
    }

    @Test
    fun shouldCalculateIncomeWithIncomingTransfers() {
        val transactions = listOf(
            createIncomeTransaction(100.0),
            createTransferTransaction(50.0, walletB, walletA), // Incoming
            createTransferTransaction(25.0, walletA, walletB), // Outgoing (should not count)
            createIncomeTransaction(75.0)
        )

        val income = service.calculateIncome(transactions, walletA)

        assertEquals(225.0, income, 0.01) // 100 + 50 + 75
    }

    @Test
    fun shouldCalculateExpensesWithOutgoingTransfers() {
        val transactions = listOf(
            createExpenseTransaction(100.0),
            createTransferTransaction(50.0, walletA, walletB), // Outgoing
            createTransferTransaction(25.0, walletB, walletA), // Incoming (should not count)
            createExpenseTransaction(75.0)
        )

        val expenses = service.calculateExpenses(transactions, walletA)

        assertEquals(225.0, expenses, 0.01) // 100 + 50 + 75
    }

    @Test
    fun shouldReturnPositiveForIncome() {
        val transaction = createIncomeTransaction(100.0)

        val effectiveAmount = service.getEffectiveAmount(transaction, walletA)

        assertEquals(100.0, effectiveAmount, 0.01)
    }

    @Test
    fun shouldReturnNegativeForExpense() {
        val transaction = createExpenseTransaction(100.0)

        val effectiveAmount = service.getEffectiveAmount(transaction, walletA)

        assertEquals(-100.0, effectiveAmount, 0.01)
    }

    @Test
    fun shouldReturnPositiveForIncomingTransfer() {
        val transfer = createTransferTransaction(100.0, walletB, walletA)

        val effectiveAmount = service.getEffectiveAmount(transfer, walletA)

        assertEquals(100.0, effectiveAmount, 0.01)
    }

    @Test
    fun shouldReturnNegativeForOutgoingTransfer() {
        val transfer = createTransferTransaction(100.0, walletA, walletB)

        val effectiveAmount = service.getEffectiveAmount(transfer, walletA)

        assertEquals(-100.0, effectiveAmount, 0.01)
    }

    @Test
    fun shouldReturnZeroForUnrelatedTransfer() {
        val transfer = createTransferTransaction(100.0, walletB, walletC)

        val effectiveAmount = service.getEffectiveAmount(transfer, walletA)

        assertEquals(0.0, effectiveAmount, 0.01)
    }

    @Test
    fun shouldCalculateComprehensivePeriodSummary() {
        val transactions = listOf(
            createIncomeTransaction(200.0),
            createIncomeTransaction(100.0),
            createExpenseTransaction(75.0),
            createExpenseTransaction(25.0),
            createTransferTransaction(50.0, walletB, walletA), // Incoming
            createTransferTransaction(30.0, walletA, walletB), // Outgoing
            createTransferTransaction(20.0, walletB, walletC)  // Unrelated
        )

        val summary = service.calculatePeriodSummary(transactions, walletA)

        assertEquals(350.0, summary.income, 0.01) // 200 + 100 + 50
        assertEquals(130.0, summary.expenses, 0.01) // 75 + 25 + 30
        assertEquals(220.0, summary.netChange, 0.01) // 350 - 130
        assertEquals(7, summary.transactionCount)
        assertEquals(50.0, summary.transfersIn, 0.01)
        assertEquals(30.0, summary.transfersOut, 0.01)
        assertEquals(2, summary.incomeTransactionCount)
        assertEquals(2, summary.expenseTransactionCount)
        assertEquals(1, summary.incomingTransferCount)
        assertEquals(1, summary.outgoingTransferCount)
    }

    @Test
    fun shouldHandlePeriodSummaryWithNoTransactions() {
        val transactions = emptyList<Transaction>()

        val summary = service.calculatePeriodSummary(transactions, walletA)

        assertEquals(0.0, summary.income, 0.01)
        assertEquals(0.0, summary.expenses, 0.01)
        assertEquals(0.0, summary.netChange, 0.01)
        assertEquals(0, summary.transactionCount)
        assertEquals(0.0, summary.transfersIn, 0.01)
        assertEquals(0.0, summary.transfersOut, 0.01)
        assertEquals(0, summary.incomeTransactionCount)
        assertEquals(0, summary.expenseTransactionCount)
        assertEquals(0, summary.incomingTransferCount)
        assertEquals(0, summary.outgoingTransferCount)
    }

    @Test
    fun shouldCalculateDailySummary() {
        val transactions = listOf(
            createIncomeTransaction(100.0),
            createExpenseTransaction(30.0),
            createTransferTransaction(20.0, walletB, walletA), // +20
            createTransferTransaction(10.0, walletA, walletB)  // -10
        )

        val summary = service.calculateDailySummary(transactions, walletA)

        assertEquals(4, summary.transactionCount)
        assertEquals(80.0, summary.netAmount, 0.01) // 100 - 30 + 20 - 10
    }

    @Test
    fun shouldHandleDailySummaryWithNegativeNet() {
        val transactions = listOf(
            createIncomeTransaction(50.0),
            createExpenseTransaction(100.0),
            createExpenseTransaction(25.0)
        )

        val summary = service.calculateDailySummary(transactions, walletA)

        assertEquals(3, summary.transactionCount)
        assertEquals(-75.0, summary.netAmount, 0.01) // 50 - 100 - 25
    }

    @Test
    fun shouldHandleZeroAmounts() {
        val transactions = listOf(
            createIncomeTransaction(0.0),
            createExpenseTransaction(0.0),
            createTransferTransaction(0.0, walletA, walletB)
        )

        val income = service.calculateIncome(transactions, walletA)
        val expenses = service.calculateExpenses(transactions, walletA)
        val netBalance = service.calculateNetBalance(transactions, walletA)

        assertEquals(0.0, income, 0.01)
        assertEquals(0.0, expenses, 0.01)
        assertEquals(0.0, netBalance, 0.01)
    }

    @Test
    fun shouldHandleNegativeAmounts() {
        val transactions = listOf(
            createTransaction(TransactionType.INCOME, -100.0, walletA),
            createTransaction(TransactionType.EXPENSE, -50.0, walletA)
        )

        val income = service.calculateIncome(transactions, walletA)
        val expenses = service.calculateExpenses(transactions, walletA)

        assertEquals(100.0, income, 0.01)
        assertEquals(50.0, expenses, 0.01)
    }

    @Test
    fun shouldHandleMultiWalletTransferChains() {
        val transactions = listOf(
            // Initial income to wallet A
            createTransaction(TransactionType.INCOME, 1000.0, walletA),
            // A -> B transfer
            createTransferTransaction(300.0, walletA, walletB),
            // B -> C transfer
            createTransferTransaction(100.0, walletB, walletC),
            // C -> A transfer (completing a cycle)
            createTransferTransaction(50.0, walletC, walletA),
            // Regular expense from A
            createTransaction(TransactionType.EXPENSE, 200.0, walletA)
        )

        // For wallet A: +1000 (income) -300 (out to B) +50 (in from C) -200 (expense) = 550
        val netBalanceA = service.calculateNetBalance(transactions, walletA)
        assertEquals(550.0, netBalanceA, 0.01)

        // For wallet B: +300 (in from A) -100 (out to C) = 200
        val netBalanceB = service.calculateNetBalance(transactions, walletB)
        assertEquals(200.0, netBalanceB, 0.01)

        // For wallet C: +100 (in from B) -50 (out to A) = 50
        val netBalanceC = service.calculateNetBalance(transactions, walletC)
        assertEquals(50.0, netBalanceC, 0.01)

        // Total across all wallets should equal original income minus expense
        val totalNet = netBalanceA + netBalanceB + netBalanceC
        assertEquals(800.0, totalNet, 0.01) // 1000 - 200
    }

    @Test
    fun shouldCalculateAccurateSummariesForMultiWallet() {
        val transactions = listOf(
            createTransaction(TransactionType.INCOME, 500.0, walletA),     // A: +500
            createTransferTransaction(100.0, walletA, walletB),             // A: -100, B: +100
            createTransferTransaction(50.0, walletB, walletA),              // A: +50, B: -50
            createTransaction(TransactionType.EXPENSE, 25.0, walletA)       // A: -25
        )

        val summaryA = service.calculatePeriodSummary(transactions, walletA)
        assertEquals(550.0, summaryA.income, 0.01)    // 500 + 50
        assertEquals(125.0, summaryA.expenses, 0.01)  // 100 + 25
        assertEquals(425.0, summaryA.netChange, 0.01) // 550 - 125

        val summaryB = service.calculatePeriodSummary(transactions, walletB)
        assertEquals(100.0, summaryB.income, 0.01)   // 100
        assertEquals(50.0, summaryB.expenses, 0.01)  // 50
        assertEquals(50.0, summaryB.netChange, 0.01) // 100 - 50
    }

    // Helper methods for creating test transactions

    private fun createIncomeTransaction(amount: Double): Transaction {
        return createTransaction(TransactionType.INCOME, amount, walletA)
    }

    private fun createExpenseTransaction(amount: Double): Transaction {
        return createTransaction(TransactionType.EXPENSE, amount, walletA)
    }

    private fun createTransferTransaction(
        amount: Double,
        sourceWalletId: String,
        destinationWalletId: String
    ): Transaction {
        return createTransaction(
            TransactionType.TRANSFER,
            amount,
            "",
            sourceWalletId,
            destinationWalletId
        )
    }

    private fun createTransaction(
        type: TransactionType,
        amount: Double,
        walletId: String = "",
        sourceWalletId: String = "",
        destinationWalletId: String = ""
    ): Transaction {
        return Transaction(
            id = UUID.randomUUID().toString(),
            title = "Test Transaction",
            amount = amount,
            type = type,
            walletId = walletId,
            sourceWalletId = sourceWalletId,
            destinationWalletId = destinationWalletId,
            userId = "test-user",
            transactionDate = Date(),
            createdAt = Date()
        )
    }
}
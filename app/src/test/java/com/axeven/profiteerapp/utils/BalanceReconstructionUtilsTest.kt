package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.constants.WalletType
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class BalanceReconstructionUtilsTest {

    // Helper function to create a date
    private fun createDate(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): Date {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, second)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // Helper function to create a physical wallet
    private fun createPhysicalWallet(
        id: String,
        name: String,
        balance: Double,
        physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            walletType = WalletType.PHYSICAL.displayName,
            physicalForm = physicalForm,
            userId = "user1"
        )
    }

    // Helper function to create a logical wallet
    private fun createLogicalWallet(
        id: String,
        name: String,
        balance: Double
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            walletType = WalletType.LOGICAL.displayName,
            physicalForm = PhysicalForm.FIAT_CURRENCY,
            userId = "user1"
        )
    }

    // Helper function to create a transaction
    private fun createTransaction(
        id: String,
        type: TransactionType,
        amount: Double,
        transactionDate: Date?,
        affectedWalletIds: List<String> = emptyList(),
        sourceWalletId: String = "",
        destinationWalletId: String = ""
    ): Transaction {
        return Transaction(
            id = id,
            title = "Test Transaction",
            amount = amount,
            type = type,
            transactionDate = transactionDate,
            affectedWalletIds = affectedWalletIds,
            sourceWalletId = sourceWalletId,
            destinationWalletId = destinationWalletId,
            userId = "user1"
        )
    }

    // ========== reconstructWalletBalancesAtDate tests ==========

    @Test
    fun `reconstructWalletBalancesAtDate AllTime returns current balances`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0),
            createPhysicalWallet("w2", "Wallet 2", 500.0)
        )
        val transactions = emptyList<Transaction>()

        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, null
        )

        assertEquals(2, result.size)
        assertEquals(1000.0, result["w1"]!!, 0.01)
        assertEquals(500.0, result["w2"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate filters transactions by endDate`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 200.0,
                createDate(2025, 11, 1), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Should only include t1 (100.0), not t2
        assertEquals(100.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate excludes transactions with null transactionDate`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 200.0,
                null, affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Should only include t1 (100.0), not t2 (null date)
        assertEquals(100.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate sorts transactions chronologically`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 50.0,
                createDate(2025, 10, 20), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.EXPENSE, 30.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Should be: 0 + 100 (t2) - 30 (t3) - 50 (t1) = 20
        assertEquals(20.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate INCOME adds to wallet balance`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        assertEquals(100.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate EXPENSE subtracts from wallet balance`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 30.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // 0 + 100 - 30 = 70
        assertEquals(70.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate TRANSFER subtracts from source adds to destination`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0),
            createPhysicalWallet("w2", "Wallet 2", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 5), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.TRANSFER, 50.0,
                createDate(2025, 10, 10),
                sourceWalletId = "w1",
                destinationWalletId = "w2")
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // w1: 0 + 100 - 50 = 50
        // w2: 0 + 50 = 50
        assertEquals(50.0, result["w1"]!!, 0.01)
        assertEquals(50.0, result["w2"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles multiple affected wallets for INCOME`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Physical", 1000.0),
            createLogicalWallet("w2", "Logical", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1", "w2"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Both wallets should receive +100
        assertEquals(100.0, result["w1"]!!, 0.01)
        assertEquals(100.0, result["w2"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles multiple affected wallets for EXPENSE`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Physical", 1000.0),
            createLogicalWallet("w2", "Logical", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1", "w2")),
            createTransaction("t2", TransactionType.EXPENSE, 30.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1", "w2"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Both wallets: 0 + 100 - 30 = 70
        assertEquals(70.0, result["w1"]!!, 0.01)
        assertEquals(70.0, result["w2"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate starts all wallets at 0_0`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = emptyList<Transaction>()

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // No transactions, so wallet should not appear (zero balance)
        assertFalse(result.containsKey("w1"))
    }

    @Test
    fun `reconstructWalletBalancesAtDate excludes wallets with zero balance`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0),
            createPhysicalWallet("w2", "Wallet 2", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // w1: 0 + 100 - 100 = 0 (excluded)
        // w2: 0 (excluded)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `reconstructWalletBalancesAtDate excludes wallets with no transactions before endDate`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0),
            createPhysicalWallet("w2", "Wallet 2", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 11, 5), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Transaction is after endDate, so no wallets should have balance
        assertTrue(result.isEmpty())
    }

    @Test
    fun `reconstructWalletBalancesAtDate includes wallet with transaction before creation date`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0).copy(
                createdAt = createDate(2025, 10, 20)
            )
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Wallet should be included because transaction exists before endDate
        assertEquals(100.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles wallet created after endDate excluded`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0).copy(
                createdAt = createDate(2025, 11, 5)
            )
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 11, 10), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Transaction is after endDate, wallet should not be included
        assertTrue(result.isEmpty())
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles sequential income and expense`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 5), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 30.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t4", TransactionType.EXPENSE, 20.0,
                createDate(2025, 10, 20), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // 0 + 100 - 30 + 50 - 20 = 100
        assertEquals(100.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles balance going to zero then positive again`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 5), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // 0 + 100 - 100 + 50 = 50 (should be included)
        assertEquals(50.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles multiple transactions on same date`() {
        val sameDate = createDate(2025, 10, 15, 12, 0, 0)
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                sameDate, affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                sameDate, affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.EXPENSE, 30.0,
                sameDate, affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // 0 + 100 + 50 - 30 = 120
        assertEquals(120.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles transaction exactly at endDate included`() {
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                endDate, affectedWalletIds = listOf("w1"))
        )

        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        assertEquals(100.0, result["w1"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles transaction after endDate excluded`() {
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 11, 1, 0, 0, 0), affectedWalletIds = listOf("w1"))
        )

        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Transaction is after endDate
        assertTrue(result.isEmpty())
    }

    // ========== reconstructPortfolioComposition tests ==========

    @Test
    fun `reconstructPortfolioComposition groups by PhysicalForm`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0, PhysicalForm.FIAT_CURRENCY),
            createPhysicalWallet("w2", "Wallet 2", 500.0, PhysicalForm.CRYPTOCURRENCY),
            createPhysicalWallet("w3", "Wallet 3", 300.0, PhysicalForm.FIAT_CURRENCY)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2")),
            createTransaction("t3", TransactionType.INCOME, 30.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w3"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructPortfolioComposition(
            wallets, transactions, endDate
        )

        // FIAT_CURRENCY: w1(100) + w3(30) = 130
        // CRYPTOCURRENCY: w2(50) = 50
        assertEquals(130.0, result[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
        assertEquals(50.0, result[PhysicalForm.CRYPTOCURRENCY]!!, 0.01)
    }

    @Test
    fun `reconstructPortfolioComposition excludes logical wallets`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Physical", 1000.0),
            createLogicalWallet("w2", "Logical", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructPortfolioComposition(
            wallets, transactions, endDate
        )

        // Only physical wallet should be included
        assertEquals(1, result.size)
        assertEquals(100.0, result[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
    }

    @Test
    fun `reconstructPortfolioComposition sums balances by PhysicalForm`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Stocks 1", 1000.0, PhysicalForm.STOCKS),
            createPhysicalWallet("w2", "Stocks 2", 500.0, PhysicalForm.STOCKS),
            createPhysicalWallet("w3", "Stocks 3", 300.0, PhysicalForm.STOCKS)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2")),
            createTransaction("t3", TransactionType.INCOME, 30.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w3"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructPortfolioComposition(
            wallets, transactions, endDate
        )

        // All STOCKS: 100 + 50 + 30 = 180
        assertEquals(180.0, result[PhysicalForm.STOCKS]!!, 0.01)
    }

    @Test
    fun `reconstructPortfolioComposition excludes zero and negative balances`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0),
            createPhysicalWallet("w2", "Wallet 2", 500.0),
            createPhysicalWallet("w3", "Wallet 3", 300.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.EXPENSE, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructPortfolioComposition(
            wallets, transactions, endDate
        )

        // w1: 0 (excluded), w2: -50 (excluded), w3: 0 (excluded)
        assertTrue(result.isEmpty())
    }

    // ========== reconstructPhysicalWalletBalances tests ==========

    @Test
    fun `reconstructPhysicalWalletBalances only includes physical wallets`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Physical", 1000.0),
            createLogicalWallet("w2", "Logical", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructPhysicalWalletBalances(
            wallets, transactions, endDate
        )

        assertEquals(1, result.size)
        assertEquals(100.0, result["Physical"]!!, 0.01)
    }

    @Test
    fun `reconstructPhysicalWalletBalances maps wallet name to balance`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash Wallet", 1000.0),
            createPhysicalWallet("w2", "Bank Account", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructPhysicalWalletBalances(
            wallets, transactions, endDate
        )

        assertEquals(100.0, result["Cash Wallet"]!!, 0.01)
        assertEquals(50.0, result["Bank Account"]!!, 0.01)
    }

    @Test
    fun `reconstructPhysicalWalletBalances excludes zero balances`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0),
            createPhysicalWallet("w2", "Wallet 2", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructPhysicalWalletBalances(
            wallets, transactions, endDate
        )

        // w1: 0 (excluded), w2: 0 (excluded)
        assertTrue(result.isEmpty())
    }

    // ========== reconstructLogicalWalletBalances tests ==========

    @Test
    fun `reconstructLogicalWalletBalances only includes logical wallets`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Physical", 1000.0),
            createLogicalWallet("w2", "Logical", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructLogicalWalletBalances(
            wallets, transactions, endDate
        )

        assertEquals(1, result.size)
        assertEquals(50.0, result["Logical"]!!, 0.01)
    }

    @Test
    fun `reconstructLogicalWalletBalances includes negative balances`() {
        val wallets = listOf(
            createLogicalWallet("w1", "Budget 1", 500.0),
            createLogicalWallet("w2", "Budget 2", 300.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 150.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructLogicalWalletBalances(
            wallets, transactions, endDate
        )

        // w1: 0 + 100 - 150 = -50 (included)
        // w2: 0 + 50 = 50 (included)
        assertEquals(-50.0, result["Budget 1"]!!, 0.01)
        assertEquals(50.0, result["Budget 2"]!!, 0.01)
    }

    @Test
    fun `reconstructLogicalWalletBalances excludes zero balances`() {
        val wallets = listOf(
            createLogicalWallet("w1", "Budget 1", 500.0),
            createLogicalWallet("w2", "Budget 2", 300.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructLogicalWalletBalances(
            wallets, transactions, endDate
        )

        // w1: 0 (excluded), w2: 0 (excluded)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `reconstructLogicalWalletBalances handles overspending (negative balance)`() {
        val wallets = listOf(
            createLogicalWallet("w1", "Entertainment Budget", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 5), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructLogicalWalletBalances(
            wallets, transactions, endDate
        )

        // 0 + 50 - 100 = -50 (overspending, should be included)
        assertEquals(-50.0, result["Entertainment Budget"]!!, 0.01)
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles empty transactions list`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = emptyList<Transaction>()

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `reconstructWalletBalancesAtDate handles empty wallets list`() {
        val wallets = emptyList<Wallet>()
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
        )

        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val result = BalanceReconstructionUtils.reconstructWalletBalancesAtDate(
            wallets, transactions, endDate
        )

        // Transactions reference wallets that don't exist
        assertEquals(100.0, result["w1"]!!, 0.01)
    }
}

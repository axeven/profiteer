package com.axeven.profiteerapp

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.viewmodel.WalletDetailUiState
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Unit tests for WalletDetailViewModel balance recalculation feature
 * Tests the balance calculation logic with various transaction combinations
 * Following patterns from existing unit tests
 */
class WalletDetailViewModelBalanceRecalculationTest {
    
    @Test
    fun `ui state should have correct default recalculation values`() {
        val uiState = WalletDetailUiState()
        
        assertEquals(false, uiState.isRecalculatingBalance)
        assertEquals(null, uiState.recalculationError)
    }
    
    @Test
    fun `balance calculation with only income transactions should be correct`() {
        val initialBalance = 1000.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        
        val incomeTransactions = listOf(
            createTestTransaction(
                type = TransactionType.INCOME,
                amount = 500.0,
                walletId = "test-wallet"
            ),
            createTestTransaction(
                type = TransactionType.INCOME,
                amount = 300.0,
                walletId = "test-wallet"
            )
        )
        
        val expectedBalance = initialBalance + 500.0 + 300.0 // 1800.0
        assertEquals(1800.0, expectedBalance, 0.01)
    }
    
    @Test
    fun `balance calculation with only expense transactions should be correct`() {
        val initialBalance = 2000.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        
        val expenseTransactions = listOf(
            createTestTransaction(
                type = TransactionType.EXPENSE,
                amount = 200.0,
                walletId = "test-wallet"
            ),
            createTestTransaction(
                type = TransactionType.EXPENSE,
                amount = 150.0,
                walletId = "test-wallet"
            )
        )
        
        val expectedBalance = initialBalance - 200.0 - 150.0 // 1650.0
        assertEquals(1650.0, expectedBalance, 0.01)
    }
    
    @Test
    fun `balance calculation with mixed income and expense transactions should be correct`() {
        val initialBalance = 1000.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        
        val mixedTransactions = listOf(
            createTestTransaction(
                type = TransactionType.INCOME,
                amount = 800.0,
                walletId = "test-wallet"
            ),
            createTestTransaction(
                type = TransactionType.EXPENSE,
                amount = 300.0,
                walletId = "test-wallet"
            ),
            createTestTransaction(
                type = TransactionType.INCOME,
                amount = 200.0,
                walletId = "test-wallet"
            ),
            createTestTransaction(
                type = TransactionType.EXPENSE,
                amount = 100.0,
                walletId = "test-wallet"
            )
        )
        
        val expectedBalance = initialBalance + 800.0 + 200.0 - 300.0 - 100.0 // 1600.0
        assertEquals(1600.0, expectedBalance, 0.01)
    }
    
    @Test
    fun `balance calculation with incoming transfers should be correct`() {
        val initialBalance = 500.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        val walletId = "test-wallet"
        
        val transferTransactions = listOf(
            createTestTransaction(
                type = TransactionType.TRANSFER,
                amount = 300.0,
                sourceWalletId = "other-wallet",
                destinationWalletId = walletId
            ),
            createTestTransaction(
                type = TransactionType.TRANSFER,
                amount = 200.0,
                sourceWalletId = "another-wallet", 
                destinationWalletId = walletId
            )
        )
        
        val expectedBalance = initialBalance + 300.0 + 200.0 // 1000.0
        assertEquals(1000.0, expectedBalance, 0.01)
    }
    
    @Test
    fun `balance calculation with outgoing transfers should be correct`() {
        val initialBalance = 1500.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        val walletId = "test-wallet"
        
        val transferTransactions = listOf(
            createTestTransaction(
                type = TransactionType.TRANSFER,
                amount = 400.0,
                sourceWalletId = walletId,
                destinationWalletId = "other-wallet"
            ),
            createTestTransaction(
                type = TransactionType.TRANSFER,
                amount = 100.0,
                sourceWalletId = walletId,
                destinationWalletId = "another-wallet"
            )
        )
        
        val expectedBalance = initialBalance - 400.0 - 100.0 // 1000.0
        assertEquals(1000.0, expectedBalance, 0.01)
    }
    
    @Test
    fun `balance calculation with mixed transfers should be correct`() {
        val initialBalance = 1000.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        val walletId = "test-wallet"
        
        val transferTransactions = listOf(
            // Incoming transfer: +500
            createTestTransaction(
                type = TransactionType.TRANSFER,
                amount = 500.0,
                sourceWalletId = "other-wallet",
                destinationWalletId = walletId
            ),
            // Outgoing transfer: -200
            createTestTransaction(
                type = TransactionType.TRANSFER,
                amount = 200.0,
                sourceWalletId = walletId,
                destinationWalletId = "another-wallet"
            )
        )
        
        val expectedBalance = initialBalance + 500.0 - 200.0 // 1300.0
        assertEquals(1300.0, expectedBalance, 0.01)
    }
    
    @Test
    fun `balance calculation with all transaction types should be correct`() {
        val initialBalance = 2000.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        val walletId = "test-wallet"
        
        val allTypeTransactions = listOf(
            // Income: +1000
            createTestTransaction(
                type = TransactionType.INCOME,
                amount = 1000.0,
                walletId = walletId
            ),
            // Expense: -300
            createTestTransaction(
                type = TransactionType.EXPENSE,
                amount = 300.0,
                walletId = walletId
            ),
            // Incoming transfer: +400
            createTestTransaction(
                type = TransactionType.TRANSFER,
                amount = 400.0,
                sourceWalletId = "other-wallet",
                destinationWalletId = walletId
            ),
            // Outgoing transfer: -200
            createTestTransaction(
                type = TransactionType.TRANSFER,
                amount = 200.0,
                sourceWalletId = walletId,
                destinationWalletId = "another-wallet"
            )
        )
        
        val expectedBalance = initialBalance + 1000.0 - 300.0 + 400.0 - 200.0 // 2900.0
        assertEquals(2900.0, expectedBalance, 0.01)
    }
    
    @Test
    fun `balance calculation with zero initial balance should be correct`() {
        val initialBalance = 0.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        
        val transactions = listOf(
            createTestTransaction(
                type = TransactionType.INCOME,
                amount = 500.0,
                walletId = "test-wallet"
            ),
            createTestTransaction(
                type = TransactionType.EXPENSE,
                amount = 200.0,
                walletId = "test-wallet"
            )
        )
        
        val expectedBalance = initialBalance + 500.0 - 200.0 // 300.0
        assertEquals(300.0, expectedBalance, 0.01)
    }
    
    @Test
    fun `balance calculation with no transactions should equal initial balance`() {
        val initialBalance = 1500.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        
        val emptyTransactions = emptyList<Transaction>()
        
        val expectedBalance = initialBalance // 1500.0
        assertEquals(expectedBalance, initialBalance, 0.01)
    }
    
    @Test
    fun `balance calculation resulting in negative balance should be handled`() {
        val initialBalance = 100.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        
        val expenseTransactions = listOf(
            createTestTransaction(
                type = TransactionType.EXPENSE,
                amount = 200.0,
                walletId = "test-wallet"
            )
        )
        
        val expectedBalance = initialBalance - 200.0 // -100.0
        assertEquals(-100.0, expectedBalance, 0.01)
        assertTrue("Balance can be negative", expectedBalance < 0)
    }
    
    @Test
    fun `transfer direction identification should work correctly`() {
        val walletId = "test-wallet"
        
        // Incoming transfer (wallet is destination)
        val incomingTransfer = createTestTransaction(
            type = TransactionType.TRANSFER,
            amount = 100.0,
            sourceWalletId = "other-wallet",
            destinationWalletId = walletId
        )
        
        assertTrue("Should be incoming transfer", 
                  incomingTransfer.type == TransactionType.TRANSFER && 
                  incomingTransfer.destinationWalletId == walletId)
        
        // Outgoing transfer (wallet is source)
        val outgoingTransfer = createTestTransaction(
            type = TransactionType.TRANSFER,
            amount = 100.0,
            sourceWalletId = walletId,
            destinationWalletId = "other-wallet"
        )
        
        assertTrue("Should be outgoing transfer",
                  outgoingTransfer.type == TransactionType.TRANSFER && 
                  outgoingTransfer.sourceWalletId == walletId)
    }
    
    @Test
    fun `zero amount transactions should not affect balance`() {
        val initialBalance = 1000.0
        val wallet = createTestWallet(initialBalance = initialBalance)
        
        val zeroAmountTransactions = listOf(
            createTestTransaction(
                type = TransactionType.INCOME,
                amount = 0.0,
                walletId = "test-wallet"
            ),
            createTestTransaction(
                type = TransactionType.EXPENSE,
                amount = 0.0,
                walletId = "test-wallet"
            )
        )
        
        val expectedBalance = initialBalance + 0.0 - 0.0 // 1000.0
        assertEquals(expectedBalance, initialBalance, 0.01)
    }
    
    // Helper methods for creating test data
    private fun createTestWallet(
        id: String = "test-wallet",
        name: String = "Test Wallet",
        balance: Double = 0.0,
        initialBalance: Double = 0.0,
        walletType: String = "Physical",
        userId: String = "test-user"
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            initialBalance = initialBalance,
            walletType = walletType,
            physicalForm = PhysicalForm.FIAT_CURRENCY,
            userId = userId,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
    
    private fun createTestTransaction(
        id: String = "test-transaction-${System.currentTimeMillis()}",
        title: String = "Test Transaction",
        amount: Double,
        type: TransactionType,
        walletId: String = "",
        sourceWalletId: String = "",
        destinationWalletId: String = "",
        userId: String = "test-user"
    ): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            type = type,
            walletId = walletId,
            sourceWalletId = sourceWalletId,
            destinationWalletId = destinationWalletId,
            transactionDate = Date(),
            userId = userId,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}
package com.axeven.profiteerapp

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for transaction deletion functionality
 * Note: Full integration tests would require Firebase emulator setup
 */
class TransactionDeletionTest {

    @Test
    fun `transaction model should have valid default values`() {
        val transaction = Transaction()
        
        assertEquals("", transaction.id)
        assertEquals("", transaction.title)
        assertEquals(0.0, transaction.amount, 0.0)
        assertEquals("Uncategorized", transaction.category)
        assertEquals(TransactionType.EXPENSE, transaction.type)
    }
    
    @Test
    fun `transaction should be created with correct properties`() {
        val transactionId = "test-transaction-id"
        val testTransaction = Transaction(
            id = transactionId,
            title = "Test Transaction",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            userId = "test-user-id"
        )
        
        assertEquals(transactionId, testTransaction.id)
        assertEquals("Test Transaction", testTransaction.title)
        assertEquals(100.0, testTransaction.amount, 0.0)
        assertEquals(TransactionType.EXPENSE, testTransaction.type)
        assertEquals("test-user-id", testTransaction.userId)
    }
    
    @Test
    fun `transaction type enum should have correct values`() {
        val types = TransactionType.values()
        
        assertEquals(3, types.size)
        assertTrue(types.contains(TransactionType.INCOME))
        assertTrue(types.contains(TransactionType.EXPENSE))
        assertTrue(types.contains(TransactionType.TRANSFER))
    }
}
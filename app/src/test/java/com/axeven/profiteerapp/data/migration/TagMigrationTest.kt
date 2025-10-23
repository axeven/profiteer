package com.axeven.profiteerapp.data.migration

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.utils.logging.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.*

/**
 * Test-Driven Development tests for TagMigration (Phase 4).
 *
 * Following TDD methodology - these tests define the expected behavior for
 * migrating existing transaction tags to normalized format.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TagMigrationTest {

    private lateinit var tagMigration: TagMigration
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var logger: Logger

    @Before
    fun setup() {
        transactionRepository = mock()
        logger = mock()
        tagMigration = TagMigration(transactionRepository, logger)
    }

    // ========================================
    // Migration Logic Tests
    // ========================================

    @Test
    fun `migrateTransactionTags should normalize tags with mixed case`() = runTest {
        // Arrange: Transaction with mixed-case tags
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test",
                tags = listOf("Food", "TRAVEL", "shopping"),
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))

        // Mock successful update
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Should update with normalized tags
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())

        verify(transactionRepository).updateTransaction(argThat { transaction ->
            transaction.id == "1" &&
            transaction.tags == listOf("food", "travel", "shopping")
        })
    }

    @Test
    fun `migrateTransactionTags should remove duplicate tags`() = runTest {
        // Arrange: Transaction with duplicate tags
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test",
                tags = listOf("food", "Food", "FOOD", "travel"),
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Duplicates removed
        assertTrue(result.isSuccess)
        verify(transactionRepository).updateTransaction(argThat { transaction ->
            transaction.tags == listOf("food", "travel")
        })
    }

    @Test
    fun `migrateTransactionTags should trim whitespace`() = runTest {
        // Arrange: Transaction with whitespace in tags
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test",
                tags = listOf("  food  ", " travel ", "shopping"),
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Whitespace trimmed
        assertTrue(result.isSuccess)
        verify(transactionRepository).updateTransaction(argThat { transaction ->
            transaction.tags == listOf("food", "travel", "shopping")
        })
    }

    @Test
    fun `migrateTransactionTags should remove Untagged keyword`() = runTest {
        // Arrange: Transaction with "Untagged" keyword
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test",
                tags = listOf("food", "Untagged", "travel", "UNTAGGED"),
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: "Untagged" removed
        assertTrue(result.isSuccess)
        verify(transactionRepository).updateTransaction(argThat { transaction ->
            transaction.tags == listOf("food", "travel")
        })
    }

    @Test
    fun `migrateTransactionTags should skip already normalized tags`() = runTest {
        // Arrange: Transaction with already normalized tags
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test",
                tags = listOf("food", "travel", "shopping"),
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: No update needed, skip transaction
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()) // 0 transactions updated
        verify(transactionRepository, never()).updateTransaction(anyOrNull())
    }

    @Test
    fun `migrateTransactionTags should handle empty tags`() = runTest {
        // Arrange: Transaction with empty tags
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test",
                tags = emptyList(),
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Skip transaction with empty tags
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
        verify(transactionRepository, never()).updateTransaction(anyOrNull())
    }

    @Test
    fun `migrateTransactionTags should handle blank tags`() = runTest {
        // Arrange: Transaction with blank tags
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test",
                tags = listOf("", "   ", "food", ""),
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Blank tags filtered out
        assertTrue(result.isSuccess)
        verify(transactionRepository).updateTransaction(argThat { transaction ->
            transaction.tags == listOf("food")
        })
    }

    @Test
    fun `migrateTransactionTags should report correct count`() = runTest {
        // Arrange: Multiple transactions, some need migration, some don't
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("Food", "TRAVEL"), userId = "user1"),
            Transaction(id = "2", title = "T2", tags = listOf("food", "travel"), userId = "user1"), // Already normalized
            Transaction(id = "3", title = "T3", tags = listOf("Shopping", "food"), userId = "user1")
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Should report 2 transactions updated (1 and 3)
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
        verify(transactionRepository, times(2)).updateTransaction(any())
    }

    @Test
    fun `migrateTransactionTags should handle repository errors gracefully`() = runTest {
        // Arrange: Transaction that will fail to update
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test",
                tags = listOf("Food", "Travel"),
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any())).thenReturn(
            Result.failure(Exception("Update failed"))
        )

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Should return failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Migration failed") == true)
    }

    @Test
    fun `migrateTransactionTags should handle no transactions`() = runTest {
        // Arrange: User with no transactions
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(emptyList()))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Success with 0 count
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
        verify(transactionRepository, never()).updateTransaction(anyOrNull())
    }

    @Test
    fun `migrateTransactionTags should preserve other transaction fields`() = runTest {
        // Arrange: Transaction with various fields
        val originalDate = Date()
        val transactions = listOf(
            Transaction(
                id = "1",
                title = "Test Transaction",
                amount = 100.0,
                category = "Test Category",
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1", "wallet2"),
                tags = listOf("Food", "TRAVEL"),
                transactionDate = originalDate,
                userId = "user1"
            )
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Only tags should change
        assertTrue(result.isSuccess)
        verify(transactionRepository).updateTransaction(argThat { transaction ->
            transaction.id == "1" &&
            transaction.title == "Test Transaction" &&
            transaction.amount == 100.0 &&
            transaction.category == "Test Category" &&
            transaction.type == TransactionType.EXPENSE &&
            transaction.walletId == "wallet1" &&
            transaction.affectedWalletIds == listOf("wallet1", "wallet2") &&
            transaction.tags == listOf("food", "travel") && // Only tags normalized
            transaction.transactionDate == originalDate &&
            transaction.userId == "user1"
        })
    }

    @Test
    fun `migrateTransactionTags should handle large batch efficiently`() = runTest {
        // Arrange: Many transactions
        val transactions = (1..50).map { i ->
            Transaction(
                id = "transaction-$i",
                title = "T$i",
                tags = listOf("Food$i", "TRAVEL$i"),
                userId = "user1"
            )
        }
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        val result = tagMigration.migrateTransactionTags("user1")

        // Assert: Should successfully migrate all
        assertTrue(result.isSuccess)
        assertEquals(50, result.getOrNull())
        verify(transactionRepository, times(50)).updateTransaction(any())
    }

    @Test
    fun `migrateTransactionTags should log progress`() = runTest {
        // Arrange
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("Food"), userId = "user1")
        )
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(transactionRepository.updateTransaction(any<Transaction>())).thenReturn(Result.success(Unit))

        // Act
        tagMigration.migrateTransactionTags("user1")

        // Assert: Should log start and completion
        verify(logger, atLeastOnce()).i(eq("TagMigration"), argThat { contains("Starting") })
        verify(logger, atLeastOnce()).i(eq("TagMigration"), argThat { contains("completed") })
    }
}

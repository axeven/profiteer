package com.axeven.profiteerapp.data.ui

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.utils.TransactionWithBalances
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Unit tests for DiscrepancyDebugUiState.
 * Tests the consolidated state pattern for the discrepancy debug screen.
 */
class DiscrepancyDebugUiStateTest {

    @Test
    fun `initial state should have default values`() {
        val state = DiscrepancyDebugUiState()

        assertTrue(state.isLoading)
        assertEquals(0, state.transactions.size)
        assertEquals(null, state.firstDiscrepancyId)
        assertEquals(0.0, state.currentDiscrepancy, 0.001)
        assertEquals(0.0, state.totalPhysicalBalance, 0.001)
        assertEquals(0.0, state.totalLogicalBalance, 0.001)
        assertEquals(null, state.error)
    }

    @Test
    fun `withTransactions should update state immutably`() {
        val original = DiscrepancyDebugUiState()
        val transactions = listOf(
            createTransactionWithBalances("t1", 100.0, 100.0, false)
        )

        val updated = original.withTransactions(
            transactions = transactions,
            firstDiscrepancyId = "t1",
            physicalTotal = 100.0,
            logicalTotal = 100.0
        )

        assertNotSame(original, updated)
        assertEquals(0, original.transactions.size)
        assertEquals(1, updated.transactions.size)
        assertFalse(updated.isLoading)
    }

    @Test
    fun `withTransactions should calculate discrepancy correctly`() {
        val transactions = listOf(
            createTransactionWithBalances("t1", 150.0, 100.0, true)
        )

        val state = DiscrepancyDebugUiState().withTransactions(
            transactions = transactions,
            firstDiscrepancyId = "t1",
            physicalTotal = 150.0,
            logicalTotal = 100.0
        )

        assertEquals(50.0, state.currentDiscrepancy, 0.001)
        assertEquals(150.0, state.totalPhysicalBalance, 0.001)
        assertEquals(100.0, state.totalLogicalBalance, 0.001)
        assertEquals("t1", state.firstDiscrepancyId)
    }

    @Test
    fun `withTransactions should handle negative discrepancy`() {
        val transactions = listOf(
            createTransactionWithBalances("t1", 100.0, 150.0, true)
        )

        val state = DiscrepancyDebugUiState().withTransactions(
            transactions = transactions,
            firstDiscrepancyId = "t1",
            physicalTotal = 100.0,
            logicalTotal = 150.0
        )

        assertEquals(-50.0, state.currentDiscrepancy, 0.001)
    }

    @Test
    fun `withError should set error state`() {
        val original = DiscrepancyDebugUiState()

        val updated = original.withError("Test error message")

        assertNotSame(original, updated)
        assertEquals("Test error message", updated.error)
        assertFalse(updated.isLoading)
    }

    @Test
    fun `withError should be immutable`() {
        val original = DiscrepancyDebugUiState()

        val updated = original.withError("Error")

        assertEquals(null, original.error)
        assertEquals("Error", updated.error)
    }

    @Test
    fun `hasDiscrepancy should return true when discrepancy exists`() {
        val state = DiscrepancyDebugUiState().withTransactions(
            transactions = emptyList(),
            firstDiscrepancyId = null,
            physicalTotal = 100.0,
            logicalTotal = 95.0
        )

        assertTrue(state.hasDiscrepancy)
    }

    @Test
    fun `hasDiscrepancy should return false when balances match`() {
        val state = DiscrepancyDebugUiState().withTransactions(
            transactions = emptyList(),
            firstDiscrepancyId = null,
            physicalTotal = 100.0,
            logicalTotal = 100.0
        )

        assertFalse(state.hasDiscrepancy)
    }

    @Test
    fun `hasDiscrepancy should handle floating point tolerance`() {
        val state = DiscrepancyDebugUiState().withTransactions(
            transactions = emptyList(),
            firstDiscrepancyId = null,
            physicalTotal = 100.0,
            logicalTotal = 100.005 // Within tolerance
        )

        assertFalse(state.hasDiscrepancy)
    }

    @Test
    fun `multiple withTransactions calls should be chainable`() {
        val state1 = DiscrepancyDebugUiState()
        val transactions1 = listOf(createTransactionWithBalances("t1", 100.0, 100.0, false))

        val state2 = state1.withTransactions(
            transactions = transactions1,
            firstDiscrepancyId = null,
            physicalTotal = 100.0,
            logicalTotal = 100.0
        )

        val transactions2 = listOf(
            createTransactionWithBalances("t1", 100.0, 100.0, false),
            createTransactionWithBalances("t2", 150.0, 100.0, true)
        )

        val state3 = state2.withTransactions(
            transactions = transactions2,
            firstDiscrepancyId = "t2",
            physicalTotal = 150.0,
            logicalTotal = 100.0
        )

        assertEquals(1, state2.transactions.size)
        assertEquals(2, state3.transactions.size)
        assertEquals("t2", state3.firstDiscrepancyId)
    }

    @Test
    fun `withTransactions with empty list should work`() {
        val state = DiscrepancyDebugUiState().withTransactions(
            transactions = emptyList(),
            firstDiscrepancyId = null,
            physicalTotal = 0.0,
            logicalTotal = 0.0
        )

        assertEquals(0, state.transactions.size)
        assertFalse(state.isLoading)
        assertEquals(0.0, state.currentDiscrepancy, 0.001)
    }

    @Test
    fun `isBalanced should return true when no discrepancy`() {
        val state = DiscrepancyDebugUiState().withTransactions(
            transactions = emptyList(),
            firstDiscrepancyId = null,
            physicalTotal = 100.0,
            logicalTotal = 100.0
        )

        assertTrue(state.isBalanced)
    }

    @Test
    fun `isBalanced should return false when discrepancy exists`() {
        val state = DiscrepancyDebugUiState().withTransactions(
            transactions = emptyList(),
            firstDiscrepancyId = "t1",
            physicalTotal = 150.0,
            logicalTotal = 100.0
        )

        assertFalse(state.isBalanced)
    }

    // Helper functions

    private fun createTransactionWithBalances(
        id: String,
        physicalBalance: Double,
        logicalBalance: Double,
        isFirstDiscrepancy: Boolean
    ): TransactionWithBalances {
        val transaction = Transaction(
            id = id,
            title = "Test Transaction",
            amount = 100.0,
            type = TransactionType.INCOME,
            userId = "user123",
            transactionDate = Date()
        )

        return TransactionWithBalances(
            transaction = transaction,
            physicalBalanceAfter = physicalBalance,
            logicalBalanceAfter = logicalBalance,
            isFirstDiscrepancy = isFirstDiscrepancy
        )
    }
}

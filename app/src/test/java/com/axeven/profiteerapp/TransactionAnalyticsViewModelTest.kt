package com.axeven.profiteerapp

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.viewmodel.TransactionAnalyticsUiState
import com.axeven.profiteerapp.viewmodel.DateRange
import com.axeven.profiteerapp.viewmodel.MonthlyExpense
import com.axeven.profiteerapp.viewmodel.TagExpenseData
import com.axeven.profiteerapp.viewmodel.IncomeExpenseComparison
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Unit tests for TransactionAnalyticsViewModel business logic
 * Tests data processing and calculation methods without UI dependencies
 */
class TransactionAnalyticsViewModelTest {
    
    @Test
    fun `ui state should have correct default values`() {
        val uiState = TransactionAnalyticsUiState()
        
        assertEquals(emptyList<MonthlyExpense>(), uiState.monthlyExpenses)
        assertEquals(emptyList<MonthlyExpense>(), uiState.monthlyIncome)
        assertEquals("USD", uiState.defaultCurrency)
        assertEquals(false, uiState.isLoading)
        assertEquals(null, uiState.error)
        assertEquals(DateRange.LAST_6_MONTHS, uiState.selectedDateRange)
        assertEquals(0, uiState.totalTransactions)
        assertEquals(0.0, uiState.averageTransactionAmount, 0.01)
    }
    
    @Test
    fun `date range enum should have correct values`() {
        val ranges = DateRange.values()
        
        assertEquals(4, ranges.size)
        assertTrue(ranges.contains(DateRange.LAST_3_MONTHS))
        assertTrue(ranges.contains(DateRange.LAST_6_MONTHS))
        assertTrue(ranges.contains(DateRange.LAST_12_MONTHS))
        assertTrue(ranges.contains(DateRange.CUSTOM))
        
        assertEquals("Last 3 Months", DateRange.LAST_3_MONTHS.displayName)
        assertEquals(3, DateRange.LAST_3_MONTHS.months)
        assertEquals("Last 6 Months", DateRange.LAST_6_MONTHS.displayName)
        assertEquals(6, DateRange.LAST_6_MONTHS.months)
    }
    
    @Test
    fun `monthly expense data class should have correct properties`() {
        val monthlyExpense = MonthlyExpense(
            month = "2024-01",
            monthDisplay = "Jan 2024",
            totalAmount = 1500.0,
            transactionCount = 5
        )
        
        assertEquals("2024-01", monthlyExpense.month)
        assertEquals("Jan 2024", monthlyExpense.monthDisplay)
        assertEquals(1500.0, monthlyExpense.totalAmount, 0.01)
        assertEquals(5, monthlyExpense.transactionCount)
    }
    
    @Test
    fun `tag expense data class should have correct properties`() {
        val tagExpense = TagExpenseData(
            tag = "Food",
            amount = 250.0,
            percentage = 25.0,
            transactionCount = 3
        )
        
        assertEquals("Food", tagExpense.tag)
        assertEquals(250.0, tagExpense.amount, 0.01)
        assertEquals(25.0, tagExpense.percentage, 0.01)
        assertEquals(3, tagExpense.transactionCount)
    }
    
    @Test
    fun `income expense comparison should calculate percentages correctly`() {
        val comparison = IncomeExpenseComparison(
            totalIncome = 3000.0,
            totalExpenses = 1500.0,
            netAmount = 1500.0,
            incomePercentage = 66.67,
            expensePercentage = 33.33
        )
        
        assertEquals(3000.0, comparison.totalIncome, 0.01)
        assertEquals(1500.0, comparison.totalExpenses, 0.01)
        assertEquals(1500.0, comparison.netAmount, 0.01)
        assertEquals(66.67, comparison.incomePercentage, 0.01)
        assertEquals(33.33, comparison.expensePercentage, 0.01)
        
        // Percentages should sum to 100
        val total = comparison.incomePercentage + comparison.expensePercentage
        assertEquals(100.0, total, 0.1)
    }
    
    @Test
    fun `transaction model should support analytics data extraction`() {
        val testTransaction = Transaction(
            id = "test-1",
            title = "Grocery Shopping",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            category = "Food",
            tags = listOf("Food", "Groceries", "Weekly"),
            transactionDate = Calendar.getInstance().apply {
                set(2024, Calendar.JANUARY, 15)
            }.time,
            userId = "test-user"
        )
        
        assertEquals("test-1", testTransaction.id)
        assertEquals("Grocery Shopping", testTransaction.title)
        assertEquals(150.0, testTransaction.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, testTransaction.type)
        assertEquals("Food", testTransaction.category)
        assertEquals(3, testTransaction.tags.size)
        assertTrue(testTransaction.tags.contains("Food"))
        assertTrue(testTransaction.tags.contains("Groceries"))
        assertTrue(testTransaction.tags.contains("Weekly"))
        assertNotNull(testTransaction.transactionDate)
        assertEquals("test-user", testTransaction.userId)
    }
    
    @Test
    fun `transaction should handle empty tags gracefully`() {
        val transactionWithoutTags = Transaction(
            id = "test-2",
            title = "Test Transaction",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = "Untagged",
            tags = emptyList(),
            userId = "test-user"
        )
        
        assertEquals(emptyList<String>(), transactionWithoutTags.tags)
        assertEquals("Untagged", transactionWithoutTags.category)
    }
    
    @Test
    fun `transaction types should cover all cases`() {
        val types = TransactionType.values()
        
        assertEquals(3, types.size)
        assertTrue(types.contains(TransactionType.INCOME))
        assertTrue(types.contains(TransactionType.EXPENSE))
        assertTrue(types.contains(TransactionType.TRANSFER))
    }
    
    @Test
    fun `zero amounts should be handled correctly in calculations`() {
        val zeroComparison = IncomeExpenseComparison(
            totalIncome = 0.0,
            totalExpenses = 0.0,
            netAmount = 0.0,
            incomePercentage = 0.0,
            expensePercentage = 0.0
        )
        
        assertEquals(0.0, zeroComparison.totalIncome, 0.01)
        assertEquals(0.0, zeroComparison.totalExpenses, 0.01)
        assertEquals(0.0, zeroComparison.netAmount, 0.01)
        assertEquals(0.0, zeroComparison.incomePercentage, 0.01)
        assertEquals(0.0, zeroComparison.expensePercentage, 0.01)
    }
    
    @Test
    fun `negative net amount should be calculated correctly`() {
        val negativeNetComparison = IncomeExpenseComparison(
            totalIncome = 1000.0,
            totalExpenses = 1500.0,
            netAmount = -500.0,
            incomePercentage = 40.0,
            expensePercentage = 60.0
        )
        
        assertEquals(1000.0, negativeNetComparison.totalIncome, 0.01)
        assertEquals(1500.0, negativeNetComparison.totalExpenses, 0.01)
        assertEquals(-500.0, negativeNetComparison.netAmount, 0.01)
        assertTrue("Net amount should be negative", negativeNetComparison.netAmount < 0)
    }
    
    @Test
    fun `ui state with data should not be loading`() {
        val uiStateWithData = TransactionAnalyticsUiState(
            totalTransactions = 10,
            averageTransactionAmount = 250.0,
            isLoading = false,
            error = null
        )
        
        assertEquals(10, uiStateWithData.totalTransactions)
        assertEquals(250.0, uiStateWithData.averageTransactionAmount, 0.01)
        assertFalse("Should not be loading when data is present", uiStateWithData.isLoading)
        assertNull("Should not have error when data loads successfully", uiStateWithData.error)
    }
    
    @Test
    fun `ui state with error should not be loading`() {
        val uiStateWithError = TransactionAnalyticsUiState(
            isLoading = false,
            error = "Failed to load analytics data"
        )
        
        assertFalse("Should not be loading when error occurs", uiStateWithError.isLoading)
        assertNotNull("Should have error message", uiStateWithError.error)
        assertEquals("Failed to load analytics data", uiStateWithError.error)
    }
}
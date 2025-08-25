package com.axeven.profiteerapp

import com.axeven.profiteerapp.viewmodel.ChartDataType
import com.axeven.profiteerapp.viewmodel.ReportUiState
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ReportViewModel expense and income transaction by tag feature
 * Tests enum values and UI state structure
 */
class ReportViewModelTransactionsByTagTest {

    @Test
    fun `ChartDataType should include expense and income transaction by tag options`() {
        val dataTypes = ChartDataType.values()
        
        assertTrue("EXPENSE_TRANSACTION_BY_TAG should be in ChartDataType enum", 
            dataTypes.contains(ChartDataType.EXPENSE_TRANSACTION_BY_TAG))
        
        assertTrue("INCOME_TRANSACTION_BY_TAG should be in ChartDataType enum", 
            dataTypes.contains(ChartDataType.INCOME_TRANSACTION_BY_TAG))
        
        assertEquals("Should have 5 chart data types", 5, dataTypes.size)
    }
    
    @Test
    fun `ReportUiState should have expense and income transaction by tag fields`() {
        val uiState = ReportUiState()
        
        assertNotNull("expenseTransactionsByTag should not be null", uiState.expenseTransactionsByTag)
        assertTrue("expenseTransactionsByTag should be empty by default", uiState.expenseTransactionsByTag.isEmpty())
        assertEquals("totalExpensesByTag should be 0.0 by default", 0.0, uiState.totalExpensesByTag, 0.01)
        
        assertNotNull("incomeTransactionsByTag should not be null", uiState.incomeTransactionsByTag)
        assertTrue("incomeTransactionsByTag should be empty by default", uiState.incomeTransactionsByTag.isEmpty())
        assertEquals("totalIncomeByTag should be 0.0 by default", 0.0, uiState.totalIncomeByTag, 0.01)
    }
    
    @Test
    fun `ReportUiState should handle expense transaction by tag data correctly`() {
        val expenseTagData = mapOf("food" to 100.0, "transport" to 50.0, "utilities" to 75.0)
        val expenseTotal = expenseTagData.values.sum()
        
        val uiState = ReportUiState(
            expenseTransactionsByTag = expenseTagData,
            totalExpensesByTag = expenseTotal,
            selectedChartDataType = ChartDataType.EXPENSE_TRANSACTION_BY_TAG
        )
        
        assertEquals("Should have correct expense tag data", expenseTagData, uiState.expenseTransactionsByTag)
        assertEquals("Should have correct expense total", expenseTotal, uiState.totalExpensesByTag, 0.01)
        assertEquals("Should have correct selected chart type", ChartDataType.EXPENSE_TRANSACTION_BY_TAG, uiState.selectedChartDataType)
    }
    
    @Test
    fun `ReportUiState should handle income transaction by tag data correctly`() {
        val incomeTagData = mapOf("salary" to 5000.0, "freelance" to 1200.0, "investment" to 300.0)
        val incomeTotal = incomeTagData.values.sum()
        
        val uiState = ReportUiState(
            incomeTransactionsByTag = incomeTagData,
            totalIncomeByTag = incomeTotal,
            selectedChartDataType = ChartDataType.INCOME_TRANSACTION_BY_TAG
        )
        
        assertEquals("Should have correct income tag data", incomeTagData, uiState.incomeTransactionsByTag)
        assertEquals("Should have correct income total", incomeTotal, uiState.totalIncomeByTag, 0.01)
        assertEquals("Should have correct selected chart type", ChartDataType.INCOME_TRANSACTION_BY_TAG, uiState.selectedChartDataType)
    }
    
    @Test
    fun `Should verify expense and income transaction by tag chart data type values`() {
        assertEquals("EXPENSE_TRANSACTION_BY_TAG should have correct string representation", 
            "EXPENSE_TRANSACTION_BY_TAG", ChartDataType.EXPENSE_TRANSACTION_BY_TAG.name)
        
        assertEquals("INCOME_TRANSACTION_BY_TAG should have correct string representation", 
            "INCOME_TRANSACTION_BY_TAG", ChartDataType.INCOME_TRANSACTION_BY_TAG.name)
    }
}
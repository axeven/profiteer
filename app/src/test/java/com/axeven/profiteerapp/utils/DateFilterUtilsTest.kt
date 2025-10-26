package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.DateFilterPeriod
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateFilterUtilsTest {

    // Helper function to create a date
    private fun createDate(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): Date {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, second)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // Helper function to create a transaction with a specific date
    private fun createTransaction(
        id: String = "t1",
        transactionDate: Date? = null
    ): Transaction {
        return Transaction(
            id = id,
            title = "Test Transaction",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            transactionDate = transactionDate,
            userId = "user1"
        )
    }

    // ========== getMonthStart tests ==========

    @Test
    fun `getMonthStart returns first day of month at 00-00-00`() {
        val date = DateFilterUtils.getMonthStart(2025, 10)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getMonthStart handles January correctly`() {
        val date = DateFilterUtils.getMonthStart(2025, 1)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getMonthStart handles December correctly`() {
        val date = DateFilterUtils.getMonthStart(2025, 12)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    // ========== getMonthEnd tests ==========

    @Test
    fun `getMonthEnd returns last day of month at 23-59-59-999`() {
        val date = DateFilterUtils.getMonthEnd(2025, 10)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendar.get(Calendar.MINUTE))
        assertEquals(59, calendar.get(Calendar.SECOND))
        assertEquals(999, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getMonthEnd handles January (31 days) correctly`() {
        val date = DateFilterUtils.getMonthEnd(2025, 1)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getMonthEnd handles February non-leap year (28 days) correctly`() {
        val date = DateFilterUtils.getMonthEnd(2025, 2)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(28, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getMonthEnd handles February leap year (29 days) correctly`() {
        val date = DateFilterUtils.getMonthEnd(2024, 2)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(29, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getMonthEnd handles April (30 days) correctly`() {
        val date = DateFilterUtils.getMonthEnd(2025, 4)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(30, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getMonthEnd handles December (31 days) correctly`() {
        val date = DateFilterUtils.getMonthEnd(2025, 12)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH))
    }

    // ========== getYearStart tests ==========

    @Test
    fun `getYearStart returns January 1st at 00-00-00`() {
        val date = DateFilterUtils.getYearStart(2025)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    // ========== getYearEnd tests ==========

    @Test
    fun `getYearEnd returns December 31st at 23-59-59-999`() {
        val date = DateFilterUtils.getYearEnd(2025)
        val calendar = Calendar.getInstance().apply { time = date }

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH))
        assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendar.get(Calendar.MINUTE))
        assertEquals(59, calendar.get(Calendar.SECOND))
        assertEquals(999, calendar.get(Calendar.MILLISECOND))
    }

    // ========== isTransactionInRange tests ==========

    @Test
    fun `isTransactionInRange includes transaction on start date`() {
        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val transaction = createTransaction(transactionDate = createDate(2025, 10, 1, 0, 0, 0))

        assertTrue(DateFilterUtils.isTransactionInRange(transaction, startDate, endDate))
    }

    @Test
    fun `isTransactionInRange includes transaction on end date`() {
        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val transaction = createTransaction(transactionDate = createDate(2025, 10, 31, 23, 59, 59))

        assertTrue(DateFilterUtils.isTransactionInRange(transaction, startDate, endDate))
    }

    @Test
    fun `isTransactionInRange includes transaction in middle of range`() {
        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val transaction = createTransaction(transactionDate = createDate(2025, 10, 15, 12, 0, 0))

        assertTrue(DateFilterUtils.isTransactionInRange(transaction, startDate, endDate))
    }

    @Test
    fun `isTransactionInRange excludes transaction before start date`() {
        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val transaction = createTransaction(transactionDate = createDate(2025, 9, 30, 23, 59, 59))

        assertFalse(DateFilterUtils.isTransactionInRange(transaction, startDate, endDate))
    }

    @Test
    fun `isTransactionInRange excludes transaction after end date`() {
        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val transaction = createTransaction(transactionDate = createDate(2025, 11, 1, 0, 0, 0))

        assertFalse(DateFilterUtils.isTransactionInRange(transaction, startDate, endDate))
    }

    @Test
    fun `isTransactionInRange handles null dates (all time) includes all`() {
        val transaction = createTransaction(transactionDate = createDate(2025, 10, 15, 12, 0, 0))

        assertTrue(DateFilterUtils.isTransactionInRange(transaction, null, null))
    }

    @Test
    fun `isTransactionInRange excludes null transactionDate`() {
        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val transaction = createTransaction(transactionDate = null)

        assertFalse(DateFilterUtils.isTransactionInRange(transaction, startDate, endDate))
    }

    @Test
    fun `isTransactionInRange excludes null transactionDate even for all time`() {
        val transaction = createTransaction(transactionDate = null)

        assertFalse(DateFilterUtils.isTransactionInRange(transaction, null, null))
    }

    @Test
    fun `isTransactionInRange with only startDate includes transaction after start`() {
        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val transaction = createTransaction(transactionDate = createDate(2025, 10, 15, 12, 0, 0))

        assertTrue(DateFilterUtils.isTransactionInRange(transaction, startDate, null))
    }

    @Test
    fun `isTransactionInRange with only endDate includes transaction before end`() {
        val endDate = createDate(2025, 10, 31, 23, 59, 59)
        val transaction = createTransaction(transactionDate = createDate(2025, 10, 15, 12, 0, 0))

        assertTrue(DateFilterUtils.isTransactionInRange(transaction, null, endDate))
    }

    // ========== filterTransactionsByDateRange tests ==========

    @Test
    fun `filterTransactionsByDateRange filters transactions within range`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 10, 1, 12, 0, 0)),
            createTransaction("t2", createDate(2025, 10, 15, 12, 0, 0)),
            createTransaction("t3", createDate(2025, 10, 31, 12, 0, 0)),
            createTransaction("t4", createDate(2025, 9, 30, 12, 0, 0)),
            createTransaction("t5", createDate(2025, 11, 1, 12, 0, 0))
        )

        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val endDate = createDate(2025, 10, 31, 23, 59, 59)

        val result = DateFilterUtils.filterTransactionsByDateRange(transactions, startDate, endDate)

        assertEquals(3, result.size)
        assertTrue(result.any { it.id == "t1" })
        assertTrue(result.any { it.id == "t2" })
        assertTrue(result.any { it.id == "t3" })
    }

    @Test
    fun `filterTransactionsByDateRange excludes null transactionDate`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 10, 15, 12, 0, 0)),
            createTransaction("t2", null)
        )

        val startDate = createDate(2025, 10, 1, 0, 0, 0)
        val endDate = createDate(2025, 10, 31, 23, 59, 59)

        val result = DateFilterUtils.filterTransactionsByDateRange(transactions, startDate, endDate)

        assertEquals(1, result.size)
        assertEquals("t1", result[0].id)
    }

    @Test
    fun `filterTransactionsByDateRange with null dates returns all non-null transactions`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 10, 15, 12, 0, 0)),
            createTransaction("t2", createDate(2023, 1, 1, 12, 0, 0)),
            createTransaction("t3", null)
        )

        val result = DateFilterUtils.filterTransactionsByDateRange(transactions, null, null)

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "t1" })
        assertTrue(result.any { it.id == "t2" })
    }

    @Test
    fun `filterTransactionsByDateRange handles empty list`() {
        val result = DateFilterUtils.filterTransactionsByDateRange(
            emptyList(),
            createDate(2025, 10, 1, 0, 0, 0),
            createDate(2025, 10, 31, 23, 59, 59)
        )

        assertTrue(result.isEmpty())
    }

    // ========== filterTransactionsByDate (with DateFilterPeriod) tests ==========

    @Test
    fun `filterTransactionsByDate AllTime returns all transactions excluding null dates`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 10, 15, 12, 0, 0)),
            createTransaction("t2", createDate(2024, 1, 1, 12, 0, 0)),
            createTransaction("t3", null)
        )

        val result = DateFilterUtils.filterTransactionsByDate(transactions, DateFilterPeriod.AllTime)

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "t1" })
        assertTrue(result.any { it.id == "t2" })
    }

    @Test
    fun `filterTransactionsByDate Month filters correctly`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 10, 1, 0, 0, 0)),
            createTransaction("t2", createDate(2025, 10, 15, 12, 0, 0)),
            createTransaction("t3", createDate(2025, 10, 31, 23, 59, 59)),
            createTransaction("t4", createDate(2025, 9, 30, 23, 59, 59)),
            createTransaction("t5", createDate(2025, 11, 1, 0, 0, 0))
        )

        val result = DateFilterUtils.filterTransactionsByDate(
            transactions,
            DateFilterPeriod.Month(2025, 10)
        )

        assertEquals(3, result.size)
        assertTrue(result.any { it.id == "t1" })
        assertTrue(result.any { it.id == "t2" })
        assertTrue(result.any { it.id == "t3" })
    }

    @Test
    fun `filterTransactionsByDate Year filters correctly`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 1, 1, 0, 0, 0)),
            createTransaction("t2", createDate(2025, 6, 15, 12, 0, 0)),
            createTransaction("t3", createDate(2025, 12, 31, 23, 59, 59)),
            createTransaction("t4", createDate(2024, 12, 31, 23, 59, 59)),
            createTransaction("t5", createDate(2026, 1, 1, 0, 0, 0))
        )

        val result = DateFilterUtils.filterTransactionsByDate(
            transactions,
            DateFilterPeriod.Year(2025)
        )

        assertEquals(3, result.size)
        assertTrue(result.any { it.id == "t1" })
        assertTrue(result.any { it.id == "t2" })
        assertTrue(result.any { it.id == "t3" })
    }

    @Test
    fun `filterTransactionsByDate Month excludes null transactionDate`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 10, 15, 12, 0, 0)),
            createTransaction("t2", null)
        )

        val result = DateFilterUtils.filterTransactionsByDate(
            transactions,
            DateFilterPeriod.Month(2025, 10)
        )

        assertEquals(1, result.size)
        assertEquals("t1", result[0].id)
    }

    @Test
    fun `filterTransactionsByDate handles February leap year boundary`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2024, 2, 29, 12, 0, 0)),
            createTransaction("t2", createDate(2024, 3, 1, 0, 0, 0))
        )

        val result = DateFilterUtils.filterTransactionsByDate(
            transactions,
            DateFilterPeriod.Month(2024, 2)
        )

        assertEquals(1, result.size)
        assertEquals("t1", result[0].id)
    }

    @Test
    fun `filterTransactionsByDate handles month boundaries for 30-day month`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 4, 30, 23, 59, 59)),
            createTransaction("t2", createDate(2025, 5, 1, 0, 0, 0))
        )

        val result = DateFilterUtils.filterTransactionsByDate(
            transactions,
            DateFilterPeriod.Month(2025, 4)
        )

        assertEquals(1, result.size)
        assertEquals("t1", result[0].id)
    }

    @Test
    fun `filterTransactionsByDate handles year boundaries`() {
        val transactions = listOf(
            createTransaction("t1", createDate(2025, 12, 31, 23, 59, 59)),
            createTransaction("t2", createDate(2026, 1, 1, 0, 0, 0))
        )

        val result = DateFilterUtils.filterTransactionsByDate(
            transactions,
            DateFilterPeriod.Year(2025)
        )

        assertEquals(1, result.size)
        assertEquals("t1", result[0].id)
    }

    @Test
    fun `filterTransactionsByDate handles empty list`() {
        val result = DateFilterUtils.filterTransactionsByDate(
            emptyList(),
            DateFilterPeriod.Month(2025, 10)
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterTransactionsByDate handles all null transactionDates`() {
        val transactions = listOf(
            createTransaction("t1", null),
            createTransaction("t2", null)
        )

        val result = DateFilterUtils.filterTransactionsByDate(
            transactions,
            DateFilterPeriod.Month(2025, 10)
        )

        assertTrue(result.isEmpty())
    }
}

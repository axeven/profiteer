package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.DateFilterPeriod
import com.axeven.profiteerapp.data.model.Transaction
import java.util.*

/**
 * Utility class for filtering transactions by date.
 *
 * This class provides functions for:
 * - Getting month/year start and end dates
 * - Checking if a transaction falls within a date range
 * - Filtering transactions by date range or DateFilterPeriod
 *
 * Important: All filtering excludes transactions with null transactionDate.
 */
object DateFilterUtils {

    /**
     * Returns the start date of a month (first day at 00:00:00.000).
     *
     * @param year The year (e.g., 2025)
     * @param month The month (1-12, where 1 = January)
     * @return Date representing the start of the month
     */
    fun getMonthStart(year: Int, month: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Returns the end date of a month (last day at 23:59:59.999).
     *
     * @param year The year (e.g., 2025)
     * @param month The month (1-12, where 1 = January)
     * @return Date representing the end of the month
     */
    fun getMonthEnd(year: Int, month: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return calendar.time
    }

    /**
     * Returns the start date of a year (January 1st at 00:00:00.000).
     *
     * @param year The year (e.g., 2025)
     * @return Date representing the start of the year
     */
    fun getYearStart(year: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Returns the end date of a year (December 31st at 23:59:59.999).
     *
     * @param year The year (e.g., 2025)
     * @return Date representing the end of the year
     */
    fun getYearEnd(year: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    /**
     * Checks if a transaction falls within the specified date range.
     *
     * Rules:
     * - Transactions with null transactionDate are always excluded (return false)
     * - If both startDate and endDate are null (All Time), includes all non-null transactions
     * - If only startDate is provided, includes transactions on or after startDate
     * - If only endDate is provided, includes transactions on or before endDate
     * - If both provided, includes transactions within range (inclusive)
     *
     * @param transaction The transaction to check
     * @param startDate The start date (inclusive), or null for no start limit
     * @param endDate The end date (inclusive), or null for no end limit
     * @return true if transaction is within range, false otherwise
     */
    fun isTransactionInRange(
        transaction: Transaction,
        startDate: Date?,
        endDate: Date?
    ): Boolean {
        // Exclude transactions with null transactionDate
        val transactionDate = transaction.transactionDate ?: return false

        // If both dates are null (All Time), include all non-null transactions
        if (startDate == null && endDate == null) {
            return true
        }

        // Check start date boundary
        if (startDate != null && transactionDate.before(startDate)) {
            return false
        }

        // Check end date boundary
        if (endDate != null && transactionDate.after(endDate)) {
            return false
        }

        return true
    }

    /**
     * Filters transactions by date range.
     *
     * @param transactions The list of transactions to filter
     * @param startDate The start date (inclusive), or null for no start limit
     * @param endDate The end date (inclusive), or null for no end limit
     * @return List of transactions within the date range (excluding null transactionDate)
     */
    fun filterTransactionsByDateRange(
        transactions: List<Transaction>,
        startDate: Date?,
        endDate: Date?
    ): List<Transaction> {
        return transactions.filter { transaction ->
            isTransactionInRange(transaction, startDate, endDate)
        }
    }

    /**
     * Filters transactions by DateFilterPeriod.
     *
     * @param transactions The list of transactions to filter
     * @param period The date filter period (AllTime, Month, or Year)
     * @return List of transactions within the period (excluding null transactionDate)
     */
    fun filterTransactionsByDate(
        transactions: List<Transaction>,
        period: DateFilterPeriod
    ): List<Transaction> {
        val (startDate, endDate) = period.getDateRange()
        return filterTransactionsByDateRange(transactions, startDate, endDate)
    }
}

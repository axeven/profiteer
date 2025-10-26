package com.axeven.profiteerapp.data.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents a date filter period for portfolio reports.
 *
 * This sealed class defines three types of filtering periods:
 * - AllTime: No filtering, shows all data
 * - Month: Filters data for a specific month and year
 * - Year: Filters data for a specific year
 */
sealed class DateFilterPeriod {

    /**
     * Returns the start and end dates for this period.
     * Returns (null, null) for AllTime.
     * Returns (startDate, endDate) for Month and Year periods.
     */
    abstract fun getDateRange(): Pair<Date?, Date?>

    /**
     * Returns a human-readable display text for this period.
     * Examples: "All Time", "October 2025", "2025"
     */
    abstract fun getDisplayText(): String

    /**
     * Represents all-time filtering (no date restriction).
     */
    object AllTime : DateFilterPeriod() {
        override fun getDateRange(): Pair<Date?, Date?> = Pair(null, null)

        override fun getDisplayText(): String = "All Time"
    }

    /**
     * Represents filtering by a specific month and year.
     *
     * @param year The year (e.g., 2025)
     * @param month The month (1-12, where 1 = January)
     */
    data class Month(val year: Int, val month: Int) : DateFilterPeriod() {
        override fun getDateRange(): Pair<Date?, Date?> {
            val calendar = Calendar.getInstance()

            // Start: First day of month at 00:00:00.000
            calendar.set(year, month - 1, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            // End: Last day of month at 23:59:59.999
            calendar.set(year, month - 1, 1, 23, 59, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endDate = calendar.time

            return Pair(startDate, endDate)
        }

        override fun getDisplayText(): String {
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, 1)

            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            return dateFormat.format(calendar.time)
        }
    }

    /**
     * Represents filtering by a specific year.
     *
     * @param year The year (e.g., 2025)
     */
    data class Year(val year: Int) : DateFilterPeriod() {
        override fun getDateRange(): Pair<Date?, Date?> {
            val calendar = Calendar.getInstance()

            // Start: January 1st at 00:00:00.000
            calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            // End: December 31st at 23:59:59.999
            calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endDate = calendar.time

            return Pair(startDate, endDate)
        }

        override fun getDisplayText(): String {
            return year.toString()
        }
    }
}

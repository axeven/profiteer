package com.axeven.profiteerapp.data.model

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateFilterPeriodTest {

    @Test
    fun `AllTime getDateRange returns null, null`() {
        val period = DateFilterPeriod.AllTime
        val (start, end) = period.getDateRange()

        assertNull(start)
        assertNull(end)
    }

    @Test
    fun `AllTime getDisplayText returns All Time`() {
        val period = DateFilterPeriod.AllTime

        assertEquals("All Time", period.getDisplayText())
    }

    @Test
    fun `Month getDateRange returns correct start date`() {
        val period = DateFilterPeriod.Month(2025, 10) // October 2025
        val (start, _) = period.getDateRange()

        assertNotNull(start)
        val calendar = Calendar.getInstance()
        calendar.time = start!!

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `Month getDateRange returns correct end date`() {
        val period = DateFilterPeriod.Month(2025, 10) // October 2025
        val (_, end) = period.getDateRange()

        assertNotNull(end)
        val calendar = Calendar.getInstance()
        calendar.time = end!!

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendar.get(Calendar.MINUTE))
        assertEquals(59, calendar.get(Calendar.SECOND))
        assertEquals(999, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `Month getDisplayText formats as MonthName Year`() {
        val period = DateFilterPeriod.Month(2025, 10) // October 2025
        val displayText = period.getDisplayText()

        // Should be "October 2025" in English locale
        assertTrue(displayText.contains("2025"))
        assertTrue(displayText.contains("Oct") || displayText.contains("October"))
    }

    @Test
    fun `Year getDateRange returns correct start date`() {
        val period = DateFilterPeriod.Year(2025)
        val (start, _) = period.getDateRange()

        assertNotNull(start)
        val calendar = Calendar.getInstance()
        calendar.time = start!!

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `Year getDateRange returns correct end date`() {
        val period = DateFilterPeriod.Year(2025)
        val (_, end) = period.getDateRange()

        assertNotNull(end)
        val calendar = Calendar.getInstance()
        calendar.time = end!!

        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH))
        assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendar.get(Calendar.MINUTE))
        assertEquals(59, calendar.get(Calendar.SECOND))
        assertEquals(999, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `Year getDisplayText formats as Year`() {
        val period = DateFilterPeriod.Year(2025)

        assertEquals("2025", period.getDisplayText())
    }

    @Test
    fun `Month handles January correctly`() {
        val period = DateFilterPeriod.Month(2025, 1)
        val (start, end) = period.getDateRange()

        val startCal = Calendar.getInstance().apply { time = start!! }
        val endCal = Calendar.getInstance().apply { time = end!! }

        assertEquals(Calendar.JANUARY, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.JANUARY, endCal.get(Calendar.MONTH))
        assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `Month handles February non-leap year correctly`() {
        val period = DateFilterPeriod.Month(2025, 2) // 2025 is not a leap year
        val (start, end) = period.getDateRange()

        val startCal = Calendar.getInstance().apply { time = start!! }
        val endCal = Calendar.getInstance().apply { time = end!! }

        assertEquals(Calendar.FEBRUARY, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FEBRUARY, endCal.get(Calendar.MONTH))
        assertEquals(28, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `Month handles February leap year correctly`() {
        val period = DateFilterPeriod.Month(2024, 2) // 2024 is a leap year
        val (start, end) = period.getDateRange()

        val startCal = Calendar.getInstance().apply { time = start!! }
        val endCal = Calendar.getInstance().apply { time = end!! }

        assertEquals(Calendar.FEBRUARY, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FEBRUARY, endCal.get(Calendar.MONTH))
        assertEquals(29, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `Month handles April (30 days) correctly`() {
        val period = DateFilterPeriod.Month(2025, 4)
        val (_, end) = period.getDateRange()

        val endCal = Calendar.getInstance().apply { time = end!! }

        assertEquals(Calendar.APRIL, endCal.get(Calendar.MONTH))
        assertEquals(30, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `Month handles December correctly`() {
        val period = DateFilterPeriod.Month(2025, 12)
        val (start, end) = period.getDateRange()

        val startCal = Calendar.getInstance().apply { time = start!! }
        val endCal = Calendar.getInstance().apply { time = end!! }

        assertEquals(Calendar.DECEMBER, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.DECEMBER, endCal.get(Calendar.MONTH))
        assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `AllTime equals itself`() {
        val period1 = DateFilterPeriod.AllTime
        val period2 = DateFilterPeriod.AllTime

        assertEquals(period1, period2)
    }

    @Test
    fun `Month equals same month and year`() {
        val period1 = DateFilterPeriod.Month(2025, 10)
        val period2 = DateFilterPeriod.Month(2025, 10)

        assertEquals(period1, period2)
    }

    @Test
    fun `Month does not equal different month`() {
        val period1 = DateFilterPeriod.Month(2025, 10)
        val period2 = DateFilterPeriod.Month(2025, 11)

        assertNotEquals(period1, period2)
    }

    @Test
    fun `Month does not equal different year`() {
        val period1 = DateFilterPeriod.Month(2025, 10)
        val period2 = DateFilterPeriod.Month(2024, 10)

        assertNotEquals(period1, period2)
    }

    @Test
    fun `Year equals same year`() {
        val period1 = DateFilterPeriod.Year(2025)
        val period2 = DateFilterPeriod.Year(2025)

        assertEquals(period1, period2)
    }

    @Test
    fun `Year does not equal different year`() {
        val period1 = DateFilterPeriod.Year(2025)
        val period2 = DateFilterPeriod.Year(2024)

        assertNotEquals(period1, period2)
    }

    @Test
    fun `AllTime does not equal Month`() {
        val period1 = DateFilterPeriod.AllTime
        val period2 = DateFilterPeriod.Month(2025, 10)

        assertNotEquals(period1, period2)
    }

    @Test
    fun `Month does not equal Year`() {
        val period1 = DateFilterPeriod.Month(2025, 10)
        val period2 = DateFilterPeriod.Year(2025)

        assertNotEquals(period1, period2)
    }
}

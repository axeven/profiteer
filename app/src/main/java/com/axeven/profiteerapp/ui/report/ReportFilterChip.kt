package com.axeven.profiteerapp.ui.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axeven.profiteerapp.data.model.DateFilterPeriod

/**
 * FilterChip component for displaying and selecting the current date filter period.
 *
 * This chip shows the currently selected period (e.g., "All Time", "October 2025", "2025")
 * and opens the MonthYearPickerDialog when clicked.
 *
 * @param currentPeriod The currently selected filter period
 * @param onClick Callback when chip is clicked
 * @param modifier Optional modifier for the chip
 */
@Composable
fun ReportFilterChip(
    currentPeriod: DateFilterPeriod,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = currentPeriod != DateFilterPeriod.AllTime,
        onClick = onClick,
        label = {
            Text(text = currentPeriod.getDisplayText())
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Filter by date",
                modifier = Modifier
            )
        },
        modifier = modifier
    )
}

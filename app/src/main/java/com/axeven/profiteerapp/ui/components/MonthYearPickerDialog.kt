package com.axeven.profiteerapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.axeven.profiteerapp.R
import com.axeven.profiteerapp.data.model.DateFilterPeriod
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog for selecting a date filter period (All Time, Month, or Year).
 *
 * This dialog allows users to filter report data by:
 * - All Time (no filtering)
 * - Specific month and year
 * - Specific year
 *
 * @param currentPeriod The currently selected period
 * @param availableMonths List of available month/year pairs from transaction data
 * @param availableYears List of available years from transaction data
 * @param onPeriodSelected Callback when user confirms a new period selection
 * @param onDismiss Callback when dialog is dismissed without selection
 */
@Composable
fun MonthYearPickerDialog(
    currentPeriod: DateFilterPeriod,
    availableMonths: List<Pair<Int, Int>>, // (year, month) pairs
    availableYears: List<Int>,
    onPeriodSelected: (DateFilterPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf(currentPeriod) }
    val hasSelectionChanged = selectedPeriod != currentPeriod

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select Period")
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // All Time option
                item {
                    PeriodOption(
                        period = DateFilterPeriod.AllTime,
                        isSelected = selectedPeriod == DateFilterPeriod.AllTime,
                        onClick = { selectedPeriod = DateFilterPeriod.AllTime }
                    )
                }

                // Divider
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // Section: Months
                if (availableMonths.isNotEmpty()) {
                    item {
                        Text(
                            text = "Months",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(availableMonths) { (year, month) ->
                        val monthPeriod = DateFilterPeriod.Month(year, month)
                        PeriodOption(
                            period = monthPeriod,
                            isSelected = selectedPeriod == monthPeriod,
                            onClick = { selectedPeriod = monthPeriod }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // Section: Years
                if (availableYears.isNotEmpty()) {
                    item {
                        Text(
                            text = "Years",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(availableYears) { year ->
                        val yearPeriod = DateFilterPeriod.Year(year)
                        PeriodOption(
                            period = yearPeriod,
                            isSelected = selectedPeriod == yearPeriod,
                            onClick = { selectedPeriod = yearPeriod }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onPeriodSelected(selectedPeriod)
                    onDismiss()
                },
                enabled = hasSelectionChanged
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Individual period option in the picker dialog.
 */
@Composable
private fun PeriodOption(
    period: DateFilterPeriod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = period.getDisplayText(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        if (isSelected) {
            RadioButton(
                selected = true,
                onClick = onClick
            )
        }
    }
}

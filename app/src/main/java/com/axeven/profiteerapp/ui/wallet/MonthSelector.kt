package com.axeven.profiteerapp.ui.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    selectedMonth: Int,
    selectedYear: Int,
    transactionCount: Int,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onMonthSelected: (Int, Int) -> Unit,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, selectedMonth)
        set(Calendar.YEAR, selectedYear)
    }
    val displayText = monthFormatter.format(calendar.time)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            IconButton(
                onClick = onNavigatePrevious,
                enabled = canNavigatePrevious
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous month",
                    tint = if (canNavigatePrevious) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
            }
            
            // Month/Year selector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showDatePicker = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select month",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (transactionCount > 0) {
                    Text(
                        text = "$transactionCount transaction${if (transactionCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Next button
            IconButton(
                onClick = onNavigateNext,
                enabled = canNavigateNext
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next month",
                    tint = if (canNavigateNext) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        MonthYearPickerDialog(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onMonthYearSelected = { month, year ->
                onMonthSelected(month, year)
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }
}

@Composable
fun MonthYearPickerDialog(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempMonth by remember { mutableStateOf(selectedMonth) }
    var tempYear by remember { mutableStateOf(selectedYear) }
    
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (2020..currentYear).toList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Month and Year",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                // Month selection
                Text(
                    text = "Month",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    monthNames.chunked(4).forEach { rowMonths ->
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            rowMonths.forEachIndexed { index, monthName ->
                                val monthIndex = monthNames.indexOf(monthName)
                                FilterChip(
                                    onClick = { tempMonth = monthIndex },
                                    label = { 
                                        Text(
                                            text = monthName.take(3),
                                            fontSize = 12.sp
                                        ) 
                                    },
                                    selected = tempMonth == monthIndex,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Year selection
                Text(
                    text = "Year",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    years.chunked(3).forEach { rowYears ->
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            rowYears.forEach { year ->
                                FilterChip(
                                    onClick = { tempYear = year },
                                    label = { 
                                        Text(
                                            text = year.toString(),
                                            fontSize = 12.sp
                                        ) 
                                    },
                                    selected = tempYear == year,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onMonthYearSelected(tempMonth, tempYear)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
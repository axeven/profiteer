package com.axeven.profiteerapp.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axeven.profiteerapp.viewmodel.MonthlyExpense
import com.axeven.profiteerapp.viewmodel.TagExpenseData
import com.axeven.profiteerapp.viewmodel.IncomeExpenseComparison
import kotlin.math.max

@Composable
fun MonthlyTrendChart(
    monthlyExpenses: List<MonthlyExpense>,
    monthlyIncome: List<MonthlyExpense>,
    defaultCurrency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Monthly Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (monthlyExpenses.isEmpty() && monthlyIncome.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available for the selected period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Chart Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(
                        color = MaterialTheme.colorScheme.error,
                        label = "Expenses",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    LegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = "Income"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Simple Bar Chart Implementation
                SimpleBarChart(
                    monthlyExpenses = monthlyExpenses,
                    monthlyIncome = monthlyIncome,
                    defaultCurrency = defaultCurrency
                )
            }
        }
    }
}

@Composable
fun ExpensesPieChart(
    tagExpenses: List<TagExpenseData>,
    defaultCurrency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Expenses Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (tagExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expense data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Simple horizontal bar chart for categories
                tagExpenses.take(5).forEachIndexed { index, tagData ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tagData.tag,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(80.dp),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .background(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (tagData.percentage / 100).toFloat())
                                    .background(
                                        getChartColor(index),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        
                        Text(
                            text = "%.1f%%".format(tagData.percentage),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(50.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (tagExpenses.size > 5) {
                    Text(
                        text = "... and ${tagExpenses.size - 5} more categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseBarChart(
    comparison: IncomeExpenseComparison,
    defaultCurrency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Income vs Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (comparison.totalIncome == 0.0 && comparison.totalExpenses == 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No income or expense data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxAmount = max(comparison.totalIncome, comparison.totalExpenses)
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Income bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(70.dp),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .background(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            if (maxAmount > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = (comparison.totalIncome / maxAmount).toFloat())
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                        
                        Text(
                            text = "%.0f %s".format(comparison.totalIncome, defaultCurrency),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Expenses bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(70.dp),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .background(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            if (maxAmount > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = (comparison.totalExpenses / maxAmount).toFloat())
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                        
                        Text(
                            text = "%.0f %s".format(comparison.totalExpenses, defaultCurrency),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Net amount indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Net: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "%.2f %s".format(comparison.netAmount, defaultCurrency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (comparison.netAmount >= 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleBarChart(
    monthlyExpenses: List<MonthlyExpense>,
    monthlyIncome: List<MonthlyExpense>,
    defaultCurrency: String
) {
    val maxExpenses = monthlyExpenses.maxOfOrNull { it.totalAmount } ?: 0.0
    val maxIncome = monthlyIncome.maxOfOrNull { it.totalAmount } ?: 0.0
    val maxAmount = max(maxExpenses, maxIncome)
    
    if (maxAmount > 0) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            monthlyExpenses.take(6).forEach { monthData ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height((120 * (monthData.totalAmount / maxAmount)).dp)
                            .background(
                                MaterialTheme.colorScheme.error,
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = monthData.monthDisplay.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Max: %.0f %s".format(maxAmount, defaultCurrency),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun getChartColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF6200EE), // Purple
        Color(0xFF03DAC6), // Teal
        Color(0xFFFF6B00), // Orange
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFE91E63), // Pink
        Color(0xFFFF9800), // Amber
        Color(0xFF9C27B0), // Deep Purple
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFFC107)  // Yellow
    )
    return colors[index % colors.size]
}
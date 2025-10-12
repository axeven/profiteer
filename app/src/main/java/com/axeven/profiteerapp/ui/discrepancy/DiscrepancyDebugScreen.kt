package com.axeven.profiteerapp.ui.discrepancy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.utils.NumberFormatter
import com.axeven.profiteerapp.utils.TransactionWithBalances
import com.axeven.profiteerapp.viewmodel.DiscrepancyDebugViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Discrepancy Debug Screen
 *
 * Displays balance discrepancies between Physical and Logical wallets,
 * showing a chronological list of transactions with running balances
 * to help identify where the discrepancy occurred.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscrepancyDebugScreen(
    viewModel: DiscrepancyDebugViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Balance Discrepancy Debug",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(error = uiState.error!!)
                }
                else -> {
                    DiscrepancyContent(
                        uiState = uiState,
                        transactions = uiState.transactions,
                        defaultCurrency = uiState.defaultCurrency
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Analyzing transactions...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Error Loading Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscrepancyContent(
    uiState: com.axeven.profiteerapp.data.ui.DiscrepancyDebugUiState,
    transactions: List<TransactionWithBalances>,
    defaultCurrency: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary header
        item {
            DiscrepancySummaryCard(
                physicalTotal = uiState.totalPhysicalBalance,
                logicalTotal = uiState.totalLogicalBalance,
                discrepancy = uiState.currentDiscrepancy,
                isBalanced = uiState.isBalanced,
                defaultCurrency = defaultCurrency
            )
        }

        // Info card
        item {
            InfoCard(
                transactionCount = transactions.size,
                firstDiscrepancyId = uiState.firstDiscrepancyId
            )
        }

        // Transaction list (in ascending order - oldest first, starting from first discrepancy)
        items(transactions) { transactionWithBalances ->
            TransactionDiscrepancyCard(
                transactionWithBalances = transactionWithBalances,
                isFirstDiscrepancy = transactionWithBalances.isFirstDiscrepancy,
                defaultCurrency = defaultCurrency
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DiscrepancySummaryCard(
    physicalTotal: Double,
    logicalTotal: Double,
    discrepancy: Double,
    isBalanced: Boolean,
    defaultCurrency: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBalanced)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBalanced) "✓ Balanced" else "⚠ Discrepancy Detected",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isBalanced)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
                if (!isBalanced) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Physical Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(physicalTotal, defaultCurrency, showSymbol = true),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Logical Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(logicalTotal, defaultCurrency, showSymbol = true),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!isBalanced) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Discrepancy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(discrepancy, defaultCurrency, showSymbol = true),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    transactionCount: Int,
    firstDiscrepancyId: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Showing $transactionCount transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (firstDiscrepancyId != null) {
                    Text(
                        text = "First discrepancy highlighted below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "All transactions balanced",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionDiscrepancyCard(
    transactionWithBalances: TransactionWithBalances,
    isFirstDiscrepancy: Boolean,
    defaultCurrency: String
) {
    val transaction = transactionWithBalances.transaction
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFirstDiscrepancy)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFirstDiscrepancy) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row with title and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isFirstDiscrepancy) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isFirstDiscrepancy) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    if (isFirstDiscrepancy) {
                        Text(
                            text = "First Discrepancy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = dateFormat.format(transaction.transactionDate ?: transaction.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (transaction.type) {
                            TransactionType.INCOME -> "+${NumberFormatter.formatCurrency(transaction.amount, defaultCurrency, showSymbol = false)}"
                            TransactionType.EXPENSE -> NumberFormatter.formatCurrency(-transaction.amount, defaultCurrency, showSymbol = false)
                            TransactionType.TRANSFER -> "↔ ${NumberFormatter.formatCurrency(transaction.amount, defaultCurrency, showSymbol = false)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (transaction.type) {
                            TransactionType.INCOME -> Color(0xFF4CAF50)
                            TransactionType.EXPENSE -> Color(0xFFF44336)
                            TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Running balance comparison
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Physical Balance",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(
                            transactionWithBalances.physicalBalanceAfter,
                            defaultCurrency,
                            showSymbol = true
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Logical Balance",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(
                            transactionWithBalances.logicalBalanceAfter,
                            defaultCurrency,
                            showSymbol = true
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

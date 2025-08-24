package com.axeven.profiteerapp.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.axeven.profiteerapp.viewmodel.WalletDetailViewModel
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.ui.home.QuickAction
import com.axeven.profiteerapp.ui.home.TransactionItem
import com.axeven.profiteerapp.utils.NumberFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailScreen(
    walletId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToCreateTransaction: (TransactionType, String) -> Unit = { _, _ -> },
    onEditTransaction: (Transaction) -> Unit = {},
    viewModel: WalletDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load wallet details when walletId changes
    LaunchedEffect(walletId) {
        if (walletId.isNotEmpty()) {
            viewModel.loadWalletDetails(walletId)
        }
    }
    
    val quickActions = listOf(
        QuickAction("Add Income", Icons.Default.Add, Color(0xFF4CAF50)),
        QuickAction("Add Expense", Icons.Default.Delete, Color(0xFFF44336)),
        QuickAction("Transfer", Icons.Default.Refresh, Color(0xFF2196F3))
    )

    // Show error if any
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar here if needed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        uiState.wallet?.name ?: "Wallet Detail", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Currency display toggle removed - using single global currency
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WalletBalanceCard(
                        wallet = uiState.wallet,
                        monthlyIncome = uiState.monthlyIncome,
                        monthlyExpenses = uiState.monthlyExpenses,
                        currency = uiState.displayCurrency,
                        selectedMonth = uiState.selectedMonth,
                        selectedYear = uiState.selectedYear,
                        isRecalculatingBalance = uiState.isRecalculatingBalance,
                        recalculationError = uiState.recalculationError,
                        onRecalculateBalance = { viewModel.recalculateBalance() },
                        onClearRecalculationError = { viewModel.clearRecalculationError() }
                    )
                }
                
                // Conversion warnings not needed with single currency
                
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(quickActions) { action ->
                            QuickActionCard(
                                action = action,
                                onClick = {
                                    when (action.title) {
                                        "Add Income" -> onNavigateToCreateTransaction(TransactionType.INCOME, walletId)
                                        "Add Expense" -> onNavigateToCreateTransaction(TransactionType.EXPENSE, walletId)
                                        "Transfer" -> onNavigateToCreateTransaction(TransactionType.TRANSFER, walletId)
                                    }
                                }
                            )
                        }
                    }
                }
                
                item {
                    MonthSelector(
                        selectedMonth = uiState.selectedMonth,
                        selectedYear = uiState.selectedYear,
                        transactionCount = uiState.transactionCount,
                        canNavigatePrevious = viewModel.canNavigateToPrevious(),
                        canNavigateNext = viewModel.canNavigateToNext(),
                        onMonthSelected = { month, year ->
                            viewModel.setSelectedMonth(month, year)
                        },
                        onNavigatePrevious = {
                            viewModel.navigateToPreviousMonth()
                        },
                        onNavigateNext = {
                            viewModel.navigateToNextMonth()
                        }
                    )
                }
                
                item {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                if (uiState.filteredTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No transactions",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (uiState.transactions.isEmpty()) "No transactions yet" else "No transactions for selected month",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (uiState.transactions.isEmpty()) 
                                        "Use the quick actions above to add your first transaction" 
                                    else 
                                        "Try selecting a different month or add new transactions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    uiState.groupedTransactions.forEach { (dateKey, transactionsForDate) ->
                        if (transactionsForDate.isNotEmpty()) {
                            item {
                                DateGroupHeader(
                                    dateKey = dateKey,
                                    dateDisplayString = viewModel.getDateDisplayString(dateKey),
                                    transactions = transactionsForDate,
                                    walletId = walletId,
                                    defaultCurrency = uiState.defaultCurrency,
                                    isExpanded = uiState.expandedDates.contains(dateKey),
                                    onToggleExpanded = { 
                                        viewModel.toggleDateExpansion(dateKey)
                                    },
                                    calculateDailySummary = viewModel::calculateDailySummary
                                )
                            }
                            
                            if (uiState.expandedDates.contains(dateKey)) {
                                items(transactionsForDate) { transaction ->
                                    Box(modifier = Modifier.padding(start = 16.dp)) {
                                        TransactionItem(
                                            transaction = transaction,
                                            wallets = uiState.allWallets,
                                            currentWalletId = walletId,
                                            defaultCurrency = uiState.defaultCurrency,
                                            onClick = { onEditTransaction(transaction) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletBalanceCard(
    wallet: Wallet?, 
    monthlyIncome: Double, 
    monthlyExpenses: Double, 
    currency: String = "USD",
    selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    isRecalculatingBalance: Boolean = false,
    recalculationError: String? = null,
    onRecalculateBalance: () -> Unit = {},
    onClearRecalculationError: () -> Unit = {}
) {
    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, selectedMonth)
        set(Calendar.YEAR, selectedYear)
    }
    val selectedMonthName = monthFormatter.format(calendar.time)
    
    val currentCalendar = Calendar.getInstance()
    val isCurrentMonth = selectedMonth == currentCalendar.get(Calendar.MONTH) && 
                        selectedYear == currentCalendar.get(Calendar.YEAR)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = wallet?.name ?: "Unknown Wallet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    if (wallet?.walletType == "Logical") {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Logical wallet",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = NumberFormatter.formatCurrency(wallet?.balance ?: 0.0, currency, showSymbol = true),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Recalculate balance button
                    IconButton(
                        onClick = onRecalculateBalance,
                        enabled = !isRecalculatingBalance,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isRecalculatingBalance) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recalculate balance",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Show recalculation error if any
                recalculationError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFF44336)
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336)
                        )
                        IconButton(
                            onClick = onClearRecalculationError,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear error",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isCurrentMonth) "This Month Income" else "$selectedMonthName Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(monthlyIncome, currency, showSymbol = true),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column {
                    Text(
                        text = if (isCurrentMonth) "This Month Expenses" else "$selectedMonthName Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(monthlyExpenses, currency, showSymbol = true),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(action: QuickAction, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .size(120.dp, 100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(action.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = action.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
package com.axeven.profiteerapp.ui.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.axeven.profiteerapp.viewmodel.HomeViewModel
import com.axeven.profiteerapp.viewmodel.SharedErrorViewModel
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.utils.NumberFormatter
import com.axeven.profiteerapp.ui.components.ErrorMessage
import java.text.SimpleDateFormat
import java.util.*


data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToWalletList: () -> Unit = {},
    onNavigateToCreateTransaction: (TransactionType) -> Unit = {},
    onEditTransaction: (Transaction) -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToTransactionList: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    refreshTrigger: Int = 0, // Add refresh trigger parameter
    viewModel: HomeViewModel = hiltViewModel(),
    sharedErrorViewModel: SharedErrorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorState by sharedErrorViewModel.errorState.collectAsState()

    // Refresh data when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.refreshData()
        }
    }

    val quickActions = listOf(
        QuickAction("Add Income", Icons.Default.Add, Color(0xFF4CAF50)),
        QuickAction("Add Expense", Icons.Default.Delete, Color(0xFFF44336)),
        QuickAction("Transfer", Icons.Default.Refresh, Color(0xFF2196F3)),
        QuickAction("Reports", Icons.Default.DateRange, Color(0xFF00BCD4)),
        QuickAction("Transaction List", Icons.Default.List, Color(0xFF9C27B0))
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
                        "Profiteer", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { /* TODO: Profile */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Show error message if there's an error
            errorState.message?.let { errorMessage ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ErrorMessage(
                            message = errorMessage,
                            shouldRetry = errorState.shouldRetry,
                            requiresReauth = errorState.requiresReauth,
                            isOffline = errorState.isOffline,
                            onRetry = { viewModel.refreshData() },
                            onSignIn = { onNavigateToAuth() },
                            onDismiss = { sharedErrorViewModel.clearError() }
                        )
                    }
                }
            }
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    BalanceCard(
                        balance = uiState.totalBalance, 
                        income = uiState.totalIncome, 
                        expenses = uiState.totalExpenses,
                        currency = uiState.defaultCurrency,
                        onBalanceClick = onNavigateToWalletList
                    )
                }
            }
            
            
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(quickActions) { action ->
                        QuickActionCard(
                            action = action,
                            onClick = {
                                when (action.title) {
                                    "Add Income" -> onNavigateToCreateTransaction(TransactionType.INCOME)
                                    "Add Expense" -> onNavigateToCreateTransaction(TransactionType.EXPENSE)
                                    "Transfer" -> onNavigateToCreateTransaction(TransactionType.TRANSFER)
                                    "Reports" -> onNavigateToReports()
                                    "Transaction List" -> onNavigateToTransactionList()
                                    // Other actions can be implemented later
                                }
                            }
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            items(uiState.transactions) { transaction ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TransactionItem(
                        transaction = transaction,
                        wallets = uiState.wallets,
                        defaultCurrency = uiState.defaultCurrency,
                        onClick = { onEditTransaction(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double, 
    income: Double, 
    expenses: Double, 
    currency: String = "USD",
    onBalanceClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onBalanceClick() },
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
                        text = "Wallets",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "View wallets",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = NumberFormatter.formatCurrency(balance, currency, showSymbol = true),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(income, currency, showSymbol = true),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(expenses, currency, showSymbol = true),
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
            .size(70.dp, 75.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(action.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = action.color,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                maxLines = 2,
                lineHeight = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction, 
    wallets: List<Wallet> = emptyList(),
    currentWalletId: String? = null,
    defaultCurrency: String = "USD",
    onClick: () -> Unit = {}
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val displayDate = transaction.transactionDate?.let { dateFormatter.format(it) } ?: "Unknown"
    
    // Determine effective transaction type and styling based on transfer direction
    val (effectiveType, isTransferBased, counterpartWallet) = when {
        transaction.type == TransactionType.TRANSFER && currentWalletId != null -> {
            val counterpart = when {
                transaction.sourceWalletId == currentWalletId -> wallets.find { it.id == transaction.destinationWalletId }
                transaction.destinationWalletId == currentWalletId -> wallets.find { it.id == transaction.sourceWalletId }
                else -> null
            }
            val effectiveType = when {
                transaction.sourceWalletId == currentWalletId -> TransactionType.EXPENSE
                transaction.destinationWalletId == currentWalletId -> TransactionType.INCOME
                else -> TransactionType.TRANSFER
            }
            Triple(effectiveType, true, counterpart)
        }
        else -> Triple(transaction.type, false, null)
    }
    
    // All transactions now use the default currency
    val currency = defaultCurrency
    
    // Calculate display amount for transfers
    val displayAmount = when {
        transaction.type == TransactionType.TRANSFER && currentWalletId != null -> {
            when {
                transaction.sourceWalletId == currentWalletId -> -kotlin.math.abs(transaction.amount)
                transaction.destinationWalletId == currentWalletId -> kotlin.math.abs(transaction.amount)
                else -> transaction.amount
            }
        }
        else -> transaction.amount
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            when (effectiveType) {
                                TransactionType.INCOME -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                TransactionType.EXPENSE -> Color(0xFFF44336).copy(alpha = 0.1f)
                                TransactionType.TRANSFER -> Color(0xFF2196F3).copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Main transaction icon
                    Icon(
                        imageVector = when (effectiveType) {
                            TransactionType.INCOME -> Icons.Default.KeyboardArrowUp
                            TransactionType.EXPENSE -> Icons.Default.KeyboardArrowDown
                            TransactionType.TRANSFER -> Icons.Default.Refresh
                        },
                        contentDescription = null,
                        tint = when (effectiveType) {
                            TransactionType.INCOME -> Color(0xFF4CAF50)
                            TransactionType.EXPENSE -> Color(0xFFF44336)
                            TransactionType.TRANSFER -> Color(0xFF2196F3)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    
                    // Transfer indicator badge
                    if (isTransferBased) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2196F3))
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Transfer",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    val displayTitle = if (isTransferBased && counterpartWallet != null) {
                        when (effectiveType) {
                            TransactionType.INCOME -> "Transfer from ${counterpartWallet.name}"
                            TransactionType.EXPENSE -> "Transfer to ${counterpartWallet.name}"
                            else -> transaction.title
                        }
                    } else {
                        transaction.title
                    }
                    
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    val subtitleText = buildString {
                        if (transaction.tags.isNotEmpty()) {
                            append(transaction.tags.joinToString(", "))
                        } else {
                            append("Untagged")
                        }
                        append(" • ")
                        append(displayDate)
                        if (isTransferBased) {
                            append(" • Transfer")
                        }
                    }
                    
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Text(
                text = "${if (displayAmount > 0) "+" else ""}${NumberFormatter.formatCompactCurrency(kotlin.math.abs(displayAmount), currency)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = when (effectiveType) {
                    TransactionType.INCOME -> Color(0xFF4CAF50)
                    TransactionType.EXPENSE -> Color(0xFFF44336)
                    TransactionType.TRANSFER -> Color(0xFF2196F3)
                }
            )
        }
    }
}
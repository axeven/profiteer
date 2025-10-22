package com.axeven.profiteerapp.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.models.Pie
import ir.ehsannarmani.compose_charts.PieChart
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.viewmodel.ReportViewModel
import com.axeven.profiteerapp.viewmodel.ChartDataType
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.utils.NumberFormatter
import com.axeven.profiteerapp.utils.TagFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreenSimple(
    onNavigateBack: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load portfolio data when screen loads
    LaunchedEffect(Unit) {
        viewModel.loadPortfolioData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Portfolio Report", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    SimplePortfolioAssetCard(
                        selectedDataType = uiState.selectedChartDataType,
                        portfolioData = uiState.portfolioComposition,
                        walletData = uiState.physicalWalletBalances,
                        logicalWalletData = uiState.logicalWalletBalances,
                        expenseTransactionsByTagData = uiState.expenseTransactionsByTag,
                        incomeTransactionsByTagData = uiState.incomeTransactionsByTag,
                        totalPortfolioBalance = uiState.totalPortfolioValue,
                        totalWalletBalance = uiState.totalPhysicalWalletValue,
                        totalLogicalWalletBalance = uiState.totalLogicalWalletValue,
                        totalExpensesByTag = uiState.totalExpensesByTag,
                        totalIncomeByTag = uiState.totalIncomeByTag,
                        defaultCurrency = uiState.defaultCurrency
                    )
                }
                
                // Pie Chart
                item {
                    val hasPortfolioData = uiState.portfolioComposition.isNotEmpty() && uiState.totalPortfolioValue > 0
                    val hasPhysicalWalletData = uiState.physicalWalletBalances.isNotEmpty() && uiState.totalPhysicalWalletValue > 0
                    val hasLogicalWalletData = uiState.logicalWalletBalances.isNotEmpty()
                    val hasExpenseTransactionsByTagData = uiState.expenseTransactionsByTag.isNotEmpty() && uiState.totalExpensesByTag > 0
                    val hasIncomeTransactionsByTagData = uiState.incomeTransactionsByTag.isNotEmpty() && uiState.totalIncomeByTag > 0
                    val hasAnyData = hasPortfolioData || hasPhysicalWalletData || hasLogicalWalletData || hasExpenseTransactionsByTagData || hasIncomeTransactionsByTagData
                    
                    if (hasAnyData) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                
                                when (uiState.selectedChartDataType) {
                                    ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> {
                                        if (hasPortfolioData) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(250.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ComposeChartsPieChartAsset(
                                                    portfolioData = uiState.portfolioComposition,
                                                    modifier = Modifier.size(200.dp)
                                                )
                                            }
                                        }
                                    }
                                    ChartDataType.PHYSICAL_WALLET_BALANCE -> {
                                        if (hasPhysicalWalletData) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(250.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ComposeChartsPieChartWallet(
                                                    walletData = uiState.physicalWalletBalances,
                                                    modifier = Modifier.size(200.dp)
                                                )
                                            }
                                        }
                                    }
                                    ChartDataType.LOGICAL_WALLET_BALANCE -> {
                                        if (hasLogicalWalletData) {
                                            ComposeChartsLogicalWalletChart(
                                                logicalWalletData = uiState.logicalWalletBalances,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> {
                                        if (hasExpenseTransactionsByTagData) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(250.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ComposeChartsPieChartExpenseTransactionsByTag(
                                                    expenseTransactionsByTagData = uiState.expenseTransactionsByTag,
                                                    modifier = Modifier.size(200.dp)
                                                )
                                            }
                                        }
                                    }
                                    ChartDataType.INCOME_TRANSACTION_BY_TAG -> {
                                        if (hasIncomeTransactionsByTagData) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(250.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ComposeChartsPieChartIncomeTransactionsByTag(
                                                    incomeTransactionsByTagData = uiState.incomeTransactionsByTag,
                                                    modifier = Modifier.size(200.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Chart Options Section
                item {
                    ChartOptionsSection(
                        selectedDataType = uiState.selectedChartDataType,
                        onDataTypeChange = viewModel::selectChartDataType,
                        hasPortfolioData = uiState.portfolioComposition.isNotEmpty() && uiState.totalPortfolioValue > 0,
                        hasPhysicalWalletData = uiState.physicalWalletBalances.isNotEmpty() && uiState.totalPhysicalWalletValue > 0,
                        hasLogicalWalletData = uiState.logicalWalletBalances.isNotEmpty(),
                        hasExpenseTransactionsByTagData = uiState.expenseTransactionsByTag.isNotEmpty() && uiState.totalExpensesByTag > 0,
                        hasIncomeTransactionsByTagData = uiState.incomeTransactionsByTag.isNotEmpty() && uiState.totalIncomeByTag > 0
                    )
                }
                
                // Chart Breakdown Section (moved to bottom)
                item {
                    val hasPortfolioData = uiState.portfolioComposition.isNotEmpty() && uiState.totalPortfolioValue > 0
                    val hasPhysicalWalletData = uiState.physicalWalletBalances.isNotEmpty() && uiState.totalPhysicalWalletValue > 0
                    val hasLogicalWalletData = uiState.logicalWalletBalances.isNotEmpty()
                    val hasExpenseTransactionsByTagData = uiState.expenseTransactionsByTag.isNotEmpty() && uiState.totalExpensesByTag > 0
                    val hasIncomeTransactionsByTagData = uiState.incomeTransactionsByTag.isNotEmpty() && uiState.totalIncomeByTag > 0
                    val hasData = when (uiState.selectedChartDataType) {
                        ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> hasPortfolioData
                        ChartDataType.PHYSICAL_WALLET_BALANCE -> hasPhysicalWalletData
                        ChartDataType.LOGICAL_WALLET_BALANCE -> hasLogicalWalletData
                        ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> hasExpenseTransactionsByTagData
                        ChartDataType.INCOME_TRANSACTION_BY_TAG -> hasIncomeTransactionsByTagData
                    }
                    
                    if (hasData) {
                        ChartBreakdownSection(
                            selectedDataType = uiState.selectedChartDataType,
                            portfolioData = uiState.portfolioComposition,
                            walletData = uiState.physicalWalletBalances,
                            logicalWalletData = uiState.logicalWalletBalances,
                            expenseTransactionsByTagData = uiState.expenseTransactionsByTag,
                            incomeTransactionsByTagData = uiState.incomeTransactionsByTag,
                            totalPortfolioBalance = uiState.totalPortfolioValue,
                            totalWalletBalance = uiState.totalPhysicalWalletValue,
                            totalLogicalWalletBalance = uiState.totalLogicalWalletValue,
                            totalExpensesByTag = uiState.totalExpensesByTag,
                            totalIncomeByTag = uiState.totalIncomeByTag,
                            defaultCurrency = uiState.defaultCurrency
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimplePortfolioAssetCard(
    selectedDataType: ChartDataType,
    portfolioData: Map<PhysicalForm, Double>,
    walletData: Map<String, Double>,
    logicalWalletData: Map<String, Double>,
    expenseTransactionsByTagData: Map<String, Double>,
    incomeTransactionsByTagData: Map<String, Double>,
    totalPortfolioBalance: Double,
    totalWalletBalance: Double,
    totalLogicalWalletBalance: Double,
    totalExpensesByTag: Double,
    totalIncomeByTag: Double,
    defaultCurrency: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val totalValue = when (selectedDataType) {
                ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> totalPortfolioBalance
                ChartDataType.PHYSICAL_WALLET_BALANCE -> totalWalletBalance
                ChartDataType.LOGICAL_WALLET_BALANCE -> totalLogicalWalletBalance
                ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> totalExpensesByTag
                ChartDataType.INCOME_TRANSACTION_BY_TAG -> totalIncomeByTag
            }
            
            val hasData = when (selectedDataType) {
                ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> portfolioData.isNotEmpty() && totalPortfolioBalance > 0
                ChartDataType.PHYSICAL_WALLET_BALANCE -> walletData.isNotEmpty() && totalWalletBalance > 0
                ChartDataType.LOGICAL_WALLET_BALANCE -> logicalWalletData.isNotEmpty()
                ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> expenseTransactionsByTagData.isNotEmpty() && totalExpensesByTag > 0
                ChartDataType.INCOME_TRANSACTION_BY_TAG -> incomeTransactionsByTagData.isNotEmpty() && totalIncomeByTag > 0
            }
            
            Text(
                text = when (selectedDataType) {
                    ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> 
                        "Total Portfolio Value: ${NumberFormatter.formatCurrency(totalValue, defaultCurrency, showSymbol = true)}"
                    ChartDataType.PHYSICAL_WALLET_BALANCE -> 
                        "Total Physical Wallet Value: ${NumberFormatter.formatCurrency(totalValue, defaultCurrency, showSymbol = true)}"
                    ChartDataType.LOGICAL_WALLET_BALANCE -> 
                        "Total Logical Wallet Value: ${NumberFormatter.formatCurrency(totalValue, defaultCurrency, showSymbol = true)}"
                    ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> 
                        "Total Expense Amount by Tag: ${NumberFormatter.formatCurrency(totalValue, defaultCurrency, showSymbol = true)}"
                    ChartDataType.INCOME_TRANSACTION_BY_TAG -> 
                        "Total Income Amount by Tag: ${NumberFormatter.formatCurrency(totalValue, defaultCurrency, showSymbol = true)}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!hasData) {
                // Show empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (selectedDataType) {
                                ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> "No portfolio data available"
                                ChartDataType.PHYSICAL_WALLET_BALANCE -> "No physical wallet data available"
                                ChartDataType.LOGICAL_WALLET_BALANCE -> "No logical wallet data available"
                                ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> "No expense transaction data available"
                                ChartDataType.INCOME_TRANSACTION_BY_TAG -> "No income transaction data available"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (selectedDataType) {
                                ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> "Add some wallets to see your asset composition"
                                ChartDataType.PHYSICAL_WALLET_BALANCE -> "Add some physical wallets to see their balance composition"
                                ChartDataType.LOGICAL_WALLET_BALANCE -> "Add some logical wallets to see their balance composition"
                                ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> "Add some expense transactions with tags to see spending breakdown by tag"
                                ChartDataType.INCOME_TRANSACTION_BY_TAG -> "Add some income transactions with tags to see income breakdown by tag"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimplePortfolioLegend(
    portfolioData: Map<PhysicalForm, Double>,
    totalBalance: Double,
    defaultCurrency: String
) {
    Column {
        
        portfolioData.entries.sortedByDescending { it.value }.forEachIndexed { index, entry ->
            val percentage = (entry.value / totalBalance * 100)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = getPhysicalFormColorSimple(entry.key, index),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = entry.key.displayNameSimple,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = NumberFormatter.formatCurrency(entry.value, defaultCurrency, showSymbol = true),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleWalletLegend(
    walletData: Map<String, Double>,
    totalBalance: Double,
    defaultCurrency: String
) {
    Column {
        
        walletData.entries.sortedByDescending { it.value }.forEachIndexed { index, entry ->
            val percentage = (entry.value / totalBalance * 100)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = getWalletColorSimple(index),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = entry.key,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = NumberFormatter.formatCurrency(entry.value, defaultCurrency, showSymbol = true),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLogicalWalletLegend(
    logicalWalletData: Map<String, Double>,
    totalBalance: Double,
    defaultCurrency: String
) {
    // Separate positive and negative balances
    val positiveData = logicalWalletData.filter { it.value > 0 }
    val negativeData = logicalWalletData.filter { it.value < 0 }
    
    Column {
        // Show positive balances
        if (positiveData.isNotEmpty()) {
            Text(
                text = "Positive Balances",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val positiveTotal = positiveData.values.sum()
            positiveData.entries.sortedByDescending { it.value }.forEachIndexed { index, entry ->
                val percentage = (entry.value / positiveTotal * 100)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = getWalletColorSimple(index),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = NumberFormatter.formatCurrency(entry.value, defaultCurrency, showSymbol = true),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50) // Green for positive
                        )
                        Text(
                            text = "${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Show negative balances
        if (negativeData.isNotEmpty()) {
            if (positiveData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Text(
                text = "Negative Balances",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val negativeTotal = negativeData.values.map { kotlin.math.abs(it) }.sum()
            negativeData.entries.sortedBy { it.value }.forEachIndexed { index, entry ->
                val absoluteValue = kotlin.math.abs(entry.value)
                val percentage = (absoluteValue / negativeTotal * 100)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = getNegativeWalletColorSimple(index),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = NumberFormatter.formatCurrency(entry.value, defaultCurrency, showSymbol = true),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error // Red for negative
                        )
                        Text(
                            text = "${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Show total balance
        if (logicalWalletData.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = NumberFormatter.formatCurrency(totalBalance, defaultCurrency, showSymbol = true),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (totalBalance >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Helper function to get consistent colors for physical forms
@Composable
fun getPhysicalFormColorSimple(physicalForm: PhysicalForm, index: Int): Color {
    return when (physicalForm) {
        PhysicalForm.FIAT_CURRENCY -> Color(0xFF4CAF50) // Green
        PhysicalForm.CRYPTOCURRENCY -> Color(0xFFFF9800) // Orange  
        PhysicalForm.STOCKS -> Color(0xFF2196F3) // Blue
        PhysicalForm.BONDS -> Color(0xFF9C27B0) // Purple
        PhysicalForm.COMMODITIES -> Color(0xFFF44336) // Red
        PhysicalForm.REAL_ESTATE -> Color(0xFF607D8B) // Blue Grey
        PhysicalForm.MUTUAL_FUNDS -> Color(0xFF795548) // Brown
        PhysicalForm.ETFS -> Color(0xFF009688) // Teal
        else -> {
            // Fallback colors for any additional forms
            val colors = listOf(
                Color(0xFFE91E63), // Pink
                Color(0xFF3F51B5), // Indigo
                Color(0xFFCDDC39), // Lime
                Color(0xFFFF5722)  // Deep Orange
            )
            colors[index % colors.size]
        }
    }
}

// Helper function to get consistent colors for wallets
@Composable
fun getWalletColorSimple(index: Int): Color {
    val colors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFF2196F3), // Blue
        Color(0xFF9C27B0), // Purple
        Color(0xFFF44336), // Red
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF795548), // Brown
        Color(0xFF009688), // Teal
        Color(0xFFE91E63), // Pink
        Color(0xFF3F51B5), // Indigo
        Color(0xFFCDDC39), // Lime
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF00BCD4), // Cyan
        Color(0xFF8BC34A), // Light Green
        Color(0xFFFF5722) // Deep Orange
    )
    return colors[index % colors.size]
}

// Helper function to get consistent colors for negative wallet balances (using red shades)
@Composable
fun getNegativeWalletColorSimple(index: Int): Color {
    val colors = listOf(
        Color(0xFFE57373), // Light Red
        Color(0xFFF48FB1), // Light Pink
        Color(0xFFFFB74D), // Light Orange
        Color(0xFFAED581), // Light Green (muted)
        Color(0xFF81C784), // Medium Green (muted)
        Color(0xFF64B5F6), // Light Blue (muted)
        Color(0xFFBA68C8), // Light Purple (muted)
        Color(0xFF4DB6AC), // Light Teal (muted)
        Color(0xFF90A4AE), // Blue Grey (muted)
        Color(0xFFA1887F), // Light Brown (muted)
        Color(0xFFDCE775), // Light Lime (muted)
        Color(0xFFFFAB91), // Light Deep Orange (muted)
        Color(0xFF4FC3F7), // Light Cyan (muted)
        Color(0xFFC5E1A5), // Very Light Green (muted)
        Color(0xFFFFCC80) // Light Amber (muted)
    )
    return colors[index % colors.size]
}

// Extension property for PhysicalForm display names
val PhysicalForm.displayNameSimple: String
    get() = when (this) {
        PhysicalForm.FIAT_CURRENCY -> "Fiat Currency"
        PhysicalForm.CRYPTOCURRENCY -> "Cryptocurrency" 
        PhysicalForm.STOCKS -> "Stocks"
        PhysicalForm.BONDS -> "Bonds"
        PhysicalForm.COMMODITIES -> "Commodities"
        PhysicalForm.REAL_ESTATE -> "Real Estate"
        PhysicalForm.MUTUAL_FUNDS -> "Mutual Funds"
        PhysicalForm.ETFS -> "ETFs"
        else -> this.displayName
    }

@Composable
fun ChartBreakdownSection(
    selectedDataType: ChartDataType,
    portfolioData: Map<PhysicalForm, Double>,
    walletData: Map<String, Double>,
    logicalWalletData: Map<String, Double>,
    expenseTransactionsByTagData: Map<String, Double>,
    incomeTransactionsByTagData: Map<String, Double>,
    totalPortfolioBalance: Double,
    totalWalletBalance: Double,
    totalLogicalWalletBalance: Double,
    totalExpensesByTag: Double,
    totalIncomeByTag: Double,
    defaultCurrency: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            
            when (selectedDataType) {
                ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> {
                    SimplePortfolioLegend(
                        portfolioData = portfolioData,
                        totalBalance = totalPortfolioBalance,
                        defaultCurrency = defaultCurrency
                    )
                }
                ChartDataType.PHYSICAL_WALLET_BALANCE -> {
                    SimpleWalletLegend(
                        walletData = walletData,
                        totalBalance = totalWalletBalance,
                        defaultCurrency = defaultCurrency
                    )
                }
                ChartDataType.LOGICAL_WALLET_BALANCE -> {
                    SimpleLogicalWalletLegend(
                        logicalWalletData = logicalWalletData,
                        totalBalance = totalLogicalWalletBalance,
                        defaultCurrency = defaultCurrency
                    )
                }
                ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> {
                    SimpleExpenseTransactionsByTagLegend(
                        expenseTransactionsByTagData = expenseTransactionsByTagData,
                        totalBalance = totalExpensesByTag,
                        defaultCurrency = defaultCurrency
                    )
                }
                ChartDataType.INCOME_TRANSACTION_BY_TAG -> {
                    SimpleIncomeTransactionsByTagLegend(
                        incomeTransactionsByTagData = incomeTransactionsByTagData,
                        totalBalance = totalIncomeByTag,
                        defaultCurrency = defaultCurrency
                    )
                }
            }
        }
    }
}

@Composable
fun ChartOptionsSection(
    selectedDataType: ChartDataType,
    onDataTypeChange: (ChartDataType) -> Unit,
    hasPortfolioData: Boolean,
    hasPhysicalWalletData: Boolean,
    hasLogicalWalletData: Boolean,
    hasExpenseTransactionsByTagData: Boolean,
    hasIncomeTransactionsByTagData: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            
            Text(
                text = "Data to Display",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedDataType) {
                                ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> "Portfolio Asset Composition"
                                ChartDataType.PHYSICAL_WALLET_BALANCE -> "Physical Wallet Balance Composition"
                                ChartDataType.LOGICAL_WALLET_BALANCE -> "Logical Wallet Balance Composition"
                                ChartDataType.EXPENSE_TRANSACTION_BY_TAG -> "Expense Transaction by Tag"
                                ChartDataType.INCOME_TRANSACTION_BY_TAG -> "Income Transaction by Tag"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Portfolio Asset Composition",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (hasPortfolioData) MaterialTheme.colorScheme.onSurface 
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Shows breakdown by asset types (stocks, crypto, etc.)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = if (hasPortfolioData) 1f else 0.6f
                                    )
                                )
                            }
                        },
                        onClick = {
                            if (hasPortfolioData) {
                                onDataTypeChange(ChartDataType.PORTFOLIO_ASSET_COMPOSITION)
                                expanded = false
                            }
                        },
                        enabled = hasPortfolioData
                    )
                    
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Physical Wallet Balance Composition",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (hasPhysicalWalletData) MaterialTheme.colorScheme.onSurface 
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Shows breakdown by individual physical wallet balances",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = if (hasPhysicalWalletData) 1f else 0.6f
                                    )
                                )
                            }
                        },
                        onClick = {
                            if (hasPhysicalWalletData) {
                                onDataTypeChange(ChartDataType.PHYSICAL_WALLET_BALANCE)
                                expanded = false
                            }
                        },
                        enabled = hasPhysicalWalletData
                    )
                    
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Logical Wallet Balance Composition",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (hasLogicalWalletData) MaterialTheme.colorScheme.onSurface 
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Shows breakdown by individual logical wallet balances",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = if (hasLogicalWalletData) 1f else 0.6f
                                    )
                                )
                            }
                        },
                        onClick = {
                            if (hasLogicalWalletData) {
                                onDataTypeChange(ChartDataType.LOGICAL_WALLET_BALANCE)
                                expanded = false
                            }
                        },
                        enabled = hasLogicalWalletData
                    )
                    
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Expense Transaction by Tag",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (hasExpenseTransactionsByTagData) MaterialTheme.colorScheme.onSurface 
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Shows breakdown by expense transaction tags (spending analysis)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = if (hasExpenseTransactionsByTagData) 1f else 0.6f
                                    )
                                )
                            }
                        },
                        onClick = {
                            if (hasExpenseTransactionsByTagData) {
                                onDataTypeChange(ChartDataType.EXPENSE_TRANSACTION_BY_TAG)
                                expanded = false
                            }
                        },
                        enabled = hasExpenseTransactionsByTagData
                    )
                    
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Income Transaction by Tag",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (hasIncomeTransactionsByTagData) MaterialTheme.colorScheme.onSurface 
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Shows breakdown by income transaction tags (income analysis)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = if (hasIncomeTransactionsByTagData) 1f else 0.6f
                                    )
                                )
                            }
                        },
                        onClick = {
                            if (hasIncomeTransactionsByTagData) {
                                onDataTypeChange(ChartDataType.INCOME_TRANSACTION_BY_TAG)
                                expanded = false
                            }
                        },
                        enabled = hasIncomeTransactionsByTagData
                    )
                }
            }
        }
    }
}

@Composable
fun ComposeChartsPieChartAsset(
    portfolioData: Map<PhysicalForm, Double>,
    modifier: Modifier = Modifier
) {
    // Convert portfolio data to ComposeCharts Pie format
    val pieData = portfolioData.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
        Pie(
            label = entry.key.displayNameSimple,
            data = entry.value,
            color = getPhysicalFormColorSimple(entry.key, index)
        )
    }
    
    PieChart(
        modifier = modifier,
        data = pieData,
        onPieClick = { pie ->
            // Optional: Handle pie slice click
            // TODO: Implement proper click handling with Logger
        },
        selectedScale = 1.2f,
        scaleAnimEnterSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        colorAnimEnterSpec = androidx.compose.animation.core.tween(300),
        colorAnimExitSpec = androidx.compose.animation.core.tween(300),
        scaleAnimExitSpec = androidx.compose.animation.core.tween(300),
        spaceDegreeAnimExitSpec = androidx.compose.animation.core.tween(300)
    )
}

@Composable
fun ComposeChartsPieChartWallet(
    walletData: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    // Convert wallet data to ComposeCharts Pie format
    val pieData = walletData.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
        Pie(
            label = entry.key,
            data = entry.value,
            color = getWalletColorSimple(index)
        )
    }
    
    PieChart(
        modifier = modifier,
        data = pieData,
        onPieClick = { pie ->
            // Optional: Handle pie slice click
            // TODO: Implement proper click handling with Logger
        },
        selectedScale = 1.2f,
        scaleAnimEnterSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        colorAnimEnterSpec = androidx.compose.animation.core.tween(300),
        colorAnimExitSpec = androidx.compose.animation.core.tween(300),
        scaleAnimExitSpec = androidx.compose.animation.core.tween(300),
        spaceDegreeAnimExitSpec = androidx.compose.animation.core.tween(300)
    )
}

@Composable
fun ComposeChartsLogicalWalletChart(
    logicalWalletData: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    // Separate positive and negative balances
    val positiveData = logicalWalletData.filter { it.value > 0 }
    val negativeData = logicalWalletData.filter { it.value < 0 }.mapValues { kotlin.math.abs(it.value) }
    
    val hasPositiveData = positiveData.isNotEmpty()
    val hasNegativeData = negativeData.isNotEmpty()
    
    if (hasPositiveData && hasNegativeData) {
        // Show side-by-side pie charts for positive and negative
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Positive balance pie chart
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Positive Balances",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val positiveePieData = positiveData.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
                        Pie(
                            label = entry.key,
                            data = entry.value,
                            color = getWalletColorSimple(index)
                        )
                    }
                    
                    PieChart(
                        modifier = Modifier.size(180.dp),
                        data = positiveePieData,
                        onPieClick = { pie ->
                            // TODO: Implement proper click handling with Logger
                        },
                        selectedScale = 1.2f,
                        scaleAnimEnterSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        ),
                        colorAnimEnterSpec = androidx.compose.animation.core.tween(300),
                        colorAnimExitSpec = androidx.compose.animation.core.tween(300),
                        scaleAnimExitSpec = androidx.compose.animation.core.tween(300),
                        spaceDegreeAnimExitSpec = androidx.compose.animation.core.tween(300)
                    )
                }
                
                // Negative balance pie chart
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Negative Balances",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val negativePieData = negativeData.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
                        Pie(
                            label = entry.key,
                            data = entry.value,
                            color = getNegativeWalletColorSimple(index)
                        )
                    }
                    
                    PieChart(
                        modifier = Modifier.size(180.dp),
                        data = negativePieData,
                        onPieClick = { pie ->
                            // TODO: Implement proper click handling with Logger
                        },
                        selectedScale = 1.2f,
                        scaleAnimEnterSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        ),
                        colorAnimEnterSpec = androidx.compose.animation.core.tween(300),
                        colorAnimExitSpec = androidx.compose.animation.core.tween(300),
                        scaleAnimExitSpec = androidx.compose.animation.core.tween(300),
                        spaceDegreeAnimExitSpec = androidx.compose.animation.core.tween(300)
                    )
                }
            }
        }
    } else if (hasPositiveData) {
        // Only positive data - show single centered pie chart
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Logical Wallet Balances",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val pieData = positiveData.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
                    Pie(
                        label = entry.key,
                        data = entry.value,
                        color = getWalletColorSimple(index)
                    )
                }
                
                PieChart(
                    modifier = Modifier.size(200.dp),
                    data = pieData,
                    onPieClick = { pie ->
                        // TODO: Implement proper click handling with Logger
                    },
                    selectedScale = 1.2f,
                    scaleAnimEnterSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    ),
                    colorAnimEnterSpec = androidx.compose.animation.core.tween(300),
                    colorAnimExitSpec = androidx.compose.animation.core.tween(300),
                    scaleAnimExitSpec = androidx.compose.animation.core.tween(300),
                    spaceDegreeAnimExitSpec = androidx.compose.animation.core.tween(300)
                )
            }
        }
    } else if (hasNegativeData) {
        // Only negative data - show single centered pie chart
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Negative Logical Wallet Balances",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val pieData = negativeData.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
                    Pie(
                        label = entry.key,
                        data = entry.value,
                        color = getNegativeWalletColorSimple(index)
                    )
                }
                
                PieChart(
                    modifier = Modifier.size(200.dp),
                    data = pieData,
                    onPieClick = { pie ->
                        // TODO: Implement proper click handling with Logger
                    },
                    selectedScale = 1.2f,
                    scaleAnimEnterSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    ),
                    colorAnimEnterSpec = androidx.compose.animation.core.tween(300),
                    colorAnimExitSpec = androidx.compose.animation.core.tween(300),
                    scaleAnimExitSpec = androidx.compose.animation.core.tween(300),
                    spaceDegreeAnimExitSpec = androidx.compose.animation.core.tween(300)
                )
            }
        }
    }
}

@Composable
fun SimpleExpenseTransactionsByTagLegend(
    expenseTransactionsByTagData: Map<String, Double>,
    totalBalance: Double,
    defaultCurrency: String
) {
    Column {

        expenseTransactionsByTagData.entries.sortedByDescending { it.value }.forEachIndexed { index, entry ->
            val percentage = (entry.value / totalBalance * 100)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = getWalletColorSimple(index),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = TagFormatter.formatTag(entry.key),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = NumberFormatter.formatCurrency(entry.value, defaultCurrency, showSymbol = true),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleIncomeTransactionsByTagLegend(
    incomeTransactionsByTagData: Map<String, Double>,
    totalBalance: Double,
    defaultCurrency: String
) {
    Column {

        incomeTransactionsByTagData.entries.sortedByDescending { it.value }.forEachIndexed { index, entry ->
            val percentage = (entry.value / totalBalance * 100)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = getWalletColorSimple(index),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = TagFormatter.formatTag(entry.key),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = NumberFormatter.formatCurrency(entry.value, defaultCurrency, showSymbol = true),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ComposeChartsPieChartExpenseTransactionsByTag(
    expenseTransactionsByTagData: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    // Convert expense transaction by tag data to ComposeCharts Pie format
    val pieData = expenseTransactionsByTagData.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
        Pie(
            label = TagFormatter.formatTag(entry.key),
            data = entry.value,
            color = getWalletColorSimple(index)
        )
    }
    
    PieChart(
        modifier = modifier,
        data = pieData,
        onPieClick = { pie ->
            // Optional: Handle pie slice click
            // TODO: Implement proper click handling with Logger
        },
        selectedScale = 1.2f,
        scaleAnimEnterSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        colorAnimEnterSpec = androidx.compose.animation.core.tween(300),
        colorAnimExitSpec = androidx.compose.animation.core.tween(300),
        scaleAnimExitSpec = androidx.compose.animation.core.tween(300),
        spaceDegreeAnimExitSpec = androidx.compose.animation.core.tween(300)
    )
}

@Composable
fun ComposeChartsPieChartIncomeTransactionsByTag(
    incomeTransactionsByTagData: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    // Convert income transaction by tag data to ComposeCharts Pie format
    val pieData = incomeTransactionsByTagData.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
        Pie(
            label = TagFormatter.formatTag(entry.key),
            data = entry.value,
            color = getWalletColorSimple(index)
        )
    }
    
    PieChart(
        modifier = modifier,
        data = pieData,
        onPieClick = { pie ->
            // Optional: Handle pie slice click
            // TODO: Implement proper click handling with Logger
        },
        selectedScale = 1.2f,
        scaleAnimEnterSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        colorAnimEnterSpec = androidx.compose.animation.core.tween(300),
        colorAnimExitSpec = androidx.compose.animation.core.tween(300),
        scaleAnimExitSpec = androidx.compose.animation.core.tween(300),
        spaceDegreeAnimExitSpec = androidx.compose.animation.core.tween(300)
    )
}


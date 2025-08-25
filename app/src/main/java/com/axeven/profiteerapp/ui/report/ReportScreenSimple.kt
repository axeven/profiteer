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
                        totalPortfolioBalance = uiState.totalPortfolioValue,
                        totalWalletBalance = uiState.totalPhysicalWalletValue,
                        defaultCurrency = uiState.defaultCurrency
                    )
                }
                
                // Pie Chart
                item {
                    val hasPortfolioData = uiState.portfolioComposition.isNotEmpty() && uiState.totalPortfolioValue > 0
                    val hasPhysicalWalletData = uiState.physicalWalletBalances.isNotEmpty() && uiState.totalPhysicalWalletValue > 0
                    val hasAnyData = hasPortfolioData || hasPhysicalWalletData
                    
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
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (uiState.selectedChartDataType) {
                                        ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> {
                                            if (hasPortfolioData) {
                                                ComposeChartsPieChartAsset(
                                                    portfolioData = uiState.portfolioComposition,
                                                    modifier = Modifier.size(200.dp)
                                                )
                                            }
                                        }
                                        ChartDataType.PHYSICAL_WALLET_BALANCE -> {
                                            if (hasPhysicalWalletData) {
                                                ComposeChartsPieChartWallet(
                                                    walletData = uiState.physicalWalletBalances,
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
                        hasPhysicalWalletData = uiState.physicalWalletBalances.isNotEmpty() && uiState.totalPhysicalWalletValue > 0
                    )
                }
                
                // Chart Breakdown Section (moved to bottom)
                item {
                    val hasPortfolioData = uiState.portfolioComposition.isNotEmpty() && uiState.totalPortfolioValue > 0
                    val hasPhysicalWalletData = uiState.physicalWalletBalances.isNotEmpty() && uiState.totalPhysicalWalletValue > 0
                    val hasData = when (uiState.selectedChartDataType) {
                        ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> hasPortfolioData
                        ChartDataType.PHYSICAL_WALLET_BALANCE -> hasPhysicalWalletData
                    }
                    
                    if (hasData) {
                        ChartBreakdownSection(
                            selectedDataType = uiState.selectedChartDataType,
                            portfolioData = uiState.portfolioComposition,
                            walletData = uiState.physicalWalletBalances,
                            totalPortfolioBalance = uiState.totalPortfolioValue,
                            totalWalletBalance = uiState.totalPhysicalWalletValue,
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
    totalPortfolioBalance: Double,
    totalWalletBalance: Double,
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
            }
            
            val hasData = when (selectedDataType) {
                ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> portfolioData.isNotEmpty() && totalPortfolioBalance > 0
                ChartDataType.PHYSICAL_WALLET_BALANCE -> walletData.isNotEmpty() && totalWalletBalance > 0
            }
            
            Text(
                text = when (selectedDataType) {
                    ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> 
                        "Total Portfolio Value: ${NumberFormatter.formatCurrency(totalValue, defaultCurrency, showSymbol = true)}"
                    ChartDataType.PHYSICAL_WALLET_BALANCE -> 
                        "Total Physical Wallet Value: ${NumberFormatter.formatCurrency(totalValue, defaultCurrency, showSymbol = true)}"
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
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (selectedDataType) {
                                ChartDataType.PORTFOLIO_ASSET_COMPOSITION -> "Add some wallets to see your asset composition"
                                ChartDataType.PHYSICAL_WALLET_BALANCE -> "Add some physical wallets to see their balance composition"
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
    totalPortfolioBalance: Double,
    totalWalletBalance: Double,
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
            }
        }
    }
}

@Composable
fun ChartOptionsSection(
    selectedDataType: ChartDataType,
    onDataTypeChange: (ChartDataType) -> Unit,
    hasPortfolioData: Boolean,
    hasPhysicalWalletData: Boolean
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
            println("Clicked on ${pie.label}: ${pie.data}")
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
            println("Clicked on ${pie.label}: ${pie.data}")
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



package com.axeven.profiteerapp.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SimplePortfolioAssetCard(
                        portfolioData = uiState.portfolioComposition,
                        totalBalance = uiState.totalPortfolioValue,
                        defaultCurrency = uiState.defaultCurrency
                    )
                }
                
                // Pie Chart
                item {
                    if (uiState.portfolioComposition.isNotEmpty() && uiState.totalPortfolioValue > 0) {
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
                                    text = "Portfolio Visualization",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ComposeChartsPieChart(
                                        portfolioData = uiState.portfolioComposition,
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
}

@Composable
fun SimplePortfolioAssetCard(
    portfolioData: Map<PhysicalForm, Double>,
    totalBalance: Double,
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
            Text(
                text = "Portfolio Asset Composition",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Total Portfolio Value: ${NumberFormatter.formatCurrency(totalBalance, defaultCurrency, showSymbol = true)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (portfolioData.isEmpty() || totalBalance <= 0) {
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
                            text = "No portfolio data available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add some wallets to see your asset composition",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Show asset breakdown as list
                SimplePortfolioLegend(
                    portfolioData = portfolioData,
                    totalBalance = totalBalance,
                    defaultCurrency = defaultCurrency
                )
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
        Text(
            text = "Asset Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        portfolioData.entries.forEachIndexed { index, entry ->
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
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
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
fun ComposeChartsPieChart(
    portfolioData: Map<PhysicalForm, Double>,
    modifier: Modifier = Modifier
) {
    // Convert portfolio data to ComposeCharts Pie format
    val pieData = portfolioData.entries.mapIndexed { index, entry ->
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



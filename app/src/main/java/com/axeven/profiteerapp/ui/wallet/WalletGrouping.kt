package com.axeven.profiteerapp.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.utils.NumberFormatter

/**
 * Displays wallets grouped by their physical form with collapsible sections.
 */
@Composable
fun GroupedWalletList(
    groupedWallets: Map<PhysicalForm, List<Wallet>>,
    defaultCurrency: String,
    conversionRates: Map<String, Double>,
    onWalletClick: (String) -> Unit,
    onWalletEdit: (Wallet) -> Unit,
    onWalletDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedForms by remember { mutableStateOf(setOf<PhysicalForm>()) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Sort groups by total balance (descending)
        val sortedGroups = groupedWallets.toList().sortedByDescending { (_, wallets) ->
            wallets.sumOf { wallet ->
                wallet.balance // All wallets use default currency now
            }
        }
        
        sortedGroups.forEach { (physicalForm, wallets) ->
            if (wallets.isNotEmpty()) {
                item {
                    PhysicalFormGroupHeader(
                        physicalForm = physicalForm,
                        wallets = wallets,
                        defaultCurrency = defaultCurrency,
                        conversionRates = conversionRates,
                        isExpanded = expandedForms.contains(physicalForm),
                        onToggleExpanded = { 
                            expandedForms = if (expandedForms.contains(physicalForm)) {
                                expandedForms - physicalForm
                            } else {
                                expandedForms + physicalForm
                            }
                        }
                    )
                }
                
                if (expandedForms.contains(physicalForm)) {
                    items(wallets) { wallet ->
                        Box(modifier = Modifier.padding(start = 16.dp)) {
                            WalletListItem(
                                wallet = wallet,
                                defaultCurrency = defaultCurrency,
                                displayCurrency = defaultCurrency,
                                displayRate = 1.0, // No conversion needed
                                onEdit = { onWalletEdit(wallet) },
                                onDelete = { onWalletDelete(wallet.id) },
                                onClick = { onWalletClick(wallet.id) }
                            )
                        }
                    }
                }
            }
        }
        
        if (groupedWallets.isEmpty()) {
            item {
                EmptyGroupedWalletState()
            }
        }
    }
}

/**
 * Header for each physical form group showing summary information.
 */
@Composable
private fun PhysicalFormGroupHeader(
    physicalForm: PhysicalForm,
    wallets: List<Wallet>,
    defaultCurrency: String,
    conversionRates: Map<String, Double>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val totalBalance = wallets.sumOf { wallet ->
        wallet.balance // All wallets use default currency now
    }
    
    val walletCount = wallets.size
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = physicalForm.icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Column {
                    Text(
                        text = physicalForm.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "$walletCount ${if (walletCount == 1) "wallet" else "wallets"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$defaultCurrency ${NumberFormatter.formatCurrency(totalBalance)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (totalBalance >= 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Collapse" else "Expand",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Physical form filter chips for the wallet list.
 */
@Composable
fun PhysicalFormFilterChips(
    availablePhysicalForms: Set<PhysicalForm>,
    selectedPhysicalForms: Set<PhysicalForm>,
    onFormToggled: (PhysicalForm) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter by Asset Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            if (selectedPhysicalForms.isNotEmpty()) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sort by category groups for better UX
            val sortedForms = availablePhysicalForms.sortedBy { form ->
                when (form) {
                    PhysicalForm.FIAT_CURRENCY, PhysicalForm.CASH_EQUIVALENT -> 0
                    PhysicalForm.STOCKS, PhysicalForm.ETFS, PhysicalForm.BONDS, PhysicalForm.MUTUAL_FUNDS -> 1
                    PhysicalForm.CRYPTOCURRENCY -> 2
                    PhysicalForm.PRECIOUS_METALS, PhysicalForm.REAL_ESTATE, PhysicalForm.COMMODITIES -> 3
                    PhysicalForm.OTHER -> 4
                }
            }
            
            sortedForms.forEach { form ->
                FilterChip(
                    onClick = { onFormToggled(form) },
                    label = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = form.icon,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(form.displayName)
                        }
                    },
                    selected = selectedPhysicalForms.contains(form)
                )
            }
        }
    }
}

/**
 * Toggle button for switching between grouped and flat view.
 */
@Composable
fun GroupingToggleButton(
    isGrouped: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isGrouped) Icons.Default.List else Icons.Default.AccountBox,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isGrouped) "Flat View" else "Group by Type")
    }
}

/**
 * Summary card showing portfolio allocation by physical form.
 */
@Composable
fun PhysicalFormSummaryCard(
    formBalances: Map<PhysicalForm, Double>,
    defaultCurrency: String,
    modifier: Modifier = Modifier
) {
    val totalBalance = formBalances.values.sum()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portfolio Allocation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$defaultCurrency ${NumberFormatter.formatCurrency(totalBalance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                formBalances.toList()
                    .sortedByDescending { it.second }
                    .forEach { (form, balance) ->
                        val percentage = if (totalBalance > 0) (balance / totalBalance * 100) else 0.0
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = form.icon,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = form.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "$defaultCurrency ${NumberFormatter.formatCurrency(balance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${NumberFormatter.formatCurrency(percentage)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // Progress indicator
                        LinearProgressIndicator(
                            progress = (percentage / 100).toFloat(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = when (form) {
                                PhysicalForm.FIAT_CURRENCY, PhysicalForm.CASH_EQUIVALENT -> MaterialTheme.colorScheme.primary
                                PhysicalForm.STOCKS, PhysicalForm.ETFS -> MaterialTheme.colorScheme.secondary
                                PhysicalForm.CRYPTOCURRENCY -> MaterialTheme.colorScheme.tertiary
                                PhysicalForm.PRECIOUS_METALS -> Color(0xFFFFD700) // Gold color
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    }
            }
        }
    }
}

/**
 * Empty state when no grouped wallets are available.
 */
@Composable
private fun EmptyGroupedWalletState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Text(
                text = "No wallets match the current filters",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Try adjusting your physical form filters or create new wallets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

// Currency conversion no longer needed - all wallets use single default currency
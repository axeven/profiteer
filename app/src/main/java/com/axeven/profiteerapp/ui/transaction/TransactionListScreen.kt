package com.axeven.profiteerapp.ui.transaction

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.ui.home.TransactionItem
import com.axeven.profiteerapp.viewmodel.TransactionListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onNavigateBack: () -> Unit = {},
    onEditTransaction: (Transaction) -> Unit = {},
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "All Transactions", 
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
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error loading transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    uiState.error?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(onClick = { viewModel.refreshData() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Filter Section (placeholder for Phase 2)
                item {
                    FilterSection(
                        uiState = uiState,
                        onDateRangeSelected = viewModel::setDateRangeFilter,
                        onPhysicalWalletToggle = viewModel::togglePhysicalWalletFilter,
                        onLogicalWalletToggle = viewModel::toggleLogicalWalletFilter,
                        onTagToggle = viewModel::toggleTagFilter,
                        onClearFilters = viewModel::clearAllFilters
                    )
                }

                // Transaction Groups
                if (uiState.groupedTransactions.isEmpty()) {
                    item {
                        EmptyTransactionState()
                    }
                } else {
                    uiState.groupedTransactions.forEach { (dateGroup, transactions) ->
                        item {
                            TransactionGroupHeader(
                                dateGroup = dateGroup,
                                transactionCount = transactions.size,
                                isExpanded = uiState.expandedGroups.contains(dateGroup),
                                onToggleExpanded = { viewModel.toggleGroupExpansion(dateGroup) }
                            )
                        }

                        if (uiState.expandedGroups.contains(dateGroup)) {
                            items(transactions, key = { it.id }) { transaction ->
                                Box(modifier = Modifier.padding(start = 16.dp)) {
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
            }
        }
    }
}

@Composable
private fun FilterSection(
    uiState: com.axeven.profiteerapp.viewmodel.TransactionListUiState,
    onDateRangeSelected: (Date?, Date?) -> Unit,
    onPhysicalWalletToggle: (String) -> Unit,
    onLogicalWalletToggle: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    var showDateRangePicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Clear All button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                val hasActiveFilters = uiState.selectedDateRange != Pair(null, null) ||
                        uiState.selectedPhysicalWallets.isNotEmpty() ||
                        uiState.selectedLogicalWallets.isNotEmpty() ||
                        uiState.selectedTags.isNotEmpty()
                
                if (hasActiveFilters) {
                    TextButton(onClick = onClearFilters) {
                        Text("Clear All")
                    }
                }
            }
            
            // Date Range Filter
            DateRangeFilter(
                selectedDateRange = uiState.selectedDateRange,
                onDateRangeClick = { showDateRangePicker = true }
            )
            
            // Wallet Filters
            WalletFilters(
                physicalWallets = uiState.wallets.filter { it.walletType == "Physical" },
                logicalWallets = uiState.wallets.filter { it.walletType == "Logical" },
                selectedPhysicalWallets = uiState.selectedPhysicalWallets,
                selectedLogicalWallets = uiState.selectedLogicalWallets,
                onPhysicalWalletToggle = onPhysicalWalletToggle,
                onLogicalWalletToggle = onLogicalWalletToggle
            )
            
            // Tag Filter
            TagFilter(
                availableTags = uiState.availableTags,
                selectedTags = uiState.selectedTags,
                onTagToggle = onTagToggle
            )
        }
    }
    
    // Date Range Picker Dialog
    if (showDateRangePicker) {
        DateRangePickerDialog(
            initialStartDate = uiState.selectedDateRange.first,
            initialEndDate = uiState.selectedDateRange.second,
            onDateRangeSelected = onDateRangeSelected,
            onDismiss = { showDateRangePicker = false }
        )
    }
}

@Composable
private fun TransactionGroupHeader(
    dateGroup: String,
    transactionCount: Int,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateGroup,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$transactionCount transaction${if (transactionCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
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

@Composable
private fun EmptyTransactionState() {
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
                imageVector = Icons.Default.List,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Text(
                text = "No transactions found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Start adding transactions to see them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

// Filter Components

@Composable
private fun DateRangeFilter(
    selectedDateRange: Pair<Date?, Date?>,
    onDateRangeClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Column {
        Text(
            text = "Date Range",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDateRangeClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        selectedDateRange.first != null && selectedDateRange.second != null -> {
                            "${dateFormatter.format(selectedDateRange.first!!)} - ${dateFormatter.format(selectedDateRange.second!!)}"
                        }
                        selectedDateRange.first != null -> {
                            "From ${dateFormatter.format(selectedDateRange.first!!)}"
                        }
                        selectedDateRange.second != null -> {
                            "Until ${dateFormatter.format(selectedDateRange.second!!)}"
                        }
                        else -> "All dates"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    initialStartDate: Date?,
    initialEndDate: Date?,
    onDateRangeSelected: (Date?, Date?) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate?.time,
        initialSelectedEndDateMillis = initialEndDate?.time
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startDate = dateRangePickerState.selectedStartDateMillis?.let { Date(it) }
                    val endDate = dateRangePickerState.selectedEndDateMillis?.let { Date(it) }
                    onDateRangeSelected(startDate, endDate)
                    onDismiss()
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
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletFilters(
    physicalWallets: List<Wallet>,
    logicalWallets: List<Wallet>,
    selectedPhysicalWallets: Set<String>,
    selectedLogicalWallets: Set<String>,
    onPhysicalWalletToggle: (String) -> Unit,
    onLogicalWalletToggle: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Wallets",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Physical Wallets Dropdown
            WalletDropdown(
                label = "Physical",
                wallets = physicalWallets,
                selectedWallets = selectedPhysicalWallets,
                onWalletToggle = onPhysicalWalletToggle,
                modifier = Modifier.weight(1f)
            )
            
            // Logical Wallets Dropdown
            WalletDropdown(
                label = "Logical",
                wallets = logicalWallets,
                selectedWallets = selectedLogicalWallets,
                onWalletToggle = onLogicalWalletToggle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDropdown(
    label: String,
    wallets: List<Wallet>,
    selectedWallets: Set<String>,
    onWalletToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = when {
                    selectedWallets.isEmpty() -> "All $label"
                    selectedWallets.size == 1 -> {
                        wallets.find { it.id in selectedWallets }?.name ?: "1 selected"
                    }
                    else -> "${selectedWallets.size} selected"
                },
                onValueChange = { },
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                wallets.forEach { wallet ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedWallets.contains(wallet.id),
                                    onCheckedChange = { onWalletToggle(wallet.id) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(wallet.name)
                            }
                        },
                        onClick = { onWalletToggle(wallet.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TagFilter(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit
) {
    if (availableTags.isNotEmpty()) {
        var expanded by remember { mutableStateOf(false) }
        
        Column {
            Text(
                text = "Tags",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = when (selectedTags.size) {
                        0 -> "All Tags"
                        1 -> selectedTags.first()
                        else -> "${selectedTags.size} tags selected"
                    },
                    onValueChange = { },
                    readOnly = true,
                    placeholder = { Text("Select tags") },
                    trailingIcon = { 
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Clear All option
                    if (selectedTags.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Clear All",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            onClick = {
                                selectedTags.forEach { tag ->
                                    onTagToggle(tag)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                    
                    // Tag options
                    availableTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onTagToggle(tag) },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tag,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            },
                            onClick = { onTagToggle(tag) }
                        )
                    }
                }
            }
        }
    }
}
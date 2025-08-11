package com.axeven.profiteer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.axeven.profiteer.viewmodel.SettingsViewModel

import com.axeven.profiteer.data.model.Wallet
import com.axeven.profiteer.data.model.CurrencyRate
import com.axeven.profiteer.utils.NumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showWalletDialog by remember { mutableStateOf(false) }
    var showEditWalletDialog by remember { mutableStateOf(false) }
    var walletToEdit by remember { mutableStateOf<Wallet?>(null) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    
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
                        "Settings", 
                        fontWeight = FontWeight.Bold
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wallet Configuration Section
            item {
                SettingSectionHeader("Wallet Configuration")
            }
            
            item {
                SettingCard(
                    title = "Create New Wallet",
                    subtitle = "Add a new wallet to manage your finances",
                    icon = Icons.Default.Add,
                    onClick = { showWalletDialog = true }
                )
            }
            
            items(uiState.wallets.size) { index ->
                val wallet = uiState.wallets[index]
                WalletItem(
                    wallet = wallet,
                    onEdit = { 
                        walletToEdit = wallet
                        showEditWalletDialog = true
                    },
                    onDelete = { viewModel.deleteWallet(wallet.id) }
                )
            }
            
            // Currency Configuration Section
            item {
                SettingSectionHeader("Currency Configuration")
            }
            
            item {
                CurrencySelector(
                    selectedCurrency = uiState.defaultCurrency,
                    onCurrencySelected = { viewModel.updateDefaultCurrency(it) },
                    onClick = { showCurrencyDialog = true }
                )
            }
            
            // Conversion Rate Section
            item {
                SettingSectionHeader("Currency Conversion Rates")
            }
            
            item {
                SettingCard(
                    title = "Add Default Conversion Rate",
                    subtitle = "Set flat conversion rate for all times",
                    icon = Icons.Default.Refresh,
                    onClick = { showRateDialog = true }
                )
            }
            
            item {
                SettingCard(
                    title = "Add Monthly Conversion Rate",
                    subtitle = "Set specific conversion rate for certain month",
                    icon = Icons.Default.DateRange,
                    onClick = { showRateDialog = true }
                )
            }
            
            items(uiState.currencyRates.size) { index ->
                val rate = uiState.currencyRates[index]
                ConversionRateItem(
                    rate = rate,
                    onEdit = { /* TODO: Edit rate */ },
                    onDelete = { viewModel.deleteCurrencyRate(rate.id) }
                )
            }
        }
    }
    
    // Dialogs
    if (showWalletDialog) {
        CreateWalletDialog(
            onDismiss = { showWalletDialog = false },
            onConfirm = { name, walletType, currency, initialBalance ->
                viewModel.createWallet(name, currency, walletType, initialBalance)
                showWalletDialog = false
            },
            defaultCurrency = uiState.defaultCurrency,
            existingWallets = uiState.wallets
        )
    }
    
    if (showEditWalletDialog && walletToEdit != null) {
        EditWalletDialog(
            wallet = walletToEdit!!,
            onDismiss = { 
                showEditWalletDialog = false
                walletToEdit = null
            },
            onConfirm = { name, walletType, currency, initialBalance ->
                viewModel.updateWallet(walletToEdit!!.id, name, currency, walletType, initialBalance)
                showEditWalletDialog = false
                walletToEdit = null
            },
            defaultCurrency = uiState.defaultCurrency,
            existingWallets = uiState.wallets
        )
    }
    
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = uiState.defaultCurrency,
            onDismiss = { showCurrencyDialog = false },
            onConfirm = { currency ->
                viewModel.updateDefaultCurrency(currency)
                showCurrencyDialog = false
            }
        )
    }
    
    if (showRateDialog) {
        ConversionRateDialog(
            onDismiss = { showRateDialog = false },
            onConfirm = { fromCurrency, toCurrency, rate, month ->
                viewModel.createCurrencyRate(fromCurrency, toCurrency, rate, month)
                showRateDialog = false
            },
            defaultCurrency = uiState.defaultCurrency
        )
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WalletItem(
    wallet: Wallet,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${wallet.walletType} • ${wallet.currency} ${NumberFormatter.formatCurrency(wallet.balance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onClick: () -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Default Currency",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = selectedCurrency,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ConversionRateItem(
    rate: CurrencyRate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (rate.month == null) Icons.Default.Refresh else Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${rate.fromCurrency} → ${rate.toCurrency}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Rate: ${NumberFormatter.formatCurrency(rate.rate)}${rate.month?.let { " • $it" } ?: " • Default"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EditWalletDialog(
    wallet: Wallet,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double) -> Unit,
    defaultCurrency: String = "USD",
    existingWallets: List<Wallet> = emptyList()
) {
    var walletName by remember { mutableStateOf(wallet.name) }
    var selectedCurrency by remember { mutableStateOf(wallet.currency) }
    var selectedWalletType by remember { mutableStateOf(wallet.walletType) }
    var initialBalanceText by remember { mutableStateOf(NumberFormatter.formatCurrency(wallet.initialBalance)) }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    var showWalletTypeDropdown by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR")
    val walletTypes = listOf("Physical", "Logical")
    
    val isNameDuplicate = existingWallets
        .filter { it.id != wallet.id } // Exclude current wallet from check
        .any { it.name.equals(walletName, ignoreCase = true) }
    val initialBalance = NumberFormatter.parseDouble(initialBalanceText) ?: 0.0
    val isFormValid = walletName.isNotBlank() && !isNameDuplicate && NumberFormatter.parseDouble(initialBalanceText) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Wallet") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    label = { Text("Wallet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isNameDuplicate && walletName.isNotBlank(),
                    supportingText = if (isNameDuplicate && walletName.isNotBlank()) {
                        { Text("Wallet name already exists", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                Box {
                    OutlinedTextField(
                        value = selectedWalletType,
                        onValueChange = { },
                        label = { Text("Wallet Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWalletTypeDropdown = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (showWalletTypeDropdown) Icons.Default.KeyboardArrowUp 
                                            else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showWalletTypeDropdown,
                        onDismissRequest = { showWalletTypeDropdown = false }
                    ) {
                        walletTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedWalletType = type
                                    showWalletTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Box {
                    OutlinedTextField(
                        value = selectedCurrency,
                        onValueChange = { },
                        label = { Text("Currency") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCurrencyDropdown = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (showCurrencyDropdown) Icons.Default.KeyboardArrowUp 
                                            else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showCurrencyDropdown,
                        onDismissRequest = { showCurrencyDropdown = false }
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    selectedCurrency = currency
                                    showCurrencyDropdown = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text("Initial Balance") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = NumberFormatter.parseDouble(initialBalanceText) == null && initialBalanceText.isNotBlank(),
                    supportingText = if (NumberFormatter.parseDouble(initialBalanceText) == null && initialBalanceText.isNotBlank()) {
                        { Text("Please enter a valid amount", color = MaterialTheme.colorScheme.error) }
                    } else {
                        { Text("This balance won't appear in transaction analytics") }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(walletName, selectedWalletType, selectedCurrency, initialBalance) },
                enabled = isFormValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double) -> Unit,
    defaultCurrency: String = "USD",
    existingWallets: List<Wallet> = emptyList()
) {
    var walletName by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(defaultCurrency) }
    var selectedWalletType by remember { mutableStateOf("Physical") }
    var initialBalanceText by remember { mutableStateOf("0.00") }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    var showWalletTypeDropdown by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR")
    val walletTypes = listOf("Physical", "Logical")
    
    val isNameDuplicate = existingWallets.any { it.name.equals(walletName, ignoreCase = true) }
    val initialBalance = NumberFormatter.parseDouble(initialBalanceText) ?: 0.0
    val isFormValid = walletName.isNotBlank() && !isNameDuplicate && NumberFormatter.parseDouble(initialBalanceText) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Wallet") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    label = { Text("Wallet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isNameDuplicate && walletName.isNotBlank(),
                    supportingText = if (isNameDuplicate && walletName.isNotBlank()) {
                        { Text("Wallet name already exists", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                Box {
                    OutlinedTextField(
                        value = selectedWalletType,
                        onValueChange = { },
                        label = { Text("Wallet Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWalletTypeDropdown = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (showWalletTypeDropdown) Icons.Default.KeyboardArrowUp 
                                            else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showWalletTypeDropdown,
                        onDismissRequest = { showWalletTypeDropdown = false }
                    ) {
                        walletTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedWalletType = type
                                    showWalletTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Box {
                    OutlinedTextField(
                        value = selectedCurrency,
                        onValueChange = { },
                        label = { Text("Currency") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCurrencyDropdown = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (showCurrencyDropdown) Icons.Default.KeyboardArrowUp 
                                            else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showCurrencyDropdown,
                        onDismissRequest = { showCurrencyDropdown = false }
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    selectedCurrency = currency
                                    showCurrencyDropdown = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text("Initial Balance") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = NumberFormatter.parseDouble(initialBalanceText) == null && initialBalanceText.isNotBlank(),
                    supportingText = if (NumberFormatter.parseDouble(initialBalanceText) == null && initialBalanceText.isNotBlank()) {
                        { Text("Please enter a valid amount", color = MaterialTheme.colorScheme.error) }
                    } else {
                        { Text("This balance won't appear in transaction analytics") }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(walletName, selectedWalletType, selectedCurrency, initialBalance) },
                enabled = isFormValid
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CurrencySelectionDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR")
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Default Currency") },
        text = {
            LazyColumn {
                items(currencies.size) { index ->
                    val currency = currencies[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCurrency = currency }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCurrency == currency,
                            onClick = { selectedCurrency = currency }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = currency)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedCurrency) }) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConversionRateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit,
    defaultCurrency: String = "USD"
) {
    var fromCurrency by remember { mutableStateOf(defaultCurrency) }
    var toCurrency by remember { mutableStateOf(if (defaultCurrency == "USD") "EUR" else "USD") }
    var rateText by remember { mutableStateOf("") }
    var monthText by remember { mutableStateOf("") }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Conversion Rate") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // From Currency Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = fromCurrency,
                            onValueChange = { },
                            label = { Text("From") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFromDropdown = true },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showFromDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenu(
                            expanded = showFromDropdown,
                            onDismissRequest = { showFromDropdown = false }
                        ) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency) },
                                    onClick = {
                                        fromCurrency = currency
                                        showFromDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // To Currency Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = toCurrency,
                            onValueChange = { },
                            label = { Text("To") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showToDropdown = true },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showToDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenu(
                            expanded = showToDropdown,
                            onDismissRequest = { showToDropdown = false }
                        ) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency) },
                                    onClick = {
                                        toCurrency = currency
                                        showToDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Conversion Rate") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = monthText,
                    onValueChange = { monthText = it },
                    label = { Text("Month (optional)") },
                    placeholder = { Text("e.g., January 2024") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    val rate = rateText.toDoubleOrNull()
                    if (rate != null) {
                        onConfirm(fromCurrency, toCurrency, rate, monthText)
                    }
                },
                enabled = fromCurrency.isNotBlank() && toCurrency.isNotBlank() && rateText.toDoubleOrNull() != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
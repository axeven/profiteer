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

data class Wallet(
    val id: String,
    val name: String,
    val currency: String,
    val balance: Double
)

data class CurrencyRate(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double,
    val month: String? = null // null means default rate for all times
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit = {}) {
    var showWalletDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var selectedDefaultCurrency by remember { mutableStateOf("USD") }
    
    val dummyWallets = remember { mutableStateListOf(
        Wallet("1", "Main Wallet", "USD", 5000.0),
        Wallet("2", "Savings", "EUR", 2000.0)
    )}
    
    val dummyRates = remember { mutableStateListOf(
        CurrencyRate("USD", "EUR", 0.85),
        CurrencyRate("USD", "GBP", 0.73),
        CurrencyRate("USD", "EUR", 0.82, "January 2024")
    )}

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
            
            items(dummyWallets.size) { index ->
                val wallet = dummyWallets[index]
                WalletItem(
                    wallet = wallet,
                    onEdit = { /* TODO: Edit wallet */ },
                    onDelete = { dummyWallets.removeAt(index) }
                )
            }
            
            // Currency Configuration Section
            item {
                SettingSectionHeader("Currency Configuration")
            }
            
            item {
                CurrencySelector(
                    selectedCurrency = selectedDefaultCurrency,
                    onCurrencySelected = { selectedDefaultCurrency = it },
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
            
            items(dummyRates.size) { index ->
                val rate = dummyRates[index]
                ConversionRateItem(
                    rate = rate,
                    onEdit = { /* TODO: Edit rate */ },
                    onDelete = { dummyRates.removeAt(index) }
                )
            }
        }
    }
    
    // Dialogs
    if (showWalletDialog) {
        CreateWalletDialog(
            onDismiss = { showWalletDialog = false },
            onConfirm = { name, currency ->
                dummyWallets.add(
                    Wallet(
                        id = (dummyWallets.size + 1).toString(),
                        name = name,
                        currency = currency,
                        balance = 0.0
                    )
                )
                showWalletDialog = false
            }
        )
    }
    
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = selectedDefaultCurrency,
            onDismiss = { showCurrencyDialog = false },
            onConfirm = { currency ->
                selectedDefaultCurrency = currency
                showCurrencyDialog = false
            }
        )
    }
    
    if (showRateDialog) {
        ConversionRateDialog(
            onDismiss = { showRateDialog = false },
            onConfirm = { fromCurrency, toCurrency, rate, month ->
                dummyRates.add(
                    CurrencyRate(fromCurrency, toCurrency, rate, month.takeIf { it.isNotBlank() })
                )
                showRateDialog = false
            }
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
                    text = "${wallet.currency} ${String.format("%.2f", wallet.balance)}",
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
                    text = "Rate: ${rate.rate}${rate.month?.let { " • $it" } ?: " • Default"}",
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
fun CreateWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var walletName by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }

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
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = selectedCurrency,
                    onValueChange = { selectedCurrency = it },
                    label = { Text("Currency") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(walletName, selectedCurrency) },
                enabled = walletName.isNotBlank()
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
    onConfirm: (String, String, Double, String) -> Unit
) {
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("EUR") }
    var rateText by remember { mutableStateOf("") }
    var monthText by remember { mutableStateOf("") }

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
                    OutlinedTextField(
                        value = fromCurrency,
                        onValueChange = { fromCurrency = it },
                        label = { Text("From") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = toCurrency,
                        onValueChange = { toCurrency = it },
                        label = { Text("To") },
                        modifier = Modifier.weight(1f)
                    )
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
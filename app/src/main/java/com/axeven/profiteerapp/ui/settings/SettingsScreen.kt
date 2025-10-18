package com.axeven.profiteerapp.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.axeven.profiteerapp.viewmodel.SettingsViewModel

import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.CurrencyRate
import com.axeven.profiteerapp.data.ui.*
import com.axeven.profiteerapp.utils.NumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Consolidated state management - replaces 4 individual mutableStateOf variables
    var settingsState by remember { mutableStateOf(SettingsUiState()) }
    
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
            // Currency Configuration Section
            item {
                SettingSectionHeader("Currency Configuration")
            }
            
            item {
                CurrencySelector(
                    selectedCurrency = uiState.defaultCurrency,
                    onCurrencySelected = { viewModel.updateDefaultCurrency(it) },
                    onClick = {
                        settingsState = settingsState.openDialog(SettingsDialogType.CURRENCY)
                    }
                )
            }
            
            // Conversion Rate Section
            item {
                SettingSectionHeader("Currency Conversion Rates")
            }
            
            item {
                SettingCard(
                    title = "Add Default Conversion Rate",
                    subtitle = "Set flat conversion rate for all times (including gold price per gram)",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        settingsState = settingsState.openDialog(SettingsDialogType.RATE)
                    }
                )
            }
            
            item {
                SettingCard(
                    title = "Add Monthly Conversion Rate",
                    subtitle = "Set specific conversion rate for certain month (including monthly gold price)",
                    icon = Icons.Default.DateRange,
                    onClick = {
                        settingsState = settingsState.openDialog(SettingsDialogType.RATE)
                    }
                )
            }
            
            items(uiState.currencyRates.size) { index ->
                val rate = uiState.currencyRates[index]
                ConversionRateItem(
                    rate = rate,
                    onEdit = {
                        settingsState = settingsState.openEditRateDialog(rate).initializeEditForm(rate)
                    },
                    onDelete = { viewModel.deleteCurrencyRate(rate.id) }
                )
            }
        }
    }
    
    // Dialogs

    if (settingsState.dialogStates.showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = uiState.defaultCurrency,
            onDismiss = {
                settingsState = settingsState.closeAllDialogs()
            },
            onConfirm = { currency ->
                viewModel.updateDefaultCurrency(currency)
                settingsState = settingsState.updateSelectedCurrency(currency).closeAllDialogs()
            }
        )
    }
    
    if (settingsState.dialogStates.showRateDialog) {
        ConversionRateDialog(
            onDismiss = {
                settingsState = settingsState.closeAllDialogs()
            },
            onConfirm = { fromCurrency, toCurrency, rate, month ->
                viewModel.createCurrencyRate(fromCurrency, toCurrency, rate, month)
                settingsState = settingsState.closeAllDialogs()
            },
            defaultCurrency = uiState.defaultCurrency
        )
    }
    
    if (settingsState.dialogStates.showEditRateDialog && settingsState.selectedRateForEdit != null) {
        EditConversionRateDialog(
            rate = settingsState.selectedRateForEdit!!,
            onDismiss = {
                settingsState = settingsState.closeAllDialogs()
            },
            onConfirm = { fromCurrency, toCurrency, rate, month ->
                viewModel.updateCurrencyRate(settingsState.selectedRateForEdit!!.id, fromCurrency, toCurrency, rate, month)
                settingsState = settingsState.closeAllDialogs()
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
                val rateDisplay = if (rate.fromCurrency == "GOLD" || rate.toCurrency == "GOLD") {
                    "Price: ${NumberFormatter.formatCurrency(rate.rate)} per gram${rate.month?.let { " • $it" } ?: " • Default"}"
                } else {
                    "Rate: ${NumberFormatter.formatCurrency(rate.rate)}${rate.month?.let { " • $it" } ?: " • Default"}"
                }
                
                Text(
                    text = rateDisplay,
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
fun CurrencySelectionDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR", "GOLD", "BTC")
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
    var selectedYear by remember { mutableStateOf("Default") }
    var selectedMonth by remember { mutableStateOf("All Months") }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR", "GOLD", "BTC")
    val years = listOf("Default", "2024", "2025", "2026")
    val months = listOf("All Months", "January", "February", "March", "April", "May", "June",
                       "July", "August", "September", "October", "November", "December")

    val isSpecialRate = fromCurrency == "GOLD" || toCurrency == "GOLD" || fromCurrency == "BTC" || toCurrency == "BTC"
    val titleText = when {
        fromCurrency == "GOLD" || toCurrency == "GOLD" -> "Add Gold Price"
        fromCurrency == "BTC" || toCurrency == "BTC" -> "Add Bitcoin Rate"
        else -> "Add Conversion Rate"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
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
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showFromDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Clickable overlay to handle dropdown opening
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showFromDropdown = true }
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
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showToDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Clickable overlay to handle dropdown opening
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showToDropdown = true }
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
                
                val rateLabel = when {
                    fromCurrency == "GOLD" || toCurrency == "GOLD" -> {
                        if (fromCurrency == "GOLD") "Price per gram in $toCurrency" else "Price per gram in $fromCurrency"
                    }
                    fromCurrency == "BTC" || toCurrency == "BTC" -> {
                        if (fromCurrency == "BTC") "Price per BTC in $toCurrency" else "Price per BTC in $fromCurrency"
                    }
                    else -> "Conversion Rate"
                }
                
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text(rateLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    placeholder = { Text(when {
                        fromCurrency == "GOLD" || toCurrency == "GOLD" -> "e.g., 65.50"
                        fromCurrency == "BTC" || toCurrency == "BTC" -> "e.g., 45000.00"
                        else -> "e.g., 1.25"
                    }) }
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Year Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedYear,
                            onValueChange = { },
                            label = { Text("Year") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showYearDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Clickable overlay to handle dropdown opening
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showYearDropdown = true }
                        )
                        
                        DropdownMenu(
                            expanded = showYearDropdown,
                            onDismissRequest = { showYearDropdown = false }
                        ) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        selectedYear = year
                                        showYearDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Month Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = { },
                            label = { Text("Month") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showMonthDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Clickable overlay to handle dropdown opening
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showMonthDropdown = true }
                        )
                        
                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false }
                        ) {
                            months.forEach { month ->
                                DropdownMenuItem(
                                    text = { Text(month) },
                                    onClick = {
                                        selectedMonth = month
                                        showMonthDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    val rate = rateText.toDoubleOrNull()
                    if (rate != null) {
                        val monthValue = if (selectedYear == "Default" || selectedMonth == "All Months") {
                            ""
                        } else {
                            "$selectedMonth $selectedYear"
                        }
                        onConfirm(fromCurrency, toCurrency, rate, monthValue)
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

@Composable
fun EditConversionRateDialog(
    rate: CurrencyRate,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit,
    defaultCurrency: String = "USD"
) {
    var fromCurrency by remember { mutableStateOf(rate.fromCurrency) }
    var toCurrency by remember { mutableStateOf(rate.toCurrency) }
    var rateText by remember { mutableStateOf(rate.rate.toString()) }
    
    // Parse existing month value to separate year and month
    val (initialYear, initialMonth) = if (rate.month.isNullOrEmpty()) {
        "Default" to "All Months"
    } else {
        val parts = rate.month.split(" ")
        if (parts.size == 2) {
            parts[1] to parts[0] // "January 2024" -> "2024" to "January"
        } else {
            "Default" to "All Months"
        }
    }
    
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR", "GOLD", "BTC")
    val years = listOf("Default", "2024", "2025", "2026")
    val months = listOf("All Months", "January", "February", "March", "April", "May", "June", 
                       "July", "August", "September", "October", "November", "December")

    val isSpecialRate = fromCurrency == "GOLD" || toCurrency == "GOLD" || fromCurrency == "BTC" || toCurrency == "BTC"
    val titleText = when {
        fromCurrency == "GOLD" || toCurrency == "GOLD" -> "Edit Gold Price"
        fromCurrency == "BTC" || toCurrency == "BTC" -> "Edit Bitcoin Price"
        else -> "Edit Conversion Rate"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
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
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showFromDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Clickable overlay to handle dropdown opening
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showFromDropdown = true }
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
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showToDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Clickable overlay to handle dropdown opening
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showToDropdown = true }
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
                
                val rateLabel = when {
                    fromCurrency == "GOLD" || toCurrency == "GOLD" -> {
                        if (fromCurrency == "GOLD") "Price per gram in $toCurrency" else "Price per gram in $fromCurrency"
                    }
                    fromCurrency == "BTC" || toCurrency == "BTC" -> {
                        if (fromCurrency == "BTC") "Price per BTC in $toCurrency" else "Price per BTC in $fromCurrency"
                    }
                    else -> "Conversion Rate"
                }
                
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text(rateLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    placeholder = { Text(when {
                        fromCurrency == "GOLD" || toCurrency == "GOLD" -> "e.g., 65.50"
                        fromCurrency == "BTC" || toCurrency == "BTC" -> "e.g., 45000.00"
                        else -> "e.g., 1.25"
                    }) }
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Year Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedYear,
                            onValueChange = { },
                            label = { Text("Year") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showYearDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Clickable overlay to handle dropdown opening
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showYearDropdown = true }
                        )
                        
                        DropdownMenu(
                            expanded = showYearDropdown,
                            onDismissRequest = { showYearDropdown = false }
                        ) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        selectedYear = year
                                        showYearDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Month Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = { },
                            label = { Text("Month") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showMonthDropdown) Icons.Default.KeyboardArrowUp 
                                                else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Clickable overlay to handle dropdown opening
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showMonthDropdown = true }
                        )
                        
                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false }
                        ) {
                            months.forEach { month ->
                                DropdownMenuItem(
                                    text = { Text(month) },
                                    onClick = {
                                        selectedMonth = month
                                        showMonthDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    val rateValue = rateText.toDoubleOrNull()
                    if (rateValue != null) {
                        val monthValue = if (selectedYear == "Default" || selectedMonth == "All Months") {
                            ""
                        } else {
                            "$selectedMonth $selectedYear"
                        }
                        onConfirm(fromCurrency, toCurrency, rateValue, monthValue)
                    }
                },
                enabled = fromCurrency.isNotBlank() && toCurrency.isNotBlank() && rateText.toDoubleOrNull() != null
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
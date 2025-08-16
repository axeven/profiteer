package com.axeven.profiteerapp.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionScreen(
    initialTransactionType: TransactionType? = null,
    preSelectedWalletId: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialTransactionType ?: TransactionType.EXPENSE) }
    var selectedPhysicalWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedLogicalWallet by remember { mutableStateOf<Wallet?>(null) }
    var tags by remember { mutableStateOf("") }
    var selectedSourceWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedDestinationWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPhysicalWalletPicker by remember { mutableStateOf(false) }
    var showLogicalWalletPicker by remember { mutableStateOf(false) }
    var showSourceWalletPicker by remember { mutableStateOf(false) }
    var showDestinationWalletPicker by remember { mutableStateOf(false) }
    
    val allSelectedWallets = listOfNotNull(selectedPhysicalWallet, selectedLogicalWallet)
    
    // Handle wallet pre-selection
    LaunchedEffect(preSelectedWalletId, uiState.wallets) {
        if (preSelectedWalletId != null && uiState.wallets.isNotEmpty()) {
            val preSelectedWallet = uiState.wallets.find { it.id == preSelectedWalletId }
            preSelectedWallet?.let { wallet ->
                when (wallet.walletType) {
                    "Physical" -> selectedPhysicalWallet = wallet
                    "Logical" -> selectedLogicalWallet = wallet
                }
                // For transfer transactions, pre-select as source wallet
                if (selectedType == TransactionType.TRANSFER) {
                    selectedSourceWallet = wallet
                }
            }
        }
    }
    
    val isFormValid = when (selectedType) {
        TransactionType.INCOME, TransactionType.EXPENSE -> {
            title.isNotBlank() && amount.isNotBlank() && allSelectedWallets.isNotEmpty() && amount.toDoubleOrNull() != null
        }
        TransactionType.TRANSFER -> {
            title.isNotBlank() && amount.isNotBlank() && 
            selectedSourceWallet != null && selectedDestinationWallet != null && 
            selectedSourceWallet != selectedDestinationWallet &&
            selectedSourceWallet?.currency == selectedDestinationWallet?.currency &&
            selectedSourceWallet?.walletType == selectedDestinationWallet?.walletType &&
            amount.toDoubleOrNull() != null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Create Transaction", 
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
            // Transaction Type Selection
            item {
                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        FilterChip(
                            onClick = { selectedType = type },
                            label = { 
                                Text(
                                    when (type) {
                                        TransactionType.INCOME -> "Income"
                                        TransactionType.EXPENSE -> "Expense"
                                        TransactionType.TRANSFER -> "Transfer"
                                    }
                                )
                            },
                            selected = selectedType == type,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Title Field
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Transaction Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Amount Field
            item {
                val currencySymbol = when (selectedType) {
                    TransactionType.TRANSFER -> selectedSourceWallet?.currency?.let { 
                        com.axeven.profiteerapp.utils.NumberFormatter.getCurrencySymbol(it)
                    } ?: com.axeven.profiteerapp.utils.NumberFormatter.getCurrencySymbol(uiState.defaultCurrency)
                    else -> {
                        // For Income/Expense, use the currency from any selected wallet
                        (selectedPhysicalWallet?.currency ?: selectedLogicalWallet?.currency)?.let {
                            com.axeven.profiteerapp.utils.NumberFormatter.getCurrencySymbol(it)
                        } ?: com.axeven.profiteerapp.utils.NumberFormatter.getCurrencySymbol(uiState.defaultCurrency)
                    }
                }
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { 
                        Text(
                            if (selectedType == TransactionType.TRANSFER) "Transfer Amount" 
                            else "Amount"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text(currencySymbol) }
                )
            }
            
            // Tags Field (not for transfers)
            if (selectedType != TransactionType.TRANSFER) {
                item {
                    TagInputField(
                        value = tags,
                        onValueChange = { tags = it },
                        availableTags = uiState.availableTags,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Wallet Selection for Income/Expense
            if (selectedType == TransactionType.INCOME || selectedType == TransactionType.EXPENSE) {
                // Physical Wallets Section
                item {
                    Text(
                        text = "Physical Wallet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPhysicalWalletPicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedPhysicalWallet?.name ?: "Choose physical wallet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    selectedPhysicalWallet?.let { wallet ->
                                        Text(
                                            text = "${wallet.walletType} • ${wallet.currency}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    } ?: run {
                                        Text(
                                            text = "Select physical wallet to be affected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                
                // Logical Wallets Section
                item {
                    Text(
                        text = "Logical Wallet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogicalWalletPicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedLogicalWallet?.name ?: "Choose logical wallet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    selectedLogicalWallet?.let { wallet ->
                                        Text(
                                            text = "${wallet.walletType} • ${wallet.currency}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    } ?: run {
                                        Text(
                                            text = "Select logical wallet to be affected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
            
            // Source and Destination Wallet Selection for Transfer
            if (selectedType == TransactionType.TRANSFER) {
                item {
                    Text(
                        text = "From Wallet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSourceWalletPicker = true },
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
                            Column {
                                Text(
                                    text = selectedSourceWallet?.name ?: "Choose source wallet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                selectedSourceWallet?.let { wallet ->
                                    Text(
                                        text = "${wallet.walletType} • ${wallet.currency}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                }
                
                item {
                    Text(
                        text = "To Wallet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDestinationWalletPicker = true },
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
                            Column {
                                Text(
                                    text = selectedDestinationWallet?.name ?: "Choose destination wallet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                selectedDestinationWallet?.let { wallet ->
                                    Text(
                                        text = "${wallet.walletType} • ${wallet.currency}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                }
                
                // Currency and wallet type mismatch warnings
                if (selectedSourceWallet != null && selectedDestinationWallet != null) {
                    if (selectedSourceWallet?.currency != selectedDestinationWallet?.currency) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Source and destination wallets must have the same currency",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    
                    if (selectedSourceWallet?.walletType != selectedDestinationWallet?.walletType) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Source and destination wallets must have the same wallet type",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Date Selection
            item {
                Text(
                    text = "Transaction Date",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
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
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null
                        )
                    }
                }
            }
            
            // Create Button
            item {
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        
                        val tagsList = if (tags.isBlank()) listOf("Untagged") 
                                       else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        
                        when (selectedType) {
                            TransactionType.INCOME, TransactionType.EXPENSE -> {
                                if (allSelectedWallets.isNotEmpty()) {
                                    viewModel.createTransaction(
                                        title = title,
                                        amount = amountValue,
                                        category = "Untagged", // Default category, will be replaced by tags
                                        type = selectedType,
                                        affectedWalletIds = allSelectedWallets.map { it.id },
                                        tags = tagsList,
                                        transactionDate = selectedDate
                                    )
                                }
                            }
                            TransactionType.TRANSFER -> {
                                if (selectedSourceWallet != null && selectedDestinationWallet != null) {
                                    viewModel.createTransferTransaction(
                                        title = title,
                                        amount = amountValue,
                                        sourceWalletId = selectedSourceWallet!!.id,
                                        destinationWalletId = selectedDestinationWallet!!.id,
                                        transactionDate = selectedDate
                                    )
                                }
                            }
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid
                ) {
                    Text("Create Transaction")
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        TransactionDatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date: Date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Physical Wallet Picker Dialog
    if (showPhysicalWalletPicker) {
        WalletPickerDialog(
            wallets = uiState.wallets.filter { it.walletType == "Physical" },
            title = "Select Physical Wallet",
            onDismiss = { showPhysicalWalletPicker = false },
            onWalletSelected = { wallet ->
                selectedPhysicalWallet = wallet
                showPhysicalWalletPicker = false
            }
        )
    }
    
    // Logical Wallet Picker Dialog
    if (showLogicalWalletPicker) {
        WalletPickerDialog(
            wallets = uiState.wallets.filter { it.walletType == "Logical" },
            title = "Select Logical Wallet",
            onDismiss = { showLogicalWalletPicker = false },
            onWalletSelected = { wallet ->
                selectedLogicalWallet = wallet
                showLogicalWalletPicker = false
            }
        )
    }
    
    // Source Wallet Picker Dialog
    if (showSourceWalletPicker) {
        WalletPickerDialog(
            wallets = uiState.wallets,
            title = "Select Source Wallet",
            onDismiss = { showSourceWalletPicker = false },
            onWalletSelected = { wallet ->
                selectedSourceWallet = wallet
                showSourceWalletPicker = false
            }
        )
    }
    
    // Destination Wallet Picker Dialog
    if (showDestinationWalletPicker) {
        WalletPickerDialog(
            wallets = uiState.wallets.filter { it.id != selectedSourceWallet?.id },
            title = "Select Destination Wallet",
            onDismiss = { showDestinationWalletPicker = false },
            onWalletSelected = { wallet ->
                selectedDestinationWallet = wallet
                showDestinationWalletPicker = false
            }
        )
    }
}

@Composable
fun WalletPickerDialog(
    wallets: List<Wallet>,
    title: String = "Select Wallet",
    onDismiss: () -> Unit,
    onWalletSelected: (Wallet) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(wallets) { wallet ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWalletSelected(wallet) }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = wallet.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${wallet.walletType} • ${wallet.currency}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDatePickerDialog(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
                    }
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
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun WalletTypePickerDialog(
    wallets: List<Wallet>,
    selectedWallets: List<Wallet>,
    title: String,
    onDismiss: () -> Unit,
    onWalletsSelected: (List<Wallet>) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedWallets.toSet()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(wallets) { wallet ->
                    WalletSelectionItem(
                        wallet = wallet,
                        isSelected = currentSelection.contains(wallet),
                        onSelectionChanged = { isSelected ->
                            currentSelection = if (isSelected) {
                                currentSelection + wallet
                            } else {
                                currentSelection - wallet
                            }
                        }
                    )
                }
                
                if (wallets.isEmpty()) {
                    item {
                        Text(
                            text = "No wallets available for this type",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onWalletsSelected(currentSelection.toList()) }
            ) {
                Text("Select (${currentSelection.size})")
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
fun WalletSelectionItem(
    wallet: Wallet,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) }
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${wallet.walletType} • ${wallet.currency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

@Composable
fun TagInputField(
    value: String,
    onValueChange: (String) -> Unit,
    availableTags: List<String>,
    modifier: Modifier = Modifier
) {
    var showSuggestions by remember { mutableStateOf(false) }
    
    // Get current input being typed (last tag after comma)
    val currentInput = value.split(",").lastOrNull()?.trim() ?: ""
    val suggestions = if (currentInput.length >= 3) {
        availableTags.filter { it.contains(currentInput, ignoreCase = true) }.take(5)
    } else {
        emptyList()
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                showSuggestions = suggestions.isNotEmpty()
            },
            label = { Text("Tags (optional)") },
            placeholder = { Text("Add tags separated by commas") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Separate multiple tags with commas") },
            trailingIcon = {
                if (suggestions.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Show suggestions"
                    )
                }
            }
        )
        
        // Show suggestions dropdown
        if (showSuggestions && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Suggestions:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    suggestions.forEach { suggestion ->
                        TextButton(
                            onClick = {
                                // Replace the current input with the suggestion
                                val existingTags = value.split(",").dropLast(1).map { it.trim() }
                                val newValue = (existingTags + suggestion).joinToString(", ")
                                onValueChange(newValue + ", ")
                                showSuggestions = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
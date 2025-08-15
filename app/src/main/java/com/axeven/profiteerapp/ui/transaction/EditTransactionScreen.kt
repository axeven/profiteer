package com.axeven.profiteerapp.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    onNavigateBack: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Track deletion state
    var deletionRequested by remember { mutableStateOf(false) }
    var wasLoading by remember { mutableStateOf(false) }
    
    // Navigate back when deletion completes successfully
    LaunchedEffect(uiState.isLoading, uiState.error, deletionRequested) {
        if (deletionRequested) {
            if (uiState.isLoading) {
                wasLoading = true
            } else if (wasLoading && !uiState.isLoading) {
                // Loading finished after deletion request
                if (uiState.error == null) {
                    // Success - navigate back
                    onNavigateBack()
                }
                // If there's an error, stay on screen to show it
            }
        }
    }
    
    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(Math.abs(transaction.amount).toString()) }
    var tags by remember { mutableStateOf(transaction.tags.joinToString(", ")) }
    var selectedType by remember { mutableStateOf(transaction.type) }
    var selectedPhysicalWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedLogicalWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedSourceWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedDestinationWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedDate by remember { mutableStateOf(transaction.transactionDate ?: Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPhysicalWalletPicker by remember { mutableStateOf(false) }
    var showLogicalWalletPicker by remember { mutableStateOf(false) }
    var showSourceWalletPicker by remember { mutableStateOf(false) }
    var showDestinationWalletPicker by remember { mutableStateOf(false) }
    
    val allSelectedWallets = listOfNotNull(selectedPhysicalWallet, selectedLogicalWallet)
    
    // Initialize wallet selections based on transaction type
    LaunchedEffect(uiState.wallets) {
        when (transaction.type) {
            TransactionType.INCOME, TransactionType.EXPENSE -> {
                // Handle both old single wallet and new multi-wallet format
                val affectedWallets = if (transaction.affectedWalletIds.isNotEmpty()) {
                    // New format: multiple wallets
                    uiState.wallets.filter { it.id in transaction.affectedWalletIds }
                } else {
                    // Old format: single wallet
                    uiState.wallets.filter { it.id == transaction.walletId }
                }
                
                selectedPhysicalWallet = affectedWallets.find { it.walletType == "Physical" }
                selectedLogicalWallet = affectedWallets.find { it.walletType == "Logical" }
            }
            TransactionType.TRANSFER -> {
                selectedSourceWallet = uiState.wallets.find { it.id == transaction.sourceWalletId }
                selectedDestinationWallet = uiState.wallets.find { it.id == transaction.destinationWalletId }
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
                        "Edit Transaction", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.clearError() // Clear any previous errors
                            deletionRequested = true
                            viewModel.deleteTransaction(transaction.id)
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
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
            // Error display
            uiState.error?.let { error ->
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
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
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
                            onClick = { /* Type change disabled in edit mode */ },
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
                            enabled = selectedType == type, // Only the current type is enabled
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
            
            // Update Button
            item {
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        
                        val tagsList = if (tags.isBlank()) listOf("Untagged") 
                                       else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        
                        when (selectedType) {
                            TransactionType.INCOME, TransactionType.EXPENSE -> {
                                if (allSelectedWallets.isNotEmpty()) {
                                    viewModel.updateTransactionWithMultipleWallets(
                                        transactionId = transaction.id,
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
                                    viewModel.updateTransaction(
                                        transactionId = transaction.id,
                                        title = title,
                                        amount = amountValue,
                                        category = "Transfer",
                                        type = selectedType,
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
                    Text("Update Transaction")
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


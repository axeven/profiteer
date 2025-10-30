package com.axeven.profiteerapp.ui.transaction

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import com.axeven.profiteerapp.utils.WalletSortingUtils
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
    val viewModelUiState by viewModel.uiState.collectAsState()

    // Consolidated state management - replaces 16 individual mutableStateOf variables
    var editState by remember {
        mutableStateOf(
            EditTransactionUiState.fromTransaction(
                transaction = transaction,
                availableWallets = viewModelUiState.wallets
            )
        )
    }

    // Track deletion completion state
    var wasLoading by remember { mutableStateOf(false) }

    // Navigate back when deletion completes successfully
    LaunchedEffect(viewModelUiState.isLoading, viewModelUiState.error, editState.deletionRequested) {
        if (editState.deletionRequested) {
            if (viewModelUiState.isLoading) {
                wasLoading = true
            } else if (wasLoading && !viewModelUiState.isLoading) {
                // Loading finished after deletion request
                if (viewModelUiState.error == null) {
                    // Success - navigate back
                    onNavigateBack()
                }
                // If there's an error, stay on screen to show it
            }
        }
    }
    
    // Update state when wallets become available
    LaunchedEffect(viewModelUiState.wallets) {
        if (viewModelUiState.wallets.isNotEmpty()) {
            editState = EditTransactionUiState.fromTransaction(
                transaction = transaction,
                availableWallets = viewModelUiState.wallets
            )
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
                            editState = editState.requestDeletion()
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
            viewModelUiState.error?.let { error ->
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
                            selected = editState.selectedType == type,
                            enabled = editState.selectedType == type, // Only the current type is enabled
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Title Field
            item {
                OutlinedTextField(
                    value = editState.title,
                    onValueChange = { newTitle ->
                        editState = editState.updateTitle(newTitle)
                    },
                    label = { Text("Transaction Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.validationErrors.titleError != null,
                    supportingText = editState.validationErrors.titleError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }
            
            // Amount Field
            item {
                // All transactions now use the default currency
                val currencySymbol = com.axeven.profiteerapp.utils.NumberFormatter.getCurrencySymbol(viewModelUiState.defaultCurrency)

                OutlinedTextField(
                    value = editState.amount,
                    onValueChange = { newAmount ->
                        editState = editState.updateAmount(newAmount)
                    },
                    label = {
                        Text(
                            if (editState.selectedType == TransactionType.TRANSFER) "Transfer Amount"
                            else "Amount"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    isError = editState.validationErrors.amountError != null,
                    supportingText = editState.validationErrors.amountError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }
            
            // Tags Field (not for transfers)
            if (editState.selectedType != TransactionType.TRANSFER) {
                item {
                    TagInputField(
                        value = editState.tags,
                        onValueChange = { newTags ->
                            editState = editState.updateTags(newTags)
                        },
                        availableTags = viewModelUiState.availableTags,
                        modifier = Modifier.fillMaxWidth(),
                        isError = editState.validationErrors.tagsError != null,
                        supportingText = editState.validationErrors.tagsError
                    )
                }
            }
            
            // Wallet Selection for Income/Expense
            if (editState.selectedType == TransactionType.INCOME || editState.selectedType == TransactionType.EXPENSE) {
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
                            .clickable {
                                editState = editState.openDialog(DialogType.PHYSICAL_WALLET)
                            },
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
                                        text = editState.selectedWallets.physical?.name ?: "Choose physical wallet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    editState.selectedWallets.physical?.let { wallet ->
                                        Text(
                                            text = wallet.walletType,
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
                            .clickable {
                                editState = editState.openDialog(DialogType.LOGICAL_WALLET)
                            },
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
                                        text = editState.selectedWallets.logical?.name ?: "Choose logical wallet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    editState.selectedWallets.logical?.let { wallet ->
                                        Text(
                                            text = wallet.walletType,
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
            if (editState.selectedType == TransactionType.TRANSFER) {
                item {
                    Text(
                        text = "From Wallet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                            editState = editState.openDialog(DialogType.SOURCE_WALLET)
                        },
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
                                    text = editState.selectedWallets.source?.name ?: "Choose source wallet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                editState.selectedWallets.source?.let { wallet ->
                                    Text(
                                        text = wallet.walletType,
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
                            .clickable {
                            editState = editState.openDialog(DialogType.DESTINATION_WALLET)
                        },
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
                                    text = editState.selectedWallets.destination?.name ?: "Choose destination wallet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                editState.selectedWallets.destination?.let { wallet ->
                                    Text(
                                        text = wallet.walletType,
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
                
                // Wallet type mismatch warnings
                if (editState.selectedWallets.source != null && editState.selectedWallets.destination != null) {
                    if (editState.selectedWallets.source?.walletType != editState.selectedWallets.destination?.walletType) {
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
                        .clickable {
                            editState = editState.openDialog(DialogType.DATE_PICKER)
                        },
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
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(editState.selectedDate),
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
                        val summary = editState.getTransactionSummary()

                        when (editState.selectedType) {
                            TransactionType.INCOME, TransactionType.EXPENSE -> {
                                val selectedWallets = listOfNotNull(
                                    editState.selectedWallets.physical,
                                    editState.selectedWallets.logical
                                )
                                if (selectedWallets.isNotEmpty()) {
                                    viewModel.updateTransactionWithMultipleWallets(
                                        transactionId = transaction.id,
                                        title = summary.title,
                                        amount = summary.amount,
                                        category = "Untagged", // Default category, will be replaced by tags
                                        type = summary.type,
                                        affectedWalletIds = selectedWallets.map { it.id },
                                        tags = summary.tags,
                                        transactionDate = summary.date
                                    )
                                }
                            }
                            TransactionType.TRANSFER -> {
                                if (editState.selectedWallets.source != null && editState.selectedWallets.destination != null) {
                                    viewModel.updateTransaction(
                                        transactionId = transaction.id,
                                        title = summary.title,
                                        amount = summary.amount,
                                        category = "Transfer",
                                        type = summary.type,
                                        sourceWalletId = editState.selectedWallets.source!!.id,
                                        destinationWalletId = editState.selectedWallets.destination!!.id,
                                        transactionDate = summary.date
                                    )
                                }
                            }
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = editState.isFormValid && editState.hasChanges
                ) {
                    Text("Update Transaction")
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (editState.dialogStates.showDatePicker) {
        TransactionDatePickerDialog(
            selectedDate = editState.selectedDate,
            onDateSelected = { date: Date ->
                editState = editState.updateSelectedDate(date).closeAllDialogs()
            },
            onDismiss = {
                editState = editState.closeAllDialogs()
            }
        )
    }
    
    // Physical Wallet Picker Dialog
    if (editState.dialogStates.showPhysicalWalletPicker) {
        WalletPickerDialog(
            wallets = WalletSortingUtils.sortAlphabetically(
                viewModelUiState.wallets.filter { it.walletType == "Physical" }
            ),
            title = "Select Physical Wallet",
            onDismiss = {
                editState = editState.closeAllDialogs()
            },
            onWalletSelected = { wallet ->
                editState = editState.updatePhysicalWallet(wallet).closeAllDialogs()
            }
        )
    }
    
    // Logical Wallet Picker Dialog
    if (editState.dialogStates.showLogicalWalletPicker) {
        WalletPickerDialog(
            wallets = WalletSortingUtils.sortAlphabetically(
                viewModelUiState.wallets.filter { it.walletType == "Logical" }
            ),
            title = "Select Logical Wallet",
            onDismiss = {
                editState = editState.closeAllDialogs()
            },
            onWalletSelected = { wallet ->
                editState = editState.updateLogicalWallet(wallet).closeAllDialogs()
            }
        )
    }
    
    // Source Wallet Picker Dialog
    if (editState.dialogStates.showSourceWalletPicker) {
        WalletPickerDialog(
            wallets = WalletSortingUtils.sortByTypeAndName(
                viewModelUiState.wallets
            ),
            title = "Select Source Wallet",
            onDismiss = {
                editState = editState.closeAllDialogs()
            },
            onWalletSelected = { wallet ->
                editState = editState.updateSourceWallet(wallet).closeAllDialogs()
            }
        )
    }
    
    // Destination Wallet Picker Dialog
    if (editState.dialogStates.showDestinationWalletPicker) {
        WalletPickerDialog(
            wallets = WalletSortingUtils.sortByTypeAndName(
                viewModelUiState.wallets.filter { it.id != editState.selectedWallets.source?.id }
            ),
            title = "Select Destination Wallet",
            onDismiss = {
                editState = editState.closeAllDialogs()
            },
            onWalletSelected = { wallet ->
                editState = editState.updateDestinationWallet(wallet).closeAllDialogs()
            }
        )
    }
}


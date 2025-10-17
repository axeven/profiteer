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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.data.constants.UIConstants
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
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
    val viewModelUiState by viewModel.uiState.collectAsState()

    // Consolidated state management - replaces 18 individual mutableStateOf variables
    var transactionState by remember {
        mutableStateOf(
            CreateTransactionUiState(
                selectedType = initialTransactionType ?: TransactionType.EXPENSE
            )
        )
    }
    
    // Handle wallet pre-selection - NO VALIDATION on initial pre-selection
    LaunchedEffect(preSelectedWalletId, viewModelUiState.wallets) {
        if (preSelectedWalletId != null && viewModelUiState.wallets.isNotEmpty()) {
            val preSelectedWallet = viewModelUiState.wallets.find { it.id == preSelectedWalletId }
            preSelectedWallet?.let { wallet ->
                // Update state directly without validation to avoid showing errors on initial load
                val updatedWallets = when (wallet.walletType) {
                    "Physical" -> transactionState.selectedWallets.updatePhysical(wallet)
                    "Logical" -> transactionState.selectedWallets.updateLogical(wallet)
                    else -> transactionState.selectedWallets
                }

                // For transfer transactions, pre-select as source wallet
                val finalWallets = if (transactionState.selectedType == TransactionType.TRANSFER) {
                    updatedWallets.copy(source = wallet, destination = null)
                } else {
                    updatedWallets
                }

                // Update state WITHOUT validation (no updateAndValidate call)
                transactionState = transactionState.copy(selectedWallets = finalWallets)
            }
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
                            onClick = {
                                transactionState = updateTransactionType(transactionState, type)
                            },
                            label = {
                                Text(
                                    when (type) {
                                        TransactionType.INCOME -> "Income"
                                        TransactionType.EXPENSE -> "Expense"
                                        TransactionType.TRANSFER -> "Transfer"
                                    }
                                )
                            },
                            selected = transactionState.selectedType == type,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Title Field
            item {
                OutlinedTextField(
                    value = transactionState.title,
                    onValueChange = { newTitle ->
                        transactionState = updateTitle(transactionState, newTitle)
                    },
                    label = { Text("Transaction Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = transactionState.validationErrors.titleError != null,
                    supportingText = transactionState.validationErrors.titleError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }
            
            // Amount Field
            item {
                // All transactions now use the default currency
                val currencySymbol = com.axeven.profiteerapp.utils.NumberFormatter.getCurrencySymbol(viewModelUiState.defaultCurrency)

                OutlinedTextField(
                    value = transactionState.amount,
                    onValueChange = { newAmount ->
                        transactionState = updateAmount(transactionState, newAmount)
                    },
                    label = {
                        Text(
                            if (transactionState.selectedType == TransactionType.TRANSFER) "Transfer Amount"
                            else "Amount"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text(currencySymbol) },
                    isError = transactionState.validationErrors.amountError != null,
                    supportingText = transactionState.validationErrors.amountError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }
            
            // Tags Field (not for transfers)
            if (transactionState.selectedType != TransactionType.TRANSFER) {
                item {
                    TagInputField(
                        value = transactionState.tags,
                        onValueChange = { newTags ->
                            transactionState = updateTags(transactionState, newTags)
                        },
                        availableTags = viewModelUiState.availableTags,
                        modifier = Modifier.fillMaxWidth(),
                        isError = transactionState.validationErrors.tagsError != null,
                        supportingText = transactionState.validationErrors.tagsError
                    )
                }
            }
            
            // Wallet Selection for Income/Expense
            if (transactionState.selectedType == TransactionType.INCOME || transactionState.selectedType == TransactionType.EXPENSE) {
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
                                transactionState = openDialog(transactionState, DialogType.PHYSICAL_WALLET)
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
                                        text = transactionState.selectedWallets.physical?.name ?: "Choose physical wallet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    transactionState.selectedWallets.physical?.let { wallet ->
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
                                transactionState = openDialog(transactionState, DialogType.LOGICAL_WALLET)
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
                                        text = transactionState.selectedWallets.logical?.name ?: "Choose logical wallet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    transactionState.selectedWallets.logical?.let { wallet ->
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
            if (transactionState.selectedType == TransactionType.TRANSFER) {
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
                                transactionState = openDialog(transactionState, DialogType.SOURCE_WALLET)
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
                                    text = transactionState.selectedWallets.source?.name ?: "Choose source wallet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                transactionState.selectedWallets.source?.let { wallet ->
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
                                transactionState = openDialog(transactionState, DialogType.DESTINATION_WALLET)
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
                                    text = transactionState.selectedWallets.destination?.name ?: "Choose destination wallet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                transactionState.selectedWallets.destination?.let { wallet ->
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
                
                // Display transfer validation errors
                transactionState.validationErrors.transferError?.let { errorMessage ->
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
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
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
                            transactionState = openDialog(transactionState, DialogType.DATE_PICKER)
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
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transactionState.selectedDate),
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
                        val summary = getTransactionSummary(transactionState)

                        when (transactionState.selectedType) {
                            TransactionType.INCOME, TransactionType.EXPENSE -> {
                                val affectedWallets = transactionState.selectedWallets.allSelected
                                if (affectedWallets.isNotEmpty()) {
                                    viewModel.createTransaction(
                                        title = summary.title,
                                        amount = summary.amount,
                                        category = "Untagged", // Default category, will be replaced by tags
                                        type = summary.type,
                                        affectedWalletIds = affectedWallets.map { it.id },
                                        tags = if (summary.tags.isEmpty()) listOf("Untagged") else summary.tags,
                                        transactionDate = summary.date
                                    )
                                }
                            }
                            TransactionType.TRANSFER -> {
                                val sourceWallet = transactionState.selectedWallets.source
                                val destinationWallet = transactionState.selectedWallets.destination
                                if (sourceWallet != null && destinationWallet != null) {
                                    viewModel.createTransferTransaction(
                                        title = summary.title,
                                        amount = summary.amount,
                                        sourceWalletId = sourceWallet.id,
                                        destinationWalletId = destinationWallet.id,
                                        transactionDate = summary.date
                                    )
                                }
                            }
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = transactionState.isFormValid
                ) {
                    Text("Create Transaction")
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (transactionState.dialogStates.showDatePicker) {
        TransactionDatePickerDialog(
            selectedDate = transactionState.selectedDate,
            onDateSelected = { date: Date ->
                transactionState = updateSelectedDate(transactionState, date)
            },
            onDismiss = {
                transactionState = closeAllDialogs(transactionState)
            }
        )
    }
    
    // Physical Wallet Picker Dialog
    if (transactionState.dialogStates.showPhysicalWalletPicker) {
        WalletPickerDialog(
            wallets = viewModelUiState.wallets.filter { it.walletType == "Physical" },
            title = "Select Physical Wallet",
            onDismiss = {
                transactionState = closeAllDialogs(transactionState)
            },
            onWalletSelected = { wallet ->
                transactionState = updatePhysicalWallet(transactionState, wallet)
            }
        )
    }
    
    // Logical Wallet Picker Dialog
    if (transactionState.dialogStates.showLogicalWalletPicker) {
        WalletPickerDialog(
            wallets = viewModelUiState.wallets.filter { it.walletType == "Logical" },
            title = "Select Logical Wallet",
            onDismiss = {
                transactionState = closeAllDialogs(transactionState)
            },
            onWalletSelected = { wallet ->
                transactionState = updateLogicalWallet(transactionState, wallet)
            }
        )
    }
    
    // Source Wallet Picker Dialog
    if (transactionState.dialogStates.showSourceWalletPicker) {
        WalletPickerDialog(
            wallets = viewModelUiState.wallets,
            title = "Select Source Wallet",
            onDismiss = {
                transactionState = closeAllDialogs(transactionState)
            },
            onWalletSelected = { wallet ->
                transactionState = updateSourceWallet(transactionState, wallet)
            }
        )
    }
    
    // Destination Wallet Picker Dialog
    if (transactionState.dialogStates.showDestinationWalletPicker) {
        WalletPickerDialog(
            wallets = viewModelUiState.wallets.filter { it.id != transactionState.selectedWallets.source?.id },
            title = "Select Destination Wallet",
            onDismiss = {
                transactionState = closeAllDialogs(transactionState)
            },
            onWalletSelected = { wallet ->
                transactionState = updateDestinationWallet(transactionState, wallet)
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
                                text = wallet.walletType,
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
        // Enhanced layout wrapper with forced column width distribution
        EqualWidthDatePickerContainer {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // Additional layer to ensure proper rendering of equal columns
                        clip = true
                    },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    headlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun EqualWidthDatePickerContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    /**
     * ENHANCED SOLUTION: Aggressive fix for Material 3 DatePicker column width bug.
     * 
     * Issue: The Saturday (last) column in Material 3 DatePicker appears narrower 
     * than other weekday columns, causing visual imbalance and usability issues.
     * 
     * Root Cause: Material 3 DatePicker internal layout calculations create uneven 
     * column distribution, particularly affecting the last column (Saturday).
     * 
     * Solution: Multi-layered approach:
     * 1. Force minimum width for proper 7-column grid (336dp = 48dp per column)
     * 2. Apply horizontal scaling if needed to ensure equal distribution
     * 3. Use precise density calculations for pixel-perfect alignment
     * 4. Override internal layout with explicit width constraints
     */
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 16.dp) // Minimal horizontal padding
            .onSizeChanged { containerSize = it }
            // Removed clipToBounds() to allow proper DatePicker visibility
    ) {
        Layout(
            content = content,
            modifier = Modifier.fillMaxWidth()
        ) { measurables, constraints ->
            // Calculate precise width for equal 7-column distribution
            val availableWidth = constraints.maxWidth
            
            // REFINED FIX: Balance equal columns with container visibility
            // Use optimal width calculation that ensures equal columns without excessive expansion
            val baseColumnWidthDp = 50.dp // Balanced column width for proper distribution
            val optimalWidthDp = baseColumnWidthDp * 7
            val optimalWidthPx = with(density) { optimalWidthDp.toPx().toInt() }
            
            // Use optimal width, but not excessively larger than container
            val targetWidth = maxOf(availableWidth, optimalWidthPx)
            
            // BALANCED: Use moderate expansion to fix columns without causing clipping
            val expandedWidth = if (targetWidth <= availableWidth) {
                targetWidth
            } else {
                (targetWidth * 1.05f).toInt() // Minimal 5% expansion only when needed
            }
            val enforcedConstraints = Constraints.fixed(
                width = expandedWidth,
                height = constraints.maxHeight
            )
            
            // Measure DatePicker with enforced constraints
            val placeable = measurables.firstOrNull()?.measure(enforcedConstraints)
            
            // Layout dimensions
            val layoutWidth = availableWidth
            val layoutHeight = placeable?.height ?: 0
            
            // Smart centering to prevent clipping while maintaining equal columns
            val xOffset = when {
                expandedWidth <= availableWidth -> {
                    // DatePicker fits within container, center it
                    (availableWidth - expandedWidth) / 2
                }
                else -> {
                    // DatePicker is wider, center it but allow controlled overflow
                    val overflow = expandedWidth - availableWidth
                    -minOf(overflow / 2, with(density) { 4.dp.toPx().toInt() }) // Max 4dp overflow on each side
                }
            }
            
            layout(layoutWidth, layoutHeight) {
                placeable?.placeRelative(x = xOffset, y = 0)
            }
        }
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
                    text = wallet.walletType,
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
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var showSuggestions by remember { mutableStateOf(false) }
    
    // Get current input being typed (last tag after comma)
    val currentInput = value.split(",").lastOrNull()?.trim() ?: ""
    val suggestions = if (currentInput.length >= UIConstants.TAG_AUTOCOMPLETE_MIN_CHARS) {
        availableTags.filter { it.contains(currentInput, ignoreCase = true) }.take(UIConstants.TAG_SUGGESTION_LIMIT)
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
            isError = isError,
            supportingText = {
                if (supportingText != null) {
                    Text(supportingText, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Separate multiple tags with commas")
                }
            },
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
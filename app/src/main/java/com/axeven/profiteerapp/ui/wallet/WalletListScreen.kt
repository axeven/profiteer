package com.axeven.profiteerapp.ui.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.utils.NumberFormatter
import com.axeven.profiteerapp.utils.WalletValidator
import com.axeven.profiteerapp.viewmodel.WalletListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToWalletDetail: (String) -> Unit = {},
    viewModel: WalletListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showCreateWalletDialog by remember { mutableStateOf(false) }
    var showEditWalletDialog by remember { mutableStateOf(false) }
    var walletToEdit by remember { mutableStateOf<Wallet?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.showPhysicalWallets) "Physical Wallets" else "Logical Wallets", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleWalletType() }) {
                        Icon(
                            imageVector = if (uiState.showPhysicalWallets) Icons.Default.AccountBox else Icons.Default.AccountCircle,
                            contentDescription = "Switch wallet type"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateWalletDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create wallet")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Toggle buttons for wallet type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { 
                        if (!uiState.showPhysicalWallets) {
                            viewModel.toggleWalletType()
                        }
                    },
                    label = { Text("Physical") },
                    selected = uiState.showPhysicalWallets,
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountBox,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                FilterChip(
                    onClick = { 
                        if (uiState.showPhysicalWallets) {
                            viewModel.toggleWalletType()
                        }
                    },
                    label = { Text("Logical") },
                    selected = !uiState.showPhysicalWallets,
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Group toggle for physical wallets
                if (uiState.showPhysicalWallets) {
                    GroupingToggleButton(
                        isGrouped = uiState.isGroupedByForm,
                        onToggle = { viewModel.toggleGroupByForm() }
                    )
                }
            }
            
            // Physical form filters for physical wallets
            if (uiState.showPhysicalWallets && uiState.availablePhysicalForms.isNotEmpty()) {
                PhysicalFormFilterChips(
                    availablePhysicalForms = uiState.availablePhysicalForms,
                    selectedPhysicalForms = uiState.selectedPhysicalForms,
                    onFormToggled = { viewModel.togglePhysicalFormFilter(it) },
                    onClearFilters = { viewModel.clearPhysicalFormFilters() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Show grouped or flat view based on UI state
                if (uiState.showPhysicalWallets && uiState.isGroupedByForm && uiState.groupedWallets.isNotEmpty()) {
                    // Grouped view for physical wallets
                    GroupedWalletList(
                        groupedWallets = uiState.groupedWallets,
                        defaultCurrency = uiState.defaultCurrency,
                        conversionRates = emptyMap(), // No longer needed
                        onWalletClick = onNavigateToWalletDetail,
                        onWalletEdit = { wallet ->
                            walletToEdit = wallet
                            showEditWalletDialog = true
                        },
                        onWalletDelete = { viewModel.deleteWallet(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Flat view for all cases
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Show unallocated balance when viewing logical wallets
                        if (!uiState.showPhysicalWallets) {
                            item {
                                UnallocatedBalanceCard(
                                    unallocatedBalance = uiState.unallocatedBalance,
                                    defaultCurrency = uiState.defaultCurrency
                                )
                            }
                        }
                        
                        if (uiState.wallets.isEmpty()) {
                            item {
                                EmptyWalletState(
                                    walletType = if (uiState.showPhysicalWallets) "physical" else "logical",
                                    onCreateWallet = { showCreateWalletDialog = true }
                                )
                            }
                        } else {
                            items(uiState.wallets) { wallet ->
                                WalletListItem(
                                    wallet = wallet,
                                    defaultCurrency = uiState.defaultCurrency,
                                    displayCurrency = uiState.displayCurrency,
                                    displayRate = uiState.displayRate,
                                    onEdit = { 
                                        walletToEdit = wallet
                                        showEditWalletDialog = true
                                    },
                                    onDelete = { viewModel.deleteWallet(wallet.id) },
                                    onClick = { onNavigateToWalletDetail(wallet.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateWalletDialog) {
        CreateWalletDialog(
            onDismiss = { showCreateWalletDialog = false },
            onConfirm = { name, walletType, _, initialBalance, physicalForm ->
                viewModel.createWallet(name, walletType, initialBalance, physicalForm)
                showCreateWalletDialog = false
            },
            defaultCurrency = uiState.defaultCurrency,
            defaultWalletType = if (uiState.showPhysicalWallets) "Physical" else "Logical",
            isWalletNameUnique = { name -> viewModel.isWalletNameUnique(name) }
        )
    }
    
    if (showEditWalletDialog && walletToEdit != null) {
        EditWalletDialog(
            wallet = walletToEdit!!,
            onDismiss = { 
                showEditWalletDialog = false
                walletToEdit = null
            },
            onConfirm = { name, walletType, _, initialBalance, physicalForm ->
                val updatedWallet = walletToEdit!!.copy(
                    name = name,
                    walletType = walletType,
                    initialBalance = initialBalance,
                    physicalForm = physicalForm,
                    balance = walletToEdit!!.balance - walletToEdit!!.initialBalance + initialBalance
                )
                viewModel.updateWallet(updatedWallet)
                showEditWalletDialog = false
                walletToEdit = null
            },
            defaultCurrency = uiState.defaultCurrency,
            isWalletNameUnique = { name -> viewModel.isWalletNameUnique(name, walletToEdit!!.id) }
        )
    }
}

@Composable
fun WalletListItem(
    wallet: Wallet,
    defaultCurrency: String,
    displayCurrency: String,
    displayRate: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Physical form icon for Physical wallets
                        if (wallet.walletType == "Physical") {
                            Text(
                                text = wallet.physicalForm.icon,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Text(
                            text = wallet.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (wallet.walletType == "Physical") Icons.Default.AccountBox else Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = wallet.walletType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        // Show physical form for Physical wallets
                        if (wallet.walletType == "Physical") {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = wallet.physicalForm.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = defaultCurrency,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Balance display
            Column {
                Text(
                    text = "Balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                // All wallets now use default currency - display with display currency conversion if needed
                val displayBalance = if (defaultCurrency != displayCurrency) {
                    wallet.balance * displayRate
                } else {
                    wallet.balance
                }
                
                // Use displayCurrency for symbol display to match the converted balance
                val balanceText = NumberFormatter.formatCurrency(displayBalance, displayCurrency, showSymbol = true)
                
                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (wallet.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyWalletState(
    walletType: String,
    onCreateWallet: () -> Unit
) {
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
                imageVector = if (walletType == "physical") Icons.Default.AccountBox else Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Text(
                text = "No $walletType wallets yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Create your first $walletType wallet to start managing your finances",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            
            Button(
                onClick = onCreateWallet,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Wallet")
            }
        }
    }
}

@Composable
fun CreateWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, PhysicalForm) -> Unit,
    defaultCurrency: String = "USD",
    defaultWalletType: String = "Physical",
    isWalletNameUnique: (String) -> Boolean = { true }
) {
    var walletName by remember { mutableStateOf("") }
    var selectedWalletType by remember { mutableStateOf(defaultWalletType) }
    var selectedPhysicalForm by remember { mutableStateOf(PhysicalForm.FIAT_CURRENCY) }
    var initialBalanceText by remember { mutableStateOf("0.00") }
    var showWalletTypeDropdown by remember { mutableStateOf(false) }
    val walletTypes = listOf("Physical", "Logical")
    
    val isNameUnique = isWalletNameUnique(walletName)
    val initialBalance = NumberFormatter.parseDouble(initialBalanceText) ?: 0.0
    
    // Validate form
    val validationResult = WalletValidator.validateWalletData(
        walletName, selectedPhysicalForm, initialBalance
    )
    val isFormValid = validationResult.isValid && isNameUnique

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
                    isError = !isNameUnique && walletName.isNotBlank(),
                    supportingText = if (!isNameUnique && walletName.isNotBlank()) {
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
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showWalletTypeDropdown = true }
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
                
                // Currency selection removed - using single global currency
                Text(
                    text = "Currency: $defaultCurrency",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // Physical Form Selection (only for Physical wallets)
                if (selectedWalletType == "Physical") {
                    PhysicalFormSelector(
                        selectedForm = selectedPhysicalForm,
                        onFormSelected = { selectedPhysicalForm = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                val balanceLabel = when (defaultCurrency) {
                    "GOLD" -> "Initial Weight (grams)"
                    "BTC" -> "Initial Amount (BTC)"
                    else -> "Initial Balance"
                }
                val balancePlaceholder = when (defaultCurrency) {
                    "GOLD" -> "0.000"
                    "BTC" -> "0.00000000"
                    else -> "0.00"
                }
                
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text(balanceLabel) },
                    placeholder = { Text(balancePlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = NumberFormatter.parseDouble(initialBalanceText) == null && initialBalanceText.isNotBlank(),
                    supportingText = if (NumberFormatter.parseDouble(initialBalanceText) == null && initialBalanceText.isNotBlank()) {
                        { Text("Please enter a valid amount", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(walletName, selectedWalletType, defaultCurrency, initialBalance, selectedPhysicalForm) },
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
fun EditWalletDialog(
    wallet: Wallet,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, PhysicalForm) -> Unit,
    defaultCurrency: String = "USD",
    isWalletNameUnique: (String) -> Boolean = { true }
) {
    var walletName by remember { mutableStateOf(wallet.name) }
    var selectedWalletType by remember { mutableStateOf(wallet.walletType) }
    var selectedPhysicalForm by remember { mutableStateOf(wallet.physicalForm) }
    var initialBalanceText by remember { mutableStateOf(NumberFormatter.formatCurrency(wallet.initialBalance)) }
    var showWalletTypeDropdown by remember { mutableStateOf(false) }
    val walletTypes = listOf("Physical", "Logical")
    
    val isNameUnique = isWalletNameUnique(walletName)
    val initialBalance = NumberFormatter.parseDouble(initialBalanceText) ?: 0.0
    
    // Validate form
    val validationResult = WalletValidator.validateWalletData(
        walletName, selectedPhysicalForm, initialBalance
    )
    val isFormValid = validationResult.isValid && isNameUnique

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
                    isError = !isNameUnique && walletName.isNotBlank(),
                    supportingText = if (!isNameUnique && walletName.isNotBlank()) {
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
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showWalletTypeDropdown = true }
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
                
                // Currency selection removed - using single global currency
                Text(
                    text = "Currency: $defaultCurrency",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // Physical Form Selection (only for Physical wallets)
                if (selectedWalletType == "Physical") {
                    PhysicalFormSelector(
                        selectedForm = selectedPhysicalForm,
                        onFormSelected = { selectedPhysicalForm = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                val balanceLabel = when (defaultCurrency) {
                    "GOLD" -> "Initial Weight (grams)"
                    "BTC" -> "Initial Amount (BTC)"
                    else -> "Initial Balance"
                }
                val balancePlaceholder = when (defaultCurrency) {
                    "GOLD" -> "0.000"
                    "BTC" -> "0.00000000"
                    else -> "0.00"
                }
                
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text(balanceLabel) },
                    placeholder = { Text(balancePlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = NumberFormatter.parseDouble(initialBalanceText) == null && initialBalanceText.isNotBlank(),
                    supportingText = if (NumberFormatter.parseDouble(initialBalanceText) == null && initialBalanceText.isNotBlank()) {
                        { Text("Please enter a valid amount", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(walletName, selectedWalletType, defaultCurrency, initialBalance, selectedPhysicalForm) },
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
fun UnallocatedBalanceCard(
    unallocatedBalance: Double,
    defaultCurrency: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unallocatedBalance >= 0) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (unallocatedBalance >= 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Text(
                    text = "Unallocated Physical Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (unallocatedBalance >= 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = NumberFormatter.formatCurrency(unallocatedBalance, defaultCurrency, showSymbol = true),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (unallocatedBalance >= 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            if (unallocatedBalance < 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You have over-allocated your logical wallets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            } else if (unallocatedBalance > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available for allocation to logical wallets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All physical balance is allocated",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

package com.axeven.profiteerapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.WalletFilter
import com.axeven.profiteerapp.utils.WalletSortingUtils

/**
 * Dialog for selecting a wallet filter (All Wallets or a specific wallet).
 *
 * This dialog allows users to filter report data by:
 * - All Wallets (no filtering)
 * - Specific wallet
 *
 * Wallets are displayed alphabetically using WalletSortingUtils.sortAlphabetically
 * and shown with their currency code (e.g., "Cash Wallet (USD)").
 *
 * @param currentFilter The currently selected wallet filter
 * @param wallets List of available wallets
 * @param defaultCurrency The default currency to display with wallet names
 * @param onFilterSelected Callback when user selects a filter
 * @param onDismiss Callback when dialog is dismissed without selection
 */
@Composable
fun WalletFilterPickerDialog(
    currentFilter: WalletFilter,
    wallets: List<Wallet>,
    defaultCurrency: String,
    onFilterSelected: (WalletFilter) -> Unit,
    onDismiss: () -> Unit
) {
    // Sort wallets alphabetically
    val sortedWallets = remember(wallets) {
        WalletSortingUtils.sortAlphabetically(wallets)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select Wallet")
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // All Wallets option
                item {
                    WalletFilterOption(
                        filter = WalletFilter.AllWallets,
                        displayText = "All Wallets",
                        isSelected = currentFilter == WalletFilter.AllWallets,
                        onClick = { onFilterSelected(WalletFilter.AllWallets) }
                    )
                }

                // Divider
                if (sortedWallets.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // Wallet options
                items(sortedWallets) { wallet ->
                    val walletFilter = WalletFilter.SpecificWallet(wallet.id, wallet.name)
                    val displayText = "${wallet.name} ($defaultCurrency)"
                    val isSelected = currentFilter is WalletFilter.SpecificWallet &&
                            currentFilter.walletId == wallet.id

                    WalletFilterOption(
                        filter = walletFilter,
                        displayText = displayText,
                        isSelected = isSelected,
                        onClick = { onFilterSelected(walletFilter) }
                    )
                }
            }
        },
        confirmButton = {
            // No confirm button needed - selection happens immediately
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Individual wallet filter option in the picker dialog.
 */
@Composable
private fun WalletFilterOption(
    filter: WalletFilter,
    displayText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            RadioButton(
                selected = true,
                onClick = onClick
            )
        }
    }
}

package com.axeven.profiteerapp.ui.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axeven.profiteerapp.data.model.WalletFilter

/**
 * FilterChip component for displaying and selecting the current wallet filter.
 *
 * This chip shows the currently selected wallet filter (e.g., "All Wallets", "Cash Wallet")
 * and opens the WalletFilterPickerDialog when clicked.
 *
 * The chip uses a wallet icon and follows Material 3 design patterns, matching the
 * style of the existing ReportFilterChip.
 *
 * @param currentFilter The currently selected wallet filter
 * @param onClick Callback when chip is clicked
 * @param modifier Optional modifier for the chip
 */
@Composable
fun WalletFilterChip(
    currentFilter: WalletFilter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = currentFilter != WalletFilter.AllWallets,
        onClick = onClick,
        label = {
            Text(text = currentFilter.getDisplayText())
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Filter by wallet",
                modifier = Modifier
            )
        },
        modifier = modifier
    )
}

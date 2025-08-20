package com.axeven.profiteerapp.ui.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.utils.WalletValidator

/**
 * A composable that provides a user-friendly interface for selecting physical forms for wallets.
 * Includes validation, filtering by currency compatibility, and visual indicators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalFormSelector(
    selectedForm: PhysicalForm,
    onFormSelected: (PhysicalForm) -> Unit,
    currency: String = "",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showCompatibleOnly: Boolean = true,
    label: String = "Physical Form"
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Filter forms based on currency compatibility if requested
    val availableForms = if (showCompatibleOnly && currency.isNotBlank()) {
        WalletValidator.getSuggestedPhysicalForms(currency)
    } else {
        PhysicalForm.values().toList()
    }
    
    val isCurrentFormCompatible = if (currency.isNotBlank()) {
        selectedForm.isCurrencyAllowed(currency)
    } else {
        true
    }
    
    Column(modifier = modifier) {
        // Main selector button
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it && enabled }
        ) {
            OutlinedTextField(
                value = selectedForm.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text(label) },
                leadingIcon = {
                    Text(
                        text = selectedForm.icon,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = enabled,
                isError = !isCurrentFormCompatible,
                supportingText = if (!isCurrentFormCompatible) {
                    {
                        Text(
                            text = "Not compatible with $currency",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    {
                        Text(
                            text = selectedForm.description,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Group forms by category for better UX
                PhysicalFormCategory.values().forEach { category ->
                    val categoryForms = availableForms.filter { form ->
                        category.forms.contains(form)
                    }
                    
                    if (categoryForms.isNotEmpty()) {
                        // Category header
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        categoryForms.forEach { form ->
                            PhysicalFormDropdownItem(
                                form = form,
                                isSelected = form == selectedForm,
                                currency = currency,
                                onClick = {
                                    onFormSelected(form)
                                    expanded = false
                                }
                            )
                        }
                        
                        if (category != PhysicalFormCategory.values().last()) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual dropdown item for physical form selection.
 */
@Composable
private fun PhysicalFormDropdownItem(
    form: PhysicalForm,
    isSelected: Boolean,
    currency: String,
    onClick: () -> Unit
) {
    val isCompatible = currency.isBlank() || form.isCurrencyAllowed(currency)
    
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = form.icon,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = form.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCompatible) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )
                    
                    Text(
                        text = form.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompatible) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        }
                    )
                    
                    // Show currency restriction if applicable
                    if (!isCompatible && currency.isNotBlank()) {
                        Text(
                            text = "Not compatible with $currency",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        onClick = if (isCompatible) onClick else { {} },
        enabled = isCompatible
    )
}

/**
 * Dialog version of the physical form selector for more detailed selection.
 */
@Composable
fun PhysicalFormSelectorDialog(
    selectedForm: PhysicalForm,
    onFormSelected: (PhysicalForm) -> Unit,
    onDismiss: () -> Unit,
    currency: String = "",
    showCompatibleOnly: Boolean = true,
    title: String = "Select Physical Form"
) {
    val availableForms = if (showCompatibleOnly && currency.isNotBlank()) {
        WalletValidator.getSuggestedPhysicalForms(currency)
    } else {
        PhysicalForm.values().toList()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Group by categories
                PhysicalFormCategory.values().forEach { category ->
                    val categoryForms = availableForms.filter { form ->
                        category.forms.contains(form)
                    }
                    
                    if (categoryForms.isNotEmpty()) {
                        item {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(categoryForms) { form ->
                            PhysicalFormDialogItem(
                                form = form,
                                isSelected = form == selectedForm,
                                currency = currency,
                                onClick = {
                                    onFormSelected(form)
                                    onDismiss()
                                }
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

/**
 * Individual dialog item for physical form selection with enhanced details.
 */
@Composable
private fun PhysicalFormDialogItem(
    form: PhysicalForm,
    isSelected: Boolean,
    currency: String,
    onClick: () -> Unit
) {
    val isCompatible = currency.isBlank() || form.isCurrencyAllowed(currency)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isCompatible) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCompatible -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = form.icon,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = form.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        isCompatible -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
                
                Text(
                    text = form.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        isCompatible -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
                
                // Show compatible currencies
                form.allowedCurrencies?.let { currencies ->
                    if (currencies.size <= 5) { // Only show if list is manageable
                        Text(
                            text = "Supports: ${currencies.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                isCompatible -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        )
                    }
                }
                
                if (!isCompatible && currency.isNotBlank()) {
                    Text(
                        text = "Not compatible with $currency",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Categories for organizing physical forms in the UI.
 */
private enum class PhysicalFormCategory(
    val displayName: String,
    val forms: Set<PhysicalForm>
) {
    CASH(
        displayName = "Cash & Equivalents",
        forms = PhysicalForm.CASH_FORMS
    ),
    INVESTMENTS(
        displayName = "Traditional Investments", 
        forms = PhysicalForm.INVESTMENT_FORMS
    ),
    DIGITAL_ASSETS(
        displayName = "Digital Assets",
        forms = PhysicalForm.DIGITAL_ASSETS
    ),
    ALTERNATIVE(
        displayName = "Alternative Investments",
        forms = PhysicalForm.ALTERNATIVE_INVESTMENTS
    ),
    OTHER(
        displayName = "Other",
        forms = setOf(PhysicalForm.OTHER)
    )
}
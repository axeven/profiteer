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

/**
 * A composable that provides a user-friendly interface for selecting physical forms for wallets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalFormSelector(
    selectedForm: PhysicalForm,
    onFormSelected: (PhysicalForm) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = "Physical Form"
) {
    var expanded by remember { mutableStateOf(false) }
    
    // All physical forms are now available since we removed currency restrictions
    val availableForms = PhysicalForm.values().toList()
    
    Column(modifier = modifier) {
        // Main selector button
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded && enabled }
        ) {
            OutlinedTextField(
                value = selectedForm.displayName,
                onValueChange = { },
                readOnly = true,
                enabled = enabled,
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                },
                supportingText = {
                    Text(
                        text = selectedForm.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableForms.forEach { form ->
                    PhysicalFormDropdownItem(
                        form = form,
                        isSelected = form == selectedForm,
                        onClick = {
                            onFormSelected(form)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * A dropdown menu item for physical form selection.
 */
@Composable
private fun PhysicalFormDropdownItem(
    form: PhysicalForm,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = form.icon,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = form.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        text = form.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        onClick = onClick
    )
}

/**
 * A full-screen dialog for physical form selection with detailed information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalFormSelectorDialog(
    selectedForm: PhysicalForm,
    onFormSelected: (PhysicalForm) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // All physical forms are available
    val availableForms = PhysicalForm.values().toList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Physical Form") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableForms) { form ->
                    PhysicalFormDialogItem(
                        form = form,
                        isSelected = form == selectedForm,
                        onClick = {
                            onFormSelected(form)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

/**
 * A dialog item for physical form selection with detailed information.
 */
@Composable
private fun PhysicalFormDialogItem(
    form: PhysicalForm,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = form.icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = form.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Text(
                    text = form.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}
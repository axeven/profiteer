package com.axeven.profiteerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(
    message: String,
    shouldRetry: Boolean = false,
    requiresReauth: Boolean = false,
    isOffline: Boolean = false,
    onRetry: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when {
        requiresReauth -> Icons.Default.AccountCircle
        isOffline -> Icons.Default.AccountBox
        shouldRetry -> Icons.Default.Refresh
        else -> Icons.Default.Info
    }

    val containerColor = when {
        requiresReauth -> MaterialTheme.colorScheme.tertiaryContainer
        isOffline -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when {
        requiresReauth -> MaterialTheme.colorScheme.onTertiaryContainer
        isOffline -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    requiresReauth -> {
                        Button(
                            onClick = onSignIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign In")
                        }
                    }
                    shouldRetry -> {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@Composable
fun ErrorSnackbar(
    message: String,
    shouldRetry: Boolean = false,
    requiresReauth: Boolean = false,
    onRetry: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val actionText = when {
        requiresReauth -> "Sign In"
        shouldRetry -> "Retry"
        else -> null
    }

    val action = when {
        requiresReauth -> onSignIn
        shouldRetry -> onRetry
        else -> null
    }

    if (actionText != null && action != null) {
        LaunchedEffect(message) {
            // Show snackbar with action
        }
    }
}
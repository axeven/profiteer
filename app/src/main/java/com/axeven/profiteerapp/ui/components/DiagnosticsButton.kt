package com.axeven.profiteerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.axeven.profiteerapp.viewmodel.DiagnosticsViewModel
import com.axeven.profiteerapp.viewmodel.DiagnosticsUiState
import com.axeven.profiteerapp.utils.CredentialDiagnostics

@Composable
fun DiagnosticsButton(
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = {
            showDialog = true
            viewModel.runDiagnostics()
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Run Diagnostics"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Debug Credentials")
    }

    if (showDialog) {
        DiagnosticsDialog(
            uiState = uiState,
            onDismiss = {
                showDialog = false
                viewModel.clearReport()
            }
        )
    }
}

@Composable
private fun DiagnosticsDialog(
    uiState: DiagnosticsUiState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Credential Diagnostics",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    uiState.isRunning -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Running diagnostics...")
                    }

                    uiState.error != null -> {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    uiState.report != null -> {
                        DiagnosticsReport(uiState.report)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsReport(report: CredentialDiagnostics.DiagnosticReport) {
    Column {
        Text(
            text = "Status: ${report.overallStatus}",
            style = MaterialTheme.typography.titleMedium,
            color = when {
                report.overallStatus.contains("HEALTHY") -> MaterialTheme.colorScheme.primary
                report.overallStatus.contains("WARNING") -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Credential Status:",
            style = MaterialTheme.typography.titleSmall
        )

        report.credentialStatuses.forEach { status ->
            Row(
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val emoji = when (status.status) {
                    CredentialDiagnostics.CredentialStatus.Status.VALID -> "✓"
                    CredentialDiagnostics.CredentialStatus.Status.EXPIRED -> "⚠️"
                    CredentialDiagnostics.CredentialStatus.Status.ERROR -> "❌"
                    CredentialDiagnostics.CredentialStatus.Status.UNKNOWN -> "❓"
                }

                Text(
                    text = "$emoji ${status.name}: ${status.details}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (report.recommendations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Recommendations:",
                style = MaterialTheme.typography.titleSmall
            )

            report.recommendations.forEach { recommendation ->
                Text(
                    text = "• $recommendation",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 1.dp, bottom = 1.dp)
                )
            }
        }
    }
}
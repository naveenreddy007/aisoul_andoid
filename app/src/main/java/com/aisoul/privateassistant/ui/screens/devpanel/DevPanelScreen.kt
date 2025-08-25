package com.aisoul.privateassistant.ui.screens.devpanel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aisoul.privateassistant.ui.theme.AISoulTheme

data class DevAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: () -> Unit
)

data class SystemInfo(
    val label: String,
    val value: String
)

@Composable
fun DevPanelScreen() {
    var showDemoDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    val systemInfo = remember {
        listOf(
            SystemInfo("App Version", "1.0.0-dev"),
            SystemInfo("Build Type", "Debug"),
            SystemInfo("AI Engine", "TensorFlow Lite"),
            SystemInfo("Database", "Room + SQLCipher"),
            SystemInfo("Demo Mode", "Active"),
            SystemInfo("Installed Models", "0"),
            SystemInfo("Total Conversations", "0"),
            SystemInfo("Database Size", "Empty")
        )
    }

    val devActions = remember {
        listOf(
            DevAction(
                title = "Demo Mode",
                description = "Toggle demo responses for testing",
                icon = Icons.Filled.PlayArrow,
                action = { showDemoDialog = true }
            ),
            DevAction(
                title = "Clear All Data",
                description = "Delete all conversations and models",
                icon = Icons.Filled.DeleteForever,
                action = { showClearDataDialog = true }
            ),
            DevAction(
                title = "Database Stats",
                description = "View detailed database information",
                icon = Icons.Filled.Storage,
                action = { /* TODO */ }
            ),
            DevAction(
                title = "Export Logs",
                description = "Export debug logs for troubleshooting",
                icon = Icons.Filled.BugReport,
                action = { /* TODO */ }
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Developer Panel",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.BugReport,
                        contentDescription = "Debug",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Debug Build",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "This panel is only available in debug builds",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "System Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    systemInfo.forEach { info ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = info.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = info.value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Development Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(devActions) { action ->
            DevActionCard(action = action)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Performance Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Model inference time: N/A\n" +
                                "• Memory usage: Normal\n" +
                                "• Battery impact: Minimal\n" +
                                "• Storage usage: < 1MB\n" +
                                "• Background processing: Disabled",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Demo Dialog
    if (showDemoDialog) {
        AlertDialog(
            onDismissRequest = { showDemoDialog = false },
            title = { Text("Demo Mode") },
            text = { Text("Demo mode is currently active. All AI responses are simulated.") },
            confirmButton = {
                TextButton(onClick = { showDemoDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("This will permanently delete all conversations, models, and user data. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Implement data clearing
                        showClearDataDialog = false
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevActionCard(action: DevAction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = action.action
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DevPanelScreenPreview() {
    AISoulTheme {
        DevPanelScreen()
    }
}
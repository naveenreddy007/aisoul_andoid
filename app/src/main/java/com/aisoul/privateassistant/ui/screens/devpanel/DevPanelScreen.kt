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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aisoul.privateassistant.ui.theme.AISoulTheme
import com.aisoul.privateassistant.core.demo.DemoModeManager

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
    val context = LocalContext.current
    val demoModeManager = remember { DemoModeManager.getInstance(context) }
    
    var showDemoDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var isDemoModeEnabled by remember { mutableStateOf(demoModeManager.isDemoModeEnabled) }

    val systemInfo = remember(isDemoModeEnabled) {
        listOf(
            SystemInfo("App Version", "1.0.0-dev"),
            SystemInfo("Build Type", "Debug"),
            SystemInfo("AI Engine", if (isDemoModeEnabled) "Demo Mode" else "TensorFlow Lite"),
            SystemInfo("Database", "Room + SQLCipher"),
            SystemInfo("Demo Mode", if (isDemoModeEnabled) "Active" else "Disabled"),
            SystemInfo("Installed Models", "0"),
            SystemInfo("Total Conversations", "0"),
            SystemInfo("Database Size", "Empty")
        )
    }

    val devActions = remember {
        listOf(
            DevAction(
                title = "Toggle Demo Mode",
                description = "Switch between demo responses and real AI",
                icon = Icons.Filled.PlayArrow,
                action = { 
                    demoModeManager.isDemoModeEnabled = !demoModeManager.isDemoModeEnabled
                    isDemoModeEnabled = demoModeManager.isDemoModeEnabled
                }
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
                    containerColor = if (isDemoModeEnabled) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.BugReport,
                        contentDescription = "Debug",
                        tint = if (isDemoModeEnabled) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isDemoModeEnabled) "Demo Mode Active" else "Debug Build - Demo Disabled",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDemoModeEnabled) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                        Text(
                            text = if (isDemoModeEnabled) {
                                "AI responses are simulated for testing"
                            } else {
                                "Real AI processing enabled"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDemoModeEnabled) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
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
                        text = "• Model inference time: ${if (isDemoModeEnabled) "Demo (800-2000ms simulated)" else "N/A"}\n" +
                                "• Memory usage: Normal\n" +
                                "• Battery impact: Minimal\n" +
                                "• Storage usage: < 1MB\n" +
                                "• Background processing: ${if (isDemoModeEnabled) "Simulated" else "Disabled"}",
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
            text = { Text("Demo mode is currently ${if (isDemoModeEnabled) "active" else "disabled"}. ${if (isDemoModeEnabled) "All AI responses are simulated." else "Real AI processing is enabled."}") },
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
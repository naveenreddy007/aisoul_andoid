package com.aisoul.privateassistant.ui.screens.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aisoul.privateassistant.ui.theme.AISoulTheme
import com.aisoul.privateassistant.services.NotificationListenerService

data class PrivacyStatus(
    val title: String,
    val description: String,
    val isActive: Boolean,
    val icon: ImageVector
)

data class Permission(
    val name: String,
    val description: String,
    val isGranted: Boolean,
    val isRequired: Boolean,
    val action: (() -> Unit)? = null
)

@Composable
fun PrivacyScreen() {
    val context = LocalContext.current
    var notificationAccessGranted by remember { 
        mutableStateOf<Boolean>(com.aisoul.privateassistant.services.NotificationListenerService.isNotificationAccessGranted(context))
    }
    
    val privacyStatuses = remember {
        listOf(
            PrivacyStatus(
                title = "Local Processing",
                description = "All AI processing happens on your device. No data is sent to external servers.",
                isActive = true,
                icon = Icons.Filled.Security
            ),
            PrivacyStatus(
                title = "Encrypted Storage",
                description = "Your conversations and data are encrypted using AES-256 encryption.",
                isActive = true,
                icon = Icons.Filled.Lock
            ),
            PrivacyStatus(
                title = "No Analytics",
                description = "We don't collect usage analytics or track your behavior.",
                isActive = true,
                icon = Icons.Filled.Check
            )
        )
    }

    val permissions = remember(notificationAccessGranted) {
        listOf(
            Permission(
                name = "Notification Access",
                description = "Required to analyze and respond to notifications",
                isGranted = notificationAccessGranted,
                isRequired = true,
                action = {
                    com.aisoul.privateassistant.services.NotificationListenerService.requestNotificationAccess(context)
                }
            ),
            Permission(
                name = "SMS Access",
                description = "Optional: Analyze SMS messages for context",
                isGranted = false,
                isRequired = false
            ),
            Permission(
                name = "Phone Access",
                description = "Optional: Handle call-related queries",
                isGranted = false,
                isRequired = false
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
                text = "Privacy & Security",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Text(
                text = "Your privacy is our priority. AI Soul processes everything locally on your device.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Text(
                text = "Privacy Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(privacyStatuses) { status ->
            PrivacyStatusCard(status = status)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(permissions) { permission ->
            PermissionCard(
                permission = permission,
                onToggle = { permissionName ->
                    when (permissionName) {
                        "Notification Access" -> {
                            permission.action?.invoke()
                            // Update status after a delay to check if user granted permission
                            // In a real app, you'd use a lifecycle observer
                        }
                        else -> {
                            // Handle other permissions
                            println("Toggle permission: $permissionName")
                        }
                    }
                }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Data Protection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• All AI models run locally on your device\n" +
                                "• Conversations are stored with AES-256 encryption\n" +
                                "• No data is transmitted to external servers\n" +
                                "• You can delete all data at any time\n" +
                                "• Open source for full transparency",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyStatusCard(status: PrivacyStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (status.isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = status.icon,
                contentDescription = status.title,
                tint = if (status.isActive) Color.Green else Color.Red,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (status.isActive) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Active",
                    tint = Color.Green
                )
            } else {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "Inactive",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    permission: Permission,
    onToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = permission.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (permission.isRequired) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Required",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (permission.action != null) {
                Button(
                    onClick = { onToggle(permission.name) },
                    enabled = !permission.isGranted
                ) {
                    Text(if (permission.isGranted) "Granted" else "Grant")
                }
            } else {
                Switch(
                    checked = permission.isGranted,
                    onCheckedChange = { onToggle(permission.name) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyScreenPreview() {
    AISoulTheme {
        PrivacyScreen()
    }
}
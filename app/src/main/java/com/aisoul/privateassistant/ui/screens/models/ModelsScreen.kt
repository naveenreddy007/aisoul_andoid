package com.aisoul.privateassistant.ui.screens.models

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aisoul.privateassistant.ui.theme.AISoulTheme

data class AIModel(
    val id: String,
    val name: String,
    val description: String,
    val sizeGB: Float,
    val minRamGB: Int,
    val minStorageGB: Int,
    val isDownloaded: Boolean = false,
    val isCompatible: Boolean = true,
    val downloadProgress: Float? = null
)

@Composable
fun ModelsScreen() {
    val deviceRamGB = 6 // Mock device specs - in real app this would be detected
    val deviceStorageGB = 32
    
    val availableModels = remember {
        listOf(
            AIModel(
                id = "gemma-2b",
                name = "Gemma 2B",
                description = "Lightweight model for basic conversations. Fast and efficient.",
                sizeGB = 1.4f,
                minRamGB = 3,
                minStorageGB = 2,
                isCompatible = deviceRamGB >= 3 && deviceStorageGB >= 2
            ),
            AIModel(
                id = "gemma-7b",
                name = "Gemma 7B",
                description = "Advanced model with better reasoning. Requires more resources.",
                sizeGB = 4.1f,
                minRamGB = 6,
                minStorageGB = 8,
                isCompatible = deviceRamGB >= 6 && deviceStorageGB >= 8
            ),
            AIModel(
                id = "phi-3-mini",
                name = "Phi-3 Mini",
                description = "Microsoft's efficient 3.8B parameter model optimized for mobile.",
                sizeGB = 2.2f,
                minRamGB = 4,
                minStorageGB = 4,
                isCompatible = deviceRamGB >= 4 && deviceStorageGB >= 4
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "AI Models",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Device info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Device Compatibility",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Available RAM: ${deviceRamGB}GB")
                        Text("Available Storage: ${deviceStorageGB}GB")
                    }
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Compatible",
                        tint = Color.Green
                    )
                }
            }
        }

        // Models list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableModels) { model ->
                ModelCard(
                    model = model,
                    onDownload = { modelId ->
                        // TODO: Implement model download
                        println("Download requested for model: $modelId")
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelCard(
    model: AIModel,
    onDownload: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (model.isCompatible) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = model.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (!model.isCompatible) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Incompatible",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Model specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Size: ${model.sizeGB}GB",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "Min RAM: ${model.minRamGB}GB",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Column {
                    Text(
                        text = "Min Storage: ${model.minStorageGB}GB",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Download progress or button
            if (model.downloadProgress != null) {
                LinearProgressIndicator(
                    progress = model.downloadProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Downloading... ${(model.downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (model.isDownloaded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Downloaded",
                        tint = Color.Green
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Downloaded",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Green
                    )
                }
            } else {
                Button(
                    onClick = { onDownload(model.id) },
                    enabled = model.isCompatible,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "Download")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (model.isCompatible) "Download" else "Incompatible")
                }
            }

            if (!model.isCompatible) {
                Text(
                    text = "This model requires more RAM or storage than available on your device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModelsScreenPreview() {
    AISoulTheme {
        ModelsScreen()
    }
}
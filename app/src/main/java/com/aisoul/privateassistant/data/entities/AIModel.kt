package com.aisoul.privateassistant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_models")
data class AIModel(
    @PrimaryKey
    val id: String, // e.g., "gemma-2b", "phi-3-mini"
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val minRamMB: Int,
    val minStorageMB: Int,
    val isDownloaded: Boolean = false,
    val downloadPath: String? = null,
    val downloadedAt: Long? = null,
    val checksumSha256: String? = null,
    val version: String = "1.0",
    val isActive: Boolean = false // Currently selected model
)
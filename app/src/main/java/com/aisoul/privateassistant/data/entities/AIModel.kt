package com.aisoul.privateassistant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI Model entity for MediaPipe LLM models
 * Supports .bin format models for MediaPipe LLM Inference API
 */
@Entity(tableName = "ai_models")
data class AIModel(
    @PrimaryKey
    val id: String, // e.g., "gemma-2b-it", "phi-2", "gemma-7b-it"
    val name: String, // Display name for the model
    val description: String, // Model description and capabilities
    val sizeBytes: Long, // Model file size in bytes
    val minRamMB: Int, // Minimum RAM requirement for the model
    val minStorageMB: Int, // Minimum storage requirement
    val isDownloaded: Boolean = false, // Whether model is downloaded locally
    val downloadPath: String? = null, // Local file path to the .bin model
    val downloadedAt: Long? = null, // Timestamp when model was downloaded
    val checksumSha256: String? = null, // SHA-256 checksum for integrity verification
    val version: String = "1.0", // Model version
    val isActive: Boolean = false, // Currently selected model for inference
    val modelFormat: String = "mediapipe_bin", // Model format: "mediapipe_bin" for MediaPipe .bin files
    val architecture: String? = null, // Model architecture: "gemma", "phi", "llama", etc.
    val quantization: String? = null, // Quantization type: "fp16", "int8", "int4", etc.
    val contextLength: Int = 2048, // Maximum context length supported by the model
    val isMediaPipeCompatible: Boolean = true // Compatibility flag for MediaPipe LLM Inference API
)
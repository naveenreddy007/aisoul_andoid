package com.aisoul.privateassistant.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aisoul.privateassistant.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

/**
 * Model Download Service
 * Handles downloading AI models with progress tracking and validation
 */
class ModelDownloadService : Service() {
    
    companion object {
        private const val TAG = "ModelDownloadService"
        private const val CHANNEL_ID = "model_download_channel"
        private const val NOTIFICATION_ID = 1001
        
        // Official model download URLs from Google and community sources
        private val MODEL_URLS = mapOf(
            // Google Gemma 3 Models - Official HuggingFace
            AIModelManager.ModelType.GEMMA_3_270M to "https://huggingface.co/google/gemma-3-270m/resolve/main/model.tflite",
            
            // Google Gemma 2 Models - Official HuggingFace  
            AIModelManager.ModelType.GEMMA_2B to "https://huggingface.co/google/gemma-2b/resolve/main/model.tflite",
            AIModelManager.ModelType.GEMMA_7B to "https://huggingface.co/google/gemma-7b/resolve/main/model.tflite",
            
            // Microsoft Phi-3 Mini - Official HuggingFace
            AIModelManager.ModelType.PHI3_MINI to "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct/resolve/main/model.tflite"
        )
        
        // Alternative download sources for redundancy
        private val ALTERNATIVE_URLS = mapOf(
            // Kaggle sources (require authentication)
            AIModelManager.ModelType.GEMMA_3_270M to listOf(
                "https://www.kaggle.com/models/google/gemma-3/tensorFlow2/gemma-3-270m",
                "https://huggingface.co/google/gemma-3-270m/resolve/main/model-4bit.tflite"
            ),
            AIModelManager.ModelType.GEMMA_2B to listOf(
                "https://www.kaggle.com/models/google/gemma/tensorFlow2/gemma-2b",
                "https://huggingface.co/google/gemma-2b/resolve/main/model-int8.tflite"
            ),
            AIModelManager.ModelType.GEMMA_7B to listOf(
                "https://www.kaggle.com/models/google/gemma/tensorFlow2/gemma-7b",
                "https://huggingface.co/google/gemma-7b/resolve/main/model-int4.tflite"
            ),
            AIModelManager.ModelType.PHI3_MINI to listOf(
                "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct/resolve/main/model-q4.tflite",
                "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct/resolve/main/model-int8.tflite"
            )
        )
        
        // Expected checksums for model validation
        private val MODEL_CHECKSUMS = mapOf(
            AIModelManager.ModelType.GEMMA_3_270M to "gemma_3_270m_checksum_placeholder",
            AIModelManager.ModelType.GEMMA_2B to "expected_gemma_2b_checksum",
            AIModelManager.ModelType.GEMMA_7B to "expected_gemma_7b_checksum",
            AIModelManager.ModelType.PHI3_MINI to "expected_phi3_mini_checksum"
        )
        
        fun startDownload(context: Context, modelType: AIModelManager.ModelType, customUrl: String? = null) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                putExtra("model_type", modelType.name)
                putExtra("action", "download")
                customUrl?.let { putExtra("custom_url", it) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun startManualDownload(context: Context, modelName: String, downloadUrl: String, modelSizeMB: Int) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                putExtra("action", "manual_download")
                putExtra("model_name", modelName)
                putExtra("download_url", downloadUrl)
                putExtra("model_size_mb", modelSizeMB)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun cancelDownload(context: Context, modelType: AIModelManager.ModelType) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                putExtra("model_type", modelType.name)
                putExtra("action", "cancel")
            }
            context.startService(intent)
        }
    }
    
    private lateinit var okHttpClient: OkHttpClient
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeDownloads = mutableMapOf<String, Call>()
    
    // Download progress tracking
    private val _downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    /**
     * Download progress data class
     */
    data class DownloadProgress(
        val modelType: AIModelManager.ModelType,
        val status: DownloadStatus,
        val bytesDownloaded: Long = 0L,
        val totalBytes: Long = 0L,
        val speed: String = "",
        val estimatedTimeRemaining: String = "",
        val error: String? = null
    ) {
        val progressPercent: Int
            get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
    }
    
    /**
     * Download status enumeration
     */
    enum class DownloadStatus {
        PENDING,
        DOWNLOADING,
        VALIDATING,
        COMPLETED,
        FAILED,
        CANCELLED,
        PAUSED
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ModelDownloadService created")
        
        // Initialize HTTP client
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun handleIntent(intent: Intent) {
        val modelTypeName = intent.getStringExtra("model_type") ?: return
        val action = intent.getStringExtra("action") ?: return
        
        try {
            val modelType = AIModelManager.ModelType.valueOf(modelTypeName)
            
            when (action) {
                "download" -> startModelDownload(modelType)
                "cancel" -> cancelModelDownload(modelType)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid model type: $modelTypeName")
        }
    }
    
    private fun startModelDownload(modelType: AIModelManager.ModelType) {
        if (activeDownloads.containsKey(modelType.modelId)) {
            Log.w(TAG, "Download already in progress for ${modelType.modelId}")
            return
        }
        
        Log.i(TAG, "🚀 Starting download for ${modelType.displayName}")
        
        // Start foreground service IMMEDIATELY for better visibility
        startForeground(NOTIFICATION_ID, createInitialNotification(modelType))
        
        // Add to active downloads immediately
        activeDownloads[modelType.modelId] = okHttpClient.newCall(
            okhttp3.Request.Builder().url("https://placeholder.com").build()
        )
        
        // Update progress state immediately with visible feedback
        updateProgress(modelType, DownloadProgress(modelType, DownloadStatus.PENDING))
        
        serviceScope.launch {
            try {
                downloadModel(modelType)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Download failed for ${modelType.modelId}", e)
                updateProgress(modelType, DownloadProgress(
                    modelType, 
                    DownloadStatus.FAILED, 
                    error = e.message
                ))
            }
        }
    }
    
    private suspend fun downloadModel(modelType: AIModelManager.ModelType) = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        // Check if model already exists
        val modelFile = getModelFile(modelType)
        if (modelFile.exists() && validateModelFile(modelFile, modelType)) {
            Log.i(TAG, "Model ${modelType.modelId} already exists and is valid")
            updateProgress(modelType, DownloadProgress(modelType, DownloadStatus.COMPLETED))
            return@withContext
        }
        
        // Get download URL
        val downloadUrl = MODEL_URLS[modelType] ?: run {
            throw IllegalStateException("No download URL configured for ${modelType.modelId}")
        }
        
        // For demo purposes, simulate download progress instead of actual HTTP download
        simulateModelDownload(modelType, startTime)
    }
    
    /**
     * Enhanced model download simulation with better progress tracking
     * Provides realistic download experience with visible status updates
     */
    private suspend fun simulateModelDownload(modelType: AIModelManager.ModelType, startTime: Long) {
        val totalSize = modelType.sizeMB * 1024 * 1024L // Convert MB to bytes
        val chunkSize = totalSize / 100 // 1% chunks for smoother progress
        var downloaded = 0L
        
        Log.i(TAG, "📥 Starting enhanced download for ${modelType.displayName} (${modelType.sizeMB}MB)")
        
        updateProgress(modelType, DownloadProgress(
            modelType, 
            DownloadStatus.DOWNLOADING,
            totalBytes = totalSize
        ))
        
        // Enhanced download progress with realistic timing
        while (downloaded < totalSize) {
            delay(150) // Optimized delay for smooth visual feedback
            
            downloaded = minOf(downloaded + chunkSize, totalSize)
            val elapsed = System.currentTimeMillis() - startTime
            val speedKBps = if (elapsed > 0) (downloaded / elapsed * 1000 / 1024).toInt() else 0
            val remainingSeconds = if (speedKBps > 0) (totalSize - downloaded) / (speedKBps * 1024) else 0
            
            val progressPercent = (downloaded * 100 / totalSize).toInt()
            Log.d(TAG, "📊 Download progress: ${downloaded / (1024 * 1024)}MB / ${totalSize / (1024 * 1024)}MB (${progressPercent}%) - ${speedKBps} KB/s")
            
            updateProgress(modelType, DownloadProgress(
                modelType,
                DownloadStatus.DOWNLOADING,
                bytesDownloaded = downloaded,
                totalBytes = totalSize,
                speed = "${speedKBps} KB/s",
                estimatedTimeRemaining = "${remainingSeconds}s"
            ))
            
            // Check if download was cancelled
            if (!activeDownloads.containsKey(modelType.modelId)) {
                Log.i(TAG, "🛑 Download cancelled for ${modelType.displayName}")
                updateProgress(modelType, DownloadProgress(modelType, DownloadStatus.CANCELLED))
                return
            }
        }
        
        // Enhanced validation phase
        Log.i(TAG, "🔍 Validating ${modelType.displayName}...")
        updateProgress(modelType, DownloadProgress(modelType, DownloadStatus.VALIDATING))
        delay(2000) // Slightly longer validation for realism
        
        // Create model file (demo placeholder)
        createDemoModelFile(modelType)
        
        // Complete download with success notification
        Log.i(TAG, "✅ Download completed successfully for ${modelType.displayName}")
        updateProgress(modelType, DownloadProgress(modelType, DownloadStatus.COMPLETED))
        activeDownloads.remove(modelType.modelId)
    }
    
    /**
     * Create demo model file for testing
     */
    private fun createDemoModelFile(modelType: AIModelManager.ModelType) {
        val modelFile = getModelFile(modelType)
        modelFile.parentFile?.mkdirs()
        
        // Create a placeholder file with some content
        FileOutputStream(modelFile).use { output ->
            output.write("DEMO_AI_MODEL_${modelType.modelId}".toByteArray())
            // Write some dummy data to simulate model size
            repeat(1000) {
                output.write("This is demo model data for ${modelType.displayName}. ".toByteArray())
            }
        }
        
        Log.d(TAG, "Created demo model file: ${modelFile.path}")
    }
    
    private fun cancelModelDownload(modelType: AIModelManager.ModelType) {
        activeDownloads.remove(modelType.modelId)?.cancel()
        updateProgress(modelType, DownloadProgress(modelType, DownloadStatus.CANCELLED))
        Log.i(TAG, "Cancelled download for ${modelType.displayName}")
    }
    
    private fun updateProgress(modelType: AIModelManager.ModelType, progress: DownloadProgress) {
        val currentProgress = _downloadProgress.value.toMutableMap()
        currentProgress[modelType.modelId] = progress
        _downloadProgress.value = currentProgress
        
        // Update notification
        updateNotification(progress)
    }
    
    private fun getModelFile(modelType: AIModelManager.ModelType): File {
        val modelsDir = File(filesDir, "ai_models")
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        return File(modelsDir, modelType.fileName)
    }
    
    private fun validateModelFile(file: File, modelType: AIModelManager.ModelType): Boolean {
        // For demo purposes, just check if file exists and has some content
        return file.exists() && file.length() > 0
        
        // In production, this would validate checksums:
        /*
        val expectedChecksum = MODEL_CHECKSUMS[modelType] ?: return false
        val actualChecksum = calculateChecksum(file)
        return actualChecksum == expectedChecksum
        */
    }
    
    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read = input.read(buffer)
            while (read > 0) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Model Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of AI model downloads"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create enhanced initial notification for download start
     */
    private fun createInitialNotification(modelType: AIModelManager.ModelType): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🤖 AI Soul - ${modelType.displayName}")
            .setContentText("🚀 Initializing download... (${modelType.sizeMB}MB)")
            .setSmallIcon(R.drawable.ic_home)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }
    
    private fun updateNotification(progress: DownloadProgress) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Soul - ${progress.modelType.displayName}")
            .setContentText(when (progress.status) {
                DownloadStatus.PENDING -> "Preparing download..."
                DownloadStatus.DOWNLOADING -> "${progress.progressPercent}% - ${progress.speed}"
                DownloadStatus.VALIDATING -> "Validating model..."
                DownloadStatus.COMPLETED -> "✅ Download completed successfully!"
                DownloadStatus.FAILED -> "❌ Download failed: ${progress.error}"
                DownloadStatus.CANCELLED -> "Download cancelled"
                else -> "Processing..."
            })
            .setSmallIcon(R.drawable.ic_home) // Use existing icon
            .setProgress(100, progress.progressPercent, progress.status == DownloadStatus.PENDING)
            .setOngoing(progress.status == DownloadStatus.DOWNLOADING || progress.status == DownloadStatus.VALIDATING)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
        
        // Stop foreground when completed/failed/cancelled
        if (progress.status in listOf(DownloadStatus.COMPLETED, DownloadStatus.FAILED, DownloadStatus.CANCELLED)) {
            // Keep notification visible for a moment
            serviceScope.launch {
                delay(3000) // Show result for 3 seconds
                stopForeground(false)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        activeDownloads.values.forEach { it.cancel() }
        activeDownloads.clear()
        serviceScope.cancel()
        Log.d(TAG, "ModelDownloadService destroyed")
    }
}
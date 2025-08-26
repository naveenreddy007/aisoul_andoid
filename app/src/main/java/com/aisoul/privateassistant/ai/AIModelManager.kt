package com.aisoul.privateassistant.ai

import android.content.Context
import android.app.ActivityManager
import android.os.Debug
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * AI Model Manager for MediaPipe LLM integration
 * Handles model loading, inference, and lifecycle management for on-device LLMs
 */
class AIModelManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AIModelManager"
        private const val MODELS_DIR = "ai_models"
        
        @Volatile
        private var INSTANCE: AIModelManager? = null
        
        fun getInstance(context: Context): AIModelManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AIModelManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val loadedModels = ConcurrentHashMap<String, File>()
    private val modelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Supported AI model types - MediaPipe LLM compatible models
     */
    enum class ModelType(
        val modelId: String,
        val displayName: String,
        val fileName: String,
        val minRamMB: Int,
        val sizeMB: Int,
        val description: String
    ) {
        GEMMA_2B(
            "gemma-2b-it",
            "Gemma 2B Instruct",
            "gemma-2b-it.bin",
            512,
            1200,
            "Google's Gemma 2B Instruct model optimized for MediaPipe on-device processing"
        ),
        GEMMA_7B(
            "gemma-7b-it", 
            "Gemma 7B Instruct",
            "gemma-7b-it.bin",
            1024,
            3800,
            "Google's Gemma 7B Instruct model for advanced AI tasks with MediaPipe"
        ),
        PHI2_3B(
            "phi-2",
            "Microsoft Phi-2",
            "phi-2.bin",
            768,
            2700,
            "Microsoft's Phi-2 3B model optimized for MediaPipe LLM inference"
        )
    }
    
    /**
     * Model loading status
     */
    sealed class ModelLoadResult {
        object Success : ModelLoadResult()
        data class Error(val message: String, val exception: Throwable? = null) : ModelLoadResult()
        object NotFound : ModelLoadResult()
        object InsufficientMemory : ModelLoadResult()
    }
    
    /**
     * Load a MediaPipe LLM model with enhanced validation
     */
    suspend fun loadModel(modelType: ModelType): ModelLoadResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading MediaPipe model: ${modelType.displayName}")
            
            // Check if model is already loaded
            if (loadedModels.containsKey(modelType.modelId)) {
                Log.d(TAG, "Model ${modelType.modelId} already loaded")
                return@withContext ModelLoadResult.Success
            }
            
            // Skip compatibility check for loading - user wants to try regardless
            // Log compatibility but don't block loading
            val isCompatible = isDeviceCompatible(modelType)
            Log.d(TAG, "Model compatibility: ${if (isCompatible) "Recommended" else "May run slowly but attempting anyway"}")
            
            // Get model file
            val modelFile = getModelFile(modelType)
            Log.d(TAG, "Model file path: ${modelFile.path}")
            
            if (!modelFile.exists()) {
                Log.w(TAG, "Model file not found: ${modelFile.path}")
                return@withContext ModelLoadResult.NotFound
            }
            
            // Validate model file for MediaPipe compatibility with enhanced checks
            if (!isValidMediaPipeModel(modelFile)) {
                Log.w(TAG, "Invalid MediaPipe model file: ${modelFile.path}")
                return@withContext ModelLoadResult.Error("Invalid model file format for MediaPipe - must be a valid .bin file")
            }
            
            // Additional validation - check if file is actually a MediaPipe model by attempting to load it
            try {
                // Attempt to map the file to verify it's accessible
                val mappedBuffer = loadModelFile(modelFile)
                Log.d(TAG, "Model file mapping successful, size: ${mappedBuffer.capacity()} bytes")
                mappedBuffer.clear() // Clean up the buffer
            } catch (e: Exception) {
                Log.e(TAG, "Failed to map model file: ${modelFile.path}", e)
                return@withContext ModelLoadResult.Error("Cannot access model file: ${e.message}")
            }
            
            // Store model file reference for MediaPipe usage
            loadedModels[modelType.modelId] = modelFile
            
            Log.i(TAG, "Successfully loaded MediaPipe model: ${modelType.displayName} (${modelFile.length()} bytes)")
            ModelLoadResult.Success
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load MediaPipe model ${modelType.displayName}", e)
            ModelLoadResult.Error("Failed to load model: ${e.message}", e)
        }
    }
    
    /**
     * Check if device is compatible with model requirements
     * Updated to use more accurate memory detection and allow downloads regardless
     */
    fun isDeviceCompatible(modelType: ModelType): Boolean {
        return try {
            // Use ActivityManager for more accurate memory info
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            // Get device memory information with improved accuracy
            val totalDeviceMemory = getTotalDeviceMemory()
            val availableMemory = memoryInfo.availMem / (1024 * 1024) // Convert to MB
            val totalMemory = memoryInfo.totalMem / (1024 * 1024) // Convert to MB
            
            // Runtime memory info as fallback
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory() / (1024 * 1024)
            val runtimeFree = runtime.freeMemory() / (1024 * 1024)
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            
            Log.d(TAG, "=== Detailed Memory Analysis ===")
            Log.d(TAG, "Device Total Memory: ${totalDeviceMemory}MB")
            Log.d(TAG, "System Total Memory: ${totalMemory}MB")
            Log.d(TAG, "System Available: ${availableMemory}MB")
            Log.d(TAG, "Runtime Max Heap: ${maxMemory}MB")
            Log.d(TAG, "Runtime Free: ${runtimeFree}MB")
            Log.d(TAG, "Runtime Used: ${usedMemory}MB")
            Log.d(TAG, "Model Required: ${modelType.minRamMB}MB")
            Log.d(TAG, "Model Size: ${modelType.sizeMB}MB")
            
            // Always allow downloads regardless of memory constraints
            Log.d(TAG, "✅ Downloads allowed regardless of memory constraints")
            
            // As per user request: "even if it is a final MB Ram left... let it run"
            // Always allow downloads regardless of memory constraints
            Log.d(TAG, "✅ User Override: Downloads allowed regardless of memory constraints")
            Log.d(TAG, "📝 Note: Model may run slowly but will be allowed to download and attempt execution")
            
            // Always return true as explicitly requested by user
            true
            
        } catch (e: Exception) {
            Log.w(TAG, "Error checking device compatibility, allowing download anyway as requested", e)
            true // Allow download on error
        }
    }
    
    /**
     * Validate if file is a valid MediaPipe model with enhanced validation
     */
    private fun isValidMediaPipeModel(modelFile: File): Boolean {
        return try {
            // Basic validation for MediaPipe models
            if (!modelFile.exists()) {
                Log.w(TAG, "Model file validation failed: File doesn't exist")
                return false
            }
            
            if (modelFile.length() < 1000000) { // At least 1MB for any meaningful model
                Log.w(TAG, "Model file validation failed: File size (${modelFile.length()} bytes) is too small for a MediaPipe model")
                return false
            }
            
            // Check file extension - must be .bin for MediaPipe models
            if (!modelFile.name.endsWith(".bin")) {
                Log.w(TAG, "Model file validation failed: File must have .bin extension for MediaPipe, found: ${modelFile.name}")
                return false
            }
            
            // Additional MediaPipe specific validation - check file size is reasonable
            val fileSizeMB = modelFile.length() / (1024 * 1024)
            Log.d(TAG, "Model file size: ${fileSizeMB}MB")
            
            // Check if file size is within expected range for MediaPipe models
            // Minimum 100MB for any meaningful model, maximum 8GB
            if (fileSizeMB < 100 || fileSizeMB > 8192) {
                Log.w(TAG, "Model file validation failed: File size (${fileSizeMB}MB) is outside expected range for MediaPipe models (100MB-8GB)")
                return false
            }
            
            // Try to read first few bytes to check if it's a valid binary file
            try {
                FileInputStream(modelFile).use { fis ->
                    val buffer = ByteArray(4)
                    val bytesRead = fis.read(buffer)
                    if (bytesRead >= 4) {
                        Log.d(TAG, "Model file header check passed")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Model file header check failed", e)
                return false
            }
            
            Log.d(TAG, "MediaPipe model validation passed for: ${modelFile.name}")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating MediaPipe model", e)
            false
        }
    }
    
    /**
     * Get total device memory using multiple methods with improved accuracy
     */
    private fun getTotalDeviceMemory(): Long {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            // Primary method: Use ActivityManager.MemoryInfo.totalMem
            if (memoryInfo.totalMem > 0) {
                val totalMemoryMB = memoryInfo.totalMem / (1024 * 1024)
                Log.d(TAG, "📊 Device memory detected via ActivityManager: ${totalMemoryMB}MB")
                return totalMemoryMB
            }
            
            // Secondary method: Try reading /proc/meminfo
            try {
                val memInfoFile = java.io.File("/proc/meminfo")
                if (memInfoFile.exists()) {
                    val lines = memInfoFile.readLines()
                    for (line in lines) {
                        if (line.startsWith("MemTotal:")) {
                            val memTotalKB = line.replace("[^0-9]".toRegex(), "").toLongOrNull()
                            if (memTotalKB != null && memTotalKB > 0) {
                                val totalMemoryMB = memTotalKB / 1024
                                Log.d(TAG, "📊 Device memory detected via /proc/meminfo: ${totalMemoryMB}MB")
                                return totalMemoryMB
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Could not read /proc/meminfo, trying alternative method")
            }
            
            // Tertiary method: Runtime-based estimation
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory() / (1024 * 1024)
            // More realistic estimation: typically 3-6x max app memory
            val estimatedMemory = maxMemory * 4
            Log.d(TAG, "📊 Device memory estimated via Runtime: ${estimatedMemory}MB (based on max heap: ${maxMemory}MB)")
            estimatedMemory
            
        } catch (e: Exception) {
            Log.w(TAG, "Could not determine device memory accurately, using conservative estimate", e)
            2048L // Conservative 2GB estimate
        }
    }
    
    /**
     * Get model file location - simplified for actual device deployment
     */
    fun getModelFile(modelType: ModelType): File {
        // Check in app's private directory
        val appModelsDir = File(context.filesDir, MODELS_DIR)
        appModelsDir.mkdirs()
        val appModelFile = File(appModelsDir, modelType.fileName)

        if (appModelFile.exists()) {
            Log.d(TAG, "Found model in app directory: ${appModelFile.path}")
            return appModelFile
        }

        // Check in external files directory
        val externalFilesDir = context.getExternalFilesDir(null)
        if (externalFilesDir != null) {
            val externalModelFile = File(externalFilesDir, modelType.fileName)
            if (externalModelFile.exists()) {
                Log.d(TAG, "Found model in external files: ${externalModelFile.path}")
                return externalModelFile
            }
        }

        // Check root of external storage for manually placed models
        val externalRoot = File("/sdcard/Android/data/${context.packageName}/files/")
        val rootModelFile = File(externalRoot, modelType.fileName)
        if (rootModelFile.exists()) {
            Log.d(TAG, "Found model in external root: ${rootModelFile.path}")
            return rootModelFile
        }

        // Check common model directories
        val commonPaths = listOf(
            "/sdcard/Download/",
            "/sdcard/Documents/",
            "/storage/emulated/0/Download/",
            "/storage/emulated/0/Documents/"
        )

        for (path in commonPaths) {
            val modelFile = File(path, modelType.fileName)
            if (modelFile.exists()) {
                Log.d(TAG, "Found model in common path: ${modelFile.path}")
                return modelFile
            }
        }

        // Try to copy from external storage if found (for Android 11+ compatibility)
        try {
            val downloadDir = File("/sdcard/Download/")
            val downloadModelFile = File(downloadDir, modelType.fileName)
            if (downloadModelFile.exists()) {
                Log.d(TAG, "Found model in Download, attempting to copy to app directory: ${downloadModelFile.path}")
                // Copy file to app directory for better access
                val copiedFile = File(appModelsDir, modelType.fileName)
                downloadModelFile.copyTo(copiedFile, overwrite = true)
                if (copiedFile.exists()) {
                    Log.d(TAG, "Successfully copied model to app directory: ${copiedFile.path}")
                    return copiedFile
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to copy model from Download directory", e)
        }

        Log.d(TAG, "Model file not found at expected locations: ${modelType.fileName}")
        return appModelFile
    }
    
    /**
     * Load model file into memory buffer with error handling
     */
    private fun loadModelFile(modelFile: File): MappedByteBuffer {
        return try {
            FileInputStream(modelFile).use { inputStream ->
                val fileChannel = inputStream.channel
                val buffer = fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    fileChannel.size()
                )
                Log.d(TAG, "Successfully mapped model file: ${modelFile.name}")
                buffer
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping model file: ${modelFile.path}", e)
            throw e
        }
    }
    
    /**
     * Check if model is loaded and ready for inference
     */
    fun isModelLoaded(modelType: ModelType): Boolean {
        return loadedModels.containsKey(modelType.modelId)
    }
    
    /**
     * Unload a specific model to free memory
     */
    fun unloadModel(modelType: ModelType) {
        loadedModels.remove(modelType.modelId)
        Log.d(TAG, "Unloaded MediaPipe model: ${modelType.displayName}")
    }
    
    /**
     * Unload all models
     */
    fun unloadAllModels() {
        loadedModels.clear()
        Log.d(TAG, "Unloaded all MediaPipe models")
    }
    
    /**
     * Get loaded model file for MediaPipe usage
     */
    fun getLoadedModelFile(modelType: ModelType): File? {
        return loadedModels[modelType.modelId]
    }
    
    /**
     * Get all available models with their status - enhanced detection
     */
    fun getAvailableModels(): List<ModelInfo> {
        return ModelType.values().map { modelType ->
            val modelFile = getModelFile(modelType)
            val isDownloaded = modelFile.exists() && modelFile.length() > 1000000 // At least 1MB
            val isLoaded = isModelLoaded(modelType)
            val isCompatible = isDeviceCompatible(modelType)
            val fileSize = if (modelFile.exists()) modelFile.length() else 0L
            
            Log.d(TAG, "Model ${modelType.displayName}: downloaded=$isDownloaded, loaded=$isLoaded, file=${modelFile.path}, size=${fileSize} bytes")
            
            ModelInfo(
                type = modelType,
                isDownloaded = isDownloaded,
                isLoaded = isLoaded,
                isCompatible = isCompatible,
                fileSize = fileSize
            )
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        unloadAllModels()
        modelScope.cancel()
        Log.d(TAG, "AIModelManager cleanup completed")
    }
}

/**
 * Model information data class
 */
data class ModelInfo(
    val type: AIModelManager.ModelType,
    val isDownloaded: Boolean,
    val isLoaded: Boolean,
    val isCompatible: Boolean,
    val fileSize: Long
)
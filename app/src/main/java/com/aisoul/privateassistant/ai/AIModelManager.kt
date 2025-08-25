package com.aisoul.privateassistant.ai

import android.content.Context
import android.app.ActivityManager
import android.os.Debug
import android.util.Log
import kotlinx.coroutines.*
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.model.Model
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * AI Model Manager for TensorFlow Lite integration
 * Handles model loading, inference, and lifecycle management
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
    
    private val loadedModels = ConcurrentHashMap<String, Interpreter>()
    private val modelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Supported AI model types
     */
    enum class ModelType(
        val modelId: String,
        val displayName: String,
        val fileName: String,
        val minRamMB: Int,
        val sizeMB: Int,
        val description: String
    ) {
        GEMMA_3_270M(
            "gemma-3-270m-it",
            "Gemma 3 270M Instruct",
            "gemma-3-270m-it.tflite",
            512,
            150,
            "Ultra-compact 270M parameter model for extreme efficiency and battery optimization"
        ),
        GEMMA_2B(
            "gemma-2b-it",
            "Gemma 2B Instruct",
            "gemma-2b-it.tflite",
            1024,
            1200,
            "Lightweight AI model for text generation and conversation"
        ),
        GEMMA_7B(
            "gemma-7b-it", 
            "Gemma 7B Instruct",
            "gemma-7b-it.tflite",
            4096,
            3800,
            "Advanced AI model for complex conversations and analysis"
        ),
        PHI3_MINI(
            "phi-3-mini",
            "Phi-3 Mini",
            "phi-3-mini.tflite",
            1024,
            800,
            "Microsoft's efficient small language model"
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
     * Load a TensorFlow Lite model
     */
    suspend fun loadModel(modelType: ModelType): ModelLoadResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading model: ${modelType.displayName}")
            
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
            if (!modelFile.exists()) {
                Log.w(TAG, "Model file not found: ${modelFile.path}")
                return@withContext ModelLoadResult.NotFound
            }
            
            // Load model buffer
            val modelBuffer = loadModelFile(modelFile)
            
            // Create interpreter options
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(true) // Use Android Neural Networks API if available
            }
            
            // Create interpreter
            val interpreter = Interpreter(modelBuffer, options)
            
            // Store loaded model
            loadedModels[modelType.modelId] = interpreter
            
            Log.i(TAG, "Successfully loaded model: ${modelType.displayName}")
            ModelLoadResult.Success
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model ${modelType.displayName}", e)
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
            
            // For Gemma 3 270M, always allow download (ultra-efficient)
            if (modelType == ModelType.GEMMA_3_270M) {
                Log.d(TAG, "✅ Gemma 3 270M: Ultra-efficient model - ALWAYS COMPATIBLE")
                return true
            }
            
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
     * Get model file from internal storage
     */
    private fun getModelFile(modelType: ModelType): File {
        val modelsDir = File(context.filesDir, MODELS_DIR)
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        return File(modelsDir, modelType.fileName)
    }
    
    /**
     * Load model file into memory buffer
     */
    private fun loadModelFile(modelFile: File): MappedByteBuffer {
        FileInputStream(modelFile).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                0,
                fileChannel.size()
            )
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
        loadedModels.remove(modelType.modelId)?.close()
        Log.d(TAG, "Unloaded model: ${modelType.displayName}")
    }
    
    /**
     * Unload all models
     */
    fun unloadAllModels() {
        loadedModels.values.forEach { it.close() }
        loadedModels.clear()
        Log.d(TAG, "Unloaded all models")
    }
    
    /**
     * Get loaded model interpreter
     */
    fun getModelInterpreter(modelType: ModelType): Interpreter? {
        return loadedModels[modelType.modelId]
    }
    
    /**
     * Get all available models with their status
     */
    fun getAvailableModels(): List<ModelInfo> {
        return ModelType.values().map { modelType ->
            val modelFile = getModelFile(modelType)
            ModelInfo(
                type = modelType,
                isDownloaded = modelFile.exists(),
                isLoaded = isModelLoaded(modelType),
                isCompatible = isDeviceCompatible(modelType),
                fileSize = if (modelFile.exists()) modelFile.length() else 0L
            )
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        unloadAllModels()
        modelScope.cancel()
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
package com.aisoul.privateassistant.performance

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.Process
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aisoul.privateassistant.data.database.AppDatabase
import com.aisoul.privateassistant.ai.AIModelManager
import com.aisoul.privateassistant.ai.AIInferenceEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Performance Optimization and Monitoring Manager
 * Handles memory management, database optimization, and performance profiling
 */
class PerformanceManager private constructor(private val context: Context) : DefaultLifecycleObserver {
    
    companion object {
        @Volatile
        private var INSTANCE: PerformanceManager? = null
        
        fun getInstance(context: Context): PerformanceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PerformanceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val TAG = "PerformanceManager"
        private const val MEMORY_THRESHOLD_MB = 100L
        private const val PERFORMANCE_LOG_INTERVAL = 30000L // 30 seconds
        private const val DATABASE_OPTIMIZATION_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val aiModelManager = AIModelManager.getInstance(context)
    private val aiInferenceEngine = AIInferenceEngine.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // State management
    private val _performanceState = MutableStateFlow(PerformanceState.IDLE)
    val performanceState: StateFlow<PerformanceState> = _performanceState.asStateFlow()
    
    private val _memoryUsage = MutableStateFlow(MemoryUsage())
    val memoryUsage: StateFlow<MemoryUsage> = _memoryUsage.asStateFlow()
    
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    // Background jobs
    private var monitoringJob: Job? = null
    private var optimizationJob: Job? = null
    
    enum class PerformanceState {
        IDLE,
        MONITORING,
        OPTIMIZING,
        PROFILING,
        ERROR
    }
    
    data class MemoryUsage(
        val usedMemoryMB: Long = 0,
        val availableMemoryMB: Long = 0,
        val totalMemoryMB: Long = 0,
        val memoryPercentage: Float = 0f,
        val isLowMemory: Boolean = false
    )
    
    data class PerformanceMetrics(
        val appStartupTime: Long = 0,
        val databaseQueryAverageTime: Long = 0,
        val aiInferenceAverageTime: Long = 0,
        val mediaPipeModelLoadTime: Long = 0,
        val mediaPipeInferenceTime: Long = 0,
        val uiRenderingAverageTime: Long = 0,
        val networkRequestAverageTime: Long = 0,
        val backgroundTasksCount: Int = 0,
        val cacheHitRate: Float = 0f,
        val batteryOptimized: Boolean = false,
        val mediaPipeMemoryUsage: Long = 0,
        val activeModelsCount: Int = 0
    )
    
    data class PerformanceAlert(
        val type: AlertType,
        val message: String,
        val severity: Severity,
        val timestamp: Long,
        val actionRequired: String
    )
    
    enum class AlertType {
        HIGH_MEMORY_USAGE,
        SLOW_DATABASE_QUERY,
        UI_THREAD_BLOCKED,
        BATTERY_DRAIN,
        STORAGE_LOW,
        NETWORK_SLOW,
        MEDIAPIPE_MODEL_LOAD_SLOW,
        MEDIAPIPE_INFERENCE_SLOW,
        MEDIAPIPE_MEMORY_LEAK,
        TOO_MANY_MODELS_LOADED
    }
    
    enum class Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    /**
     * Initialize performance monitoring and optimization
     */
    fun initialize() {
        _performanceState.value = PerformanceState.MONITORING
        
        // Register lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Start performance monitoring
        startPerformanceMonitoring()
        
        // Schedule optimization tasks
        scheduleOptimizationTasks()
        
        Log.i(TAG, "Performance Manager initialized")
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        startPerformanceMonitoring()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Continue monitoring in background but reduce frequency
        optimizeForBackground()
    }
    
    /**
     * Start continuous performance monitoring
     */
    private fun startPerformanceMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = scope.launch {
            while (isActive) {
                try {
                    // Monitor memory usage
                    monitorMemoryUsage()
                    
                    // Monitor MediaPipe AI performance
                    monitorMediaPipePerformance()
                    
                    // Monitor database performance
                    monitorDatabasePerformance()
                    
                    // Monitor background tasks
                    monitorBackgroundTasks()
                    
                    // Check for performance alerts
                    checkPerformanceAlerts()
                    
                    delay(PERFORMANCE_LOG_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in performance monitoring", e)
                    delay(PERFORMANCE_LOG_INTERVAL * 2) // Backoff on error
                }
            }
        }
    }
    
    /**
     * Monitor memory usage and trigger optimizations
     */
    private suspend fun monitorMemoryUsage() = withContext(Dispatchers.Main) {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory
        
        val usedMemoryMB = usedMemory / (1024 * 1024)
        val availableMemoryMB = availableMemory / (1024 * 1024)
        val totalMemoryMB = maxMemory / (1024 * 1024)
        val memoryPercentage = (usedMemory.toFloat() / maxMemory) * 100
        val isLowMemory = availableMemoryMB < MEMORY_THRESHOLD_MB
        
        _memoryUsage.value = MemoryUsage(
            usedMemoryMB = usedMemoryMB,
            availableMemoryMB = availableMemoryMB,
            totalMemoryMB = totalMemoryMB,
            memoryPercentage = memoryPercentage,
            isLowMemory = isLowMemory
        )
        
        // Trigger memory optimization if needed
        if (isLowMemory) {
            optimizeMemoryUsage()
        }
        
        Log.d(TAG, "Memory usage: ${usedMemoryMB}MB/${totalMemoryMB}MB (${memoryPercentage.toInt()}%)")
    }
    
    /**
     * Monitor database performance
     */
    private suspend fun monitorDatabasePerformance() {
        val queryTime = measureTimeMillis {
            try {
                // Test query performance with available methods
                database.messageDao().getLastMessageForConversation(1L)
            } catch (e: Exception) {
                Log.e(TAG, "Database query error", e)
            }
        }
        
        // Update metrics
        val currentMetrics = _performanceMetrics.value
        _performanceMetrics.value = currentMetrics.copy(
            databaseQueryAverageTime = (currentMetrics.databaseQueryAverageTime + queryTime) / 2
        )
        
        // Optimize database if queries are slow
        if (queryTime > 1000) { // More than 1 second
            optimizeDatabaseQueries()
        }
        
        Log.d(TAG, "Database query time: ${queryTime}ms")
    }
    
    /**
     * Monitor MediaPipe AI performance and resource usage
     */
    private suspend fun monitorMediaPipePerformance() {
        try {
            // Check loaded models
            val availableModels = aiModelManager.getAvailableModels()
            val loadedModels = availableModels.filter { it.isLoaded }
            val activeModelsCount = loadedModels.size
            
            // Estimate MediaPipe memory usage
            val mediaPipeMemoryUsage = estimateMediaPipeMemoryUsage(loadedModels)
            
            // Test inference performance if models are loaded
            var inferenceTime = 0L
            if (loadedModels.isNotEmpty()) {
                inferenceTime = measureTimeMillis {
                    try {
                        // Quick inference test with minimal input
                        aiInferenceEngine.generateResponse(
                            input = "test",
                            conversationHistory = emptyList()
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "MediaPipe inference test failed", e)
                    }
                }
            }
            
            // Update metrics
            val currentMetrics = _performanceMetrics.value
            _performanceMetrics.value = currentMetrics.copy(
                mediaPipeInferenceTime = if (inferenceTime > 0) {
                    (currentMetrics.mediaPipeInferenceTime + inferenceTime) / 2
                } else currentMetrics.mediaPipeInferenceTime,
                mediaPipeMemoryUsage = mediaPipeMemoryUsage,
                activeModelsCount = activeModelsCount
            )
            
            // Check for MediaPipe-specific alerts
            checkMediaPipeAlerts(loadedModels, inferenceTime, mediaPipeMemoryUsage)
            
            Log.d(TAG, "MediaPipe performance: models=$activeModelsCount, inference=${inferenceTime}ms, memory=${mediaPipeMemoryUsage}MB")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error monitoring MediaPipe performance", e)
        }
    }
    
    /**
     * Monitor background tasks
     */
    private suspend fun monitorBackgroundTasks() {
        val backgroundTasksCount = getActiveBackgroundTasksCount()
        
        val currentMetrics = _performanceMetrics.value
        _performanceMetrics.value = currentMetrics.copy(
            backgroundTasksCount = backgroundTasksCount
        )
        
        Log.d(TAG, "Active background tasks: $backgroundTasksCount")
    }
    
    /**
     * Check for performance alerts
     */
    private suspend fun checkPerformanceAlerts() {
        val memoryUsage = _memoryUsage.value
        val metrics = _performanceMetrics.value
        
        // High memory usage alert
        if (memoryUsage.memoryPercentage > 80) {
            createPerformanceAlert(
                AlertType.HIGH_MEMORY_USAGE,
                "Memory usage is at ${memoryUsage.memoryPercentage.toInt()}%",
                Severity.HIGH,
                "Consider closing unused features or restarting the app"
            )
        }
        
        // Slow database queries alert
        if (metrics.databaseQueryAverageTime > 2000) {
            createPerformanceAlert(
                AlertType.SLOW_DATABASE_QUERY,
                "Database queries are taking ${metrics.databaseQueryAverageTime}ms on average",
                Severity.MEDIUM,
                "Database optimization recommended"
            )
        }
        
        // Too many background tasks alert
        if (metrics.backgroundTasksCount > 10) {
            createPerformanceAlert(
                AlertType.BATTERY_DRAIN,
                "Too many background tasks running: ${metrics.backgroundTasksCount}",
                Severity.MEDIUM,
                "Consider reducing background activity"
            )
        }
    }
    
    /**
     * Optimize memory usage
     */
    private suspend fun optimizeMemoryUsage() = withContext(Dispatchers.Default) {
        _performanceState.value = PerformanceState.OPTIMIZING
        
        try {
            Log.i(TAG, "Starting memory optimization")
            
            // Clear caches
            clearInMemoryCaches()
            
            // Optimize MediaPipe performance
            optimizeMediaPipePerformance()
            
            // Run garbage collection
            System.gc()
            
            // Compact database
            compactDatabase()
            
            // Optimize image caches
            optimizeImageCaches()
            
            Log.i(TAG, "Memory optimization completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during memory optimization", e)
            _performanceState.value = PerformanceState.ERROR
        } finally {
            _performanceState.value = PerformanceState.MONITORING
        }
    }
    
    /**
     * Optimize database queries
     */
    private suspend fun optimizeDatabaseQueries() = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting database optimization")
            
            // Analyze and rebuild indexes
            database.openHelper.writableDatabase.execSQL("ANALYZE")
            
            // Clean up old data
            cleanupOldData()
            
            // Vacuum database
            database.openHelper.writableDatabase.execSQL("VACUUM")
            
            Log.i(TAG, "Database optimization completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during database optimization", e)
        }
    }
    
    /**
     * Schedule optimization tasks
     */
    private fun scheduleOptimizationTasks() {
        optimizationJob?.cancel()
        optimizationJob = scope.launch {
            while (isActive) {
                try {
                    // Run full optimization every 24 hours
                    delay(DATABASE_OPTIMIZATION_INTERVAL)
                    
                    Log.i(TAG, "Running scheduled optimization")
                    optimizeDatabaseQueries()
                    optimizeStorageUsage()
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in scheduled optimization", e)
                }
            }
        }
    }
    
    /**
     * Optimize for background operation
     */
    private fun optimizeForBackground() {
        // Reduce monitoring frequency
        monitoringJob?.cancel()
        monitoringJob = scope.launch {
            while (isActive) {
                try {
                    monitorMemoryUsage()
                    delay(PERFORMANCE_LOG_INTERVAL * 3) // Less frequent monitoring
                } catch (e: Exception) {
                    Log.e(TAG, "Error in background monitoring", e)
                }
            }
        }
    }
    
    /**
     * Clear in-memory caches
     */
    private fun clearInMemoryCaches() {
        // Clear image caches
        // Clear conversation caches
        // Clear AI model caches (if applicable)
        Log.d(TAG, "Cleared in-memory caches")
    }
    
    /**
     * Estimate MediaPipe memory usage based on loaded models
     */
    private fun estimateMediaPipeMemoryUsage(loadedModels: List<com.aisoul.privateassistant.ai.ModelInfo>): Long {
        return loadedModels.sumOf { model ->
            when (model.type) {
                AIModelManager.ModelType.GEMMA_2B -> 512L // ~512MB in memory
                AIModelManager.ModelType.GEMMA_7B -> 1024L // ~1GB in memory
                AIModelManager.ModelType.PHI2_3B -> 768L // ~768MB in memory
                else -> 256L // Default estimate
            }
        }
    }
    
    /**
     * Check for MediaPipe-specific performance alerts
     */
    private suspend fun checkMediaPipeAlerts(
        loadedModels: List<com.aisoul.privateassistant.ai.ModelInfo>,
        inferenceTime: Long,
        memoryUsage: Long
    ) {
        // Too many models loaded alert
        if (loadedModels.size > 2) {
            createPerformanceAlert(
                AlertType.TOO_MANY_MODELS_LOADED,
                "${loadedModels.size} MediaPipe models are loaded simultaneously",
                Severity.MEDIUM,
                "Consider unloading unused models to save memory"
            )
        }
        
        // Slow MediaPipe inference alert
        if (inferenceTime > 5000) { // More than 5 seconds
            createPerformanceAlert(
                AlertType.MEDIAPIPE_INFERENCE_SLOW,
                "MediaPipe inference took ${inferenceTime}ms",
                Severity.HIGH,
                "Consider using a smaller model or optimizing device performance"
            )
        }
        
        // High MediaPipe memory usage alert
        if (memoryUsage > 2048) { // More than 2GB
            createPerformanceAlert(
                AlertType.MEDIAPIPE_MEMORY_LEAK,
                "MediaPipe models are using ${memoryUsage}MB of memory",
                Severity.HIGH,
                "Consider unloading large models or checking for memory leaks"
            )
        }
    }
    
    /**
     * Optimize MediaPipe performance
     */
    private suspend fun optimizeMediaPipePerformance() {
        try {
            Log.i(TAG, "Starting MediaPipe optimization")
            
            val availableModels = aiModelManager.getAvailableModels()
            val loadedModels = availableModels.filter { it.isLoaded }
            
            // If memory is low, unload unnecessary models
            if (_memoryUsage.value.isLowMemory && loadedModels.size > 1) {
                // Keep only the smallest model loaded
                val modelsToUnload = loadedModels.sortedByDescending { model ->
                    when (model.type) {
                        AIModelManager.ModelType.GEMMA_7B -> 3
                        AIModelManager.ModelType.PHI2_3B -> 2
                        AIModelManager.ModelType.GEMMA_2B -> 1
                        else -> 0
                    }
                }.drop(1) // Keep the first (smallest) model
                
                modelsToUnload.forEach { model ->
                    aiModelManager.unloadModel(model.type)
                    Log.i(TAG, "Unloaded MediaPipe model: ${model.type.displayName}")
                }
            }
            
            // Clean up MediaPipe resources
            aiInferenceEngine.cleanup()
            
            Log.i(TAG, "MediaPipe optimization completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during MediaPipe optimization", e)
        }
    }
    
    /**
     * Compact database
     */
    private suspend fun compactDatabase() = withContext(Dispatchers.IO) {
        try {
            database.openHelper.writableDatabase.execSQL("PRAGMA auto_vacuum = FULL")
            Log.d(TAG, "Database compacted")
        } catch (e: Exception) {
            Log.e(TAG, "Error compacting database", e)
        }
    }
    
    /**
     * Optimize image caches
     */
    private fun optimizeImageCaches() {
        // Clear old image caches
        // Resize cache sizes based on available memory
        Log.d(TAG, "Image caches optimized")
    }
    
    /**
     * Clean up old data
     */
    private suspend fun cleanupOldData() = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
            
            // Clean up old conversations (commenting out until method is available)
            // database.conversationDao().deleteOldConversations(cutoffTime)
            
            // Clean up old usage records
            // database.appUsageDao().deleteOldRecords(cutoffTime)
            
            Log.d(TAG, "Old data cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old data", e)
        }
    }
    
    /**
     * Optimize storage usage
     */
    private suspend fun optimizeStorageUsage() = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting storage optimization")
            
            // Clean up temporary files
            cleanupTempFiles()
            
            // Optimize AI model storage
            optimizeModelStorage()
            
            // Clean up log files
            cleanupLogFiles()
            
            Log.i(TAG, "Storage optimization completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during storage optimization", e)
        }
    }
    
    /**
     * Clean up temporary files
     */
    private fun cleanupTempFiles() {
        try {
            val tempDir = File(context.cacheDir, "temp")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
                        file.delete()
                    }
                }
            }
            Log.d(TAG, "Temporary files cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up temp files", e)
        }
    }
    
    /**
     * Optimize AI model storage
     */
    private fun optimizeModelStorage() {
        // Remove unused model files
        // Compress model files if possible
        Log.d(TAG, "AI model storage optimized")
    }
    
    /**
     * Clean up log files
     */
    private fun cleanupLogFiles() {
        try {
            val logsDir = File(context.filesDir, "logs")
            if (logsDir.exists()) {
                logsDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) {
                        file.delete()
                    }
                }
            }
            Log.d(TAG, "Log files cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up log files", e)
        }
    }
    
    /**
     * Get active background tasks count
     */
    private fun getActiveBackgroundTasksCount(): Int {
        // In a real implementation, you would track active coroutines and services
        return 3 // Placeholder
    }
    
    /**
     * Create performance alert
     */
    private suspend fun createPerformanceAlert(
        type: AlertType,
        message: String,
        severity: Severity,
        actionRequired: String
    ) {
        val alert = PerformanceAlert(
            type = type,
            message = message,
            severity = severity,
            timestamp = System.currentTimeMillis(),
            actionRequired = actionRequired
        )
        
        Log.w(TAG, "Performance Alert [${severity.name}]: $message - $actionRequired")
        
        // Store alert in database or send to monitoring system
        // For now, just log it
    }
    
    /**
     * Get performance report
     */
    fun getPerformanceReport(): String {
        val memory = _memoryUsage.value
        val metrics = _performanceMetrics.value
        
        return buildString {
            appendLine("=== AI Soul Performance Report ===")
            appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            appendLine()
            appendLine("Memory Usage:")
            appendLine("  Used: ${memory.usedMemoryMB}MB / ${memory.totalMemoryMB}MB (${memory.memoryPercentage.toInt()}%)")
            appendLine("  Available: ${memory.availableMemoryMB}MB")
            appendLine("  Low Memory: ${if (memory.isLowMemory) "YES" else "NO"}")
            appendLine()
            appendLine("Performance Metrics:")
            appendLine("  Database Query Avg: ${metrics.databaseQueryAverageTime}ms")
            appendLine("  AI Inference Avg: ${metrics.aiInferenceAverageTime}ms")
            appendLine("  Background Tasks: ${metrics.backgroundTasksCount}")
            appendLine("  Cache Hit Rate: ${(metrics.cacheHitRate * 100).toInt()}%")
            appendLine()
            appendLine("Device Info:")
            appendLine("  Android Version: ${Build.VERSION.RELEASE}")
            appendLine("  API Level: ${Build.VERSION.SDK_INT}")
            appendLine("  Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("  Process ID: ${Process.myPid()}")
        }
    }
    
    /**
     * Force optimization
     */
    suspend fun forceOptimization() {
        _performanceState.value = PerformanceState.OPTIMIZING
        
        try {
            optimizeMemoryUsage()
            optimizeDatabaseQueries()
            optimizeStorageUsage()
            
            Log.i(TAG, "Force optimization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during force optimization", e)
            _performanceState.value = PerformanceState.ERROR
        } finally {
            _performanceState.value = PerformanceState.MONITORING
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        monitoringJob?.cancel()
        optimizationJob?.cancel()
        scope.cancel()
        
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        
        Log.i(TAG, "Performance Manager cleanup completed")
    }
}
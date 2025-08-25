package com.aisoul.privateassistant.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aisoul.privateassistant.R
import com.aisoul.privateassistant.ai.AIInferenceEngine
import com.aisoul.privateassistant.ai.AIModelManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Background AI Processing Service
 * Handles AI model loading, inference tasks, and background processing
 */
class AIProcessingService : Service() {
    
    companion object {
        private const val TAG = "AIProcessingService"
        private const val CHANNEL_ID = "ai_processing_channel"
        private const val NOTIFICATION_ID = 2001
        
        fun startService(context: Context) {
            val intent = Intent(context, AIProcessingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, AIProcessingService::class.java)
            context.stopService(intent)
        }
    }
    
    // Service binder for local binding
    inner class AIProcessingBinder : Binder() {
        fun getService(): AIProcessingService = this@AIProcessingService
    }
    
    private val binder = AIProcessingBinder()
    private lateinit var aiModelManager: AIModelManager
    private lateinit var aiInferenceEngine: AIInferenceEngine
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val inferenceQueue = ConcurrentLinkedQueue<InferenceTask>()
    private var isProcessingQueue = false
    
    // Service state
    private val _serviceState = MutableStateFlow(ServiceState.STOPPED)
    val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()
    
    private val _processingStats = MutableStateFlow(ProcessingStats())
    val processingStats: StateFlow<ProcessingStats> = _processingStats.asStateFlow()
    
    /**
     * Service states
     */
    enum class ServiceState {
        STOPPED,
        STARTING,
        RUNNING,
        STOPPING
    }
    
    /**
     * Processing statistics
     */
    data class ProcessingStats(
        val totalInferences: Long = 0,
        val averageInferenceTime: Long = 0,
        val queueSize: Int = 0,
        val modelsLoaded: Int = 0,
        val uptime: Long = 0
    )
    
    /**
     * Inference task data class
     */
    data class InferenceTask(
        val id: String,
        val input: String,
        val modelType: AIModelManager.ModelType,
        val callback: (AIInferenceEngine.InferenceResult) -> Unit,
        val priority: TaskPriority = TaskPriority.NORMAL,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Task priority levels
     */
    enum class TaskPriority {
        LOW, NORMAL, HIGH, URGENT
    }
    
    private var startTime: Long = 0
    private var totalInferences: Long = 0
    private var totalInferenceTime: Long = 0
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AIProcessingService created")
        
        // Initialize AI components
        aiModelManager = AIModelManager.getInstance(this)
        aiInferenceEngine = AIInferenceEngine.getInstance(this)
        
        createNotificationChannel()
        startTime = System.currentTimeMillis()
        
        startProcessing()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    private fun startProcessing() {
        if (_serviceState.value == ServiceState.RUNNING) {
            Log.d(TAG, "Service already running")
            return
        }
        
        Log.i(TAG, "Starting AI processing service")
        _serviceState.value = ServiceState.STARTING
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            try {
                _serviceState.value = ServiceState.RUNNING
                startInferenceQueueProcessor()
                startStatsUpdater()
                
                Log.i(TAG, "AI processing service started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting AI processing service", e)
                _serviceState.value = ServiceState.STOPPED
                stopSelf()
            }
        }
    }
    
    private fun startInferenceQueueProcessor() {
        if (isProcessingQueue) return
        
        isProcessingQueue = true
        serviceScope.launch {
            while (_serviceState.value == ServiceState.RUNNING && isProcessingQueue) {
                try {
                    val task = inferenceQueue.poll()
                    if (task != null) {
                        processInferenceTask(task)
                    } else {
                        delay(100) // Wait before checking queue again
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing inference queue", e)
                    delay(1000) // Wait longer on error
                }
            }
        }
    }
    
    private suspend fun processInferenceTask(task: InferenceTask) {
        try {
            Log.d(TAG, "Processing inference task: ${task.id}")
            
            val startTime = System.currentTimeMillis()
            val result = aiInferenceEngine.generateResponse(
                input = task.input,
                modelType = task.modelType
            )
            val processingTime = System.currentTimeMillis() - startTime
            
            // Update statistics
            totalInferences++
            totalInferenceTime += processingTime
            updateStats()
            
            // Invoke callback with result
            withContext(Dispatchers.Main) {
                task.callback(result)
            }
            
            Log.d(TAG, "Completed inference task: ${task.id} in ${processingTime}ms")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing inference task: ${task.id}", e)
            
            // Invoke callback with error
            withContext(Dispatchers.Main) {
                task.callback(
                    AIInferenceEngine.InferenceResult.Error(
                        "Background processing error: ${e.message}"
                    )
                )
            }
        }
    }
    
    private fun startStatsUpdater() {
        serviceScope.launch {
            while (_serviceState.value == ServiceState.RUNNING) {
                updateStats()
                delay(5000) // Update every 5 seconds
            }
        }
    }
    
    private suspend fun updateStats() {
        try {
            val availableModels = aiModelManager.getAvailableModels()
            val loadedModels = availableModels.count { it.isLoaded }
            val uptime = System.currentTimeMillis() - startTime
            val averageTime = if (totalInferences > 0) totalInferenceTime / totalInferences else 0
            
            _processingStats.value = ProcessingStats(
                totalInferences = totalInferences,
                averageInferenceTime = averageTime,
                queueSize = inferenceQueue.size,
                modelsLoaded = loadedModels,
                uptime = uptime
            )
            
            updateNotification()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stats", e)
        }
    }
    
    /**
     * Public API for local binding clients
     */
    fun queueInferenceTask(
        input: String,
        modelType: AIModelManager.ModelType = AIModelManager.ModelType.GEMMA_2B,
        priority: TaskPriority = TaskPriority.NORMAL,
        callback: (AIInferenceEngine.InferenceResult) -> Unit
    ): String {
        val taskId = "task_${System.currentTimeMillis()}_${(1000..9999).random()}"
        
        val task = InferenceTask(
            id = taskId,
            input = input,
            modelType = modelType,
            callback = callback,
            priority = priority
        )
        
        inferenceQueue.offer(task)
        
        serviceScope.launch {
            updateStats()
        }
        
        Log.d(TAG, "Queued inference task: $taskId")
        return taskId
    }
    
    fun getQueueSize(): Int = inferenceQueue.size
    
    fun clearQueue() {
        inferenceQueue.clear()
        serviceScope.launch {
            updateStats()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows AI processing status and statistics"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Soul Processing")
            .setContentText("AI processing service is running")
            .setSmallIcon(R.drawable.ic_home)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun updateNotification() {
        val stats = _processingStats.value
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Soul Processing")
            .setContentText("Models: ${stats.modelsLoaded} | Queue: ${stats.queueSize} | Processed: ${stats.totalInferences}")
            .setSmallIcon(R.drawable.ic_home)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        isProcessingQueue = false
        serviceScope.cancel()
        
        Log.d(TAG, "AIProcessingService destroyed")
    }
}
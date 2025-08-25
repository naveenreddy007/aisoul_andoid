package com.aisoul.privateassistant.error

import android.content.Context
import android.os.Build
import android.util.Log
import com.aisoul.privateassistant.data.database.AppDatabase
import com.aisoul.privateassistant.data.entities.ErrorLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Advanced Error Handling and Logging System
 * Provides comprehensive error tracking, logging, and crash reporting
 */
class ErrorHandler private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: ErrorHandler? = null
        
        fun getInstance(context: Context): ErrorHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ErrorHandler(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val TAG = "ErrorHandler"
        private const val LOG_FILE_PREFIX = "aisoul_log_"
        private const val CRASH_FILE_PREFIX = "aisoul_crash_"
        private const val MAX_LOG_FILES = 10
        private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024 // 5MB
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Logging directories
    private val logsDir = File(context.filesDir, "logs")
    private val crashesDir = File(context.filesDir, "crashes")
    
    // State management
    private val _errorState = MutableStateFlow(ErrorState.IDLE)
    val errorState: StateFlow<ErrorState> = _errorState.asStateFlow()
    
    private val _recentErrors = MutableStateFlow<List<ErrorInfo>>(emptyList())
    val recentErrors: StateFlow<List<ErrorInfo>> = _recentErrors.asStateFlow()
    
    // Current log file
    private var currentLogFile: File? = null
    private var currentLogWriter: FileWriter? = null
    
    enum class ErrorState {
        IDLE,
        LOGGING,
        CRASH_DETECTED,
        REPORTING,
        ERROR
    }
    
    enum class LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        FATAL
    }
    
    enum class ErrorCategory {
        AI_PROCESSING,
        DATABASE,
        NETWORK,
        UI,
        PERMISSION,
        SECURITY,
        PERFORMANCE,
        SYSTEM,
        UNKNOWN
    }
    
    data class ErrorInfo(
        val id: String = UUID.randomUUID().toString(),
        val level: LogLevel,
        val category: ErrorCategory,
        val message: String,
        val stackTrace: String?,
        val timestamp: Long = System.currentTimeMillis(),
        val context: String?,
        val userAction: String?,
        val deviceInfo: DeviceInfo,
        val appVersion: String,
        val userId: String? = null
    )
    
    data class DeviceInfo(
        val manufacturer: String = Build.MANUFACTURER,
        val model: String = Build.MODEL,
        val androidVersion: String = Build.VERSION.RELEASE,
        val apiLevel: Int = Build.VERSION.SDK_INT,
        val availableMemory: Long = 0,
        val totalMemory: Long = 0,
        val batteryLevel: Int = -1
    )
    
    /**
     * Initialize error handling system
     */
    fun initialize() {
        // Create logging directories
        logsDir.mkdirs()
        crashesDir.mkdirs()
        
        // Set up uncaught exception handler
        setupUncaughtExceptionHandler()
        
        // Initialize log file
        initializeLogFile()
        
        // Load recent errors from database
        loadRecentErrors()
        
        // Clean up old log files
        cleanupOldLogs()
        
        Log.i(TAG, "Error Handler initialized")
    }
    
    /**
     * Log debug message
     */
    fun logDebug(tag: String, message: String, context: String? = null) {
        log(LogLevel.DEBUG, ErrorCategory.SYSTEM, tag, message, null, context, null)
    }
    
    /**
     * Log info message
     */
    fun logInfo(tag: String, message: String, context: String? = null) {
        log(LogLevel.INFO, ErrorCategory.SYSTEM, tag, message, null, context, null)
    }
    
    /**
     * Log warning message
     */
    fun logWarning(tag: String, message: String, context: String? = null) {
        log(LogLevel.WARNING, ErrorCategory.SYSTEM, tag, message, null, context, null)
    }
    
    /**
     * Log error message
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null, context: String? = null) {
        log(LogLevel.ERROR, categorizeError(throwable), tag, message, throwable, context, null)
    }
    
    /**
     * Log fatal error
     */
    fun logFatal(tag: String, message: String, throwable: Throwable, context: String? = null) {
        log(LogLevel.FATAL, categorizeError(throwable), tag, message, throwable, context, null)
    }
    
    /**
     * Log AI processing error
     */
    fun logAIError(message: String, throwable: Throwable? = null, context: String? = null) {
        log(LogLevel.ERROR, ErrorCategory.AI_PROCESSING, "AI", message, throwable, context, null)
    }
    
    /**
     * Log database error
     */
    fun logDatabaseError(message: String, throwable: Throwable? = null, context: String? = null) {
        log(LogLevel.ERROR, ErrorCategory.DATABASE, "Database", message, throwable, context, null)
    }
    
    /**
     * Log network error
     */
    fun logNetworkError(message: String, throwable: Throwable? = null, context: String? = null) {
        log(LogLevel.ERROR, ErrorCategory.NETWORK, "Network", message, throwable, context, null)
    }
    
    /**
     * Log UI error
     */
    fun logUIError(message: String, throwable: Throwable? = null, context: String? = null) {
        log(LogLevel.ERROR, ErrorCategory.UI, "UI", message, throwable, context, null)
    }
    
    /**
     * Log performance issue
     */
    fun logPerformanceIssue(message: String, context: String? = null) {
        log(LogLevel.WARNING, ErrorCategory.PERFORMANCE, "Performance", message, null, context, null)
    }
    
    /**
     * Log security event
     */
    fun logSecurityEvent(message: String, context: String? = null) {
        log(LogLevel.WARNING, ErrorCategory.SECURITY, "Security", message, null, context, null)
    }
    
    /**
     * Log user action with error
     */
    fun logUserActionError(userAction: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, ErrorCategory.UI, "UserAction", message, throwable, null, userAction)
    }
    
    /**
     * Main logging function
     */
    private fun log(
        level: LogLevel,
        category: ErrorCategory,
        tag: String,
        message: String,
        throwable: Throwable?,
        context: String?,
        userAction: String?
    ) {
        _errorState.value = ErrorState.LOGGING
        
        scope.launch {
            try {
                val deviceInfo = getCurrentDeviceInfo()
                val appVersion = getAppVersion()
                
                val errorInfo = ErrorInfo(
                    level = level,
                    category = category,
                    message = "[$tag] $message",
                    stackTrace = throwable?.let { getStackTrace(it) },
                    context = context,
                    userAction = userAction,
                    deviceInfo = deviceInfo,
                    appVersion = appVersion
                )
                
                // Log to Android Log
                logToAndroidLog(level, tag, message, throwable)
                
                // Log to file
                logToFile(errorInfo)
                
                // Store in database
                storeInDatabase(errorInfo)
                
                // Update recent errors
                updateRecentErrors(errorInfo)
                
                // Check if crash reporting is needed
                if (level == LogLevel.FATAL) {
                    handleCrash(errorInfo)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in logging system", e)
                _errorState.value = ErrorState.ERROR
            } finally {
                _errorState.value = ErrorState.IDLE
            }
        }
    }
    
    /**
     * Log to Android Log system
     */
    private fun logToAndroidLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARNING -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
            LogLevel.FATAL -> Log.wtf(tag, message, throwable)
        }
    }
    
    /**
     * Log to file
     */
    private suspend fun logToFile(errorInfo: ErrorInfo) = withContext(Dispatchers.IO) {
        try {
            val logEntry = formatLogEntry(errorInfo)
            
            // Check if current log file is too large
            if (currentLogFile?.length() ?: 0 > MAX_LOG_FILE_SIZE) {
                rotateLogFile()
            }
            
            // Ensure log file is initialized
            if (currentLogWriter == null) {
                initializeLogFile()
            }
            
            currentLogWriter?.appendLine(logEntry)
            currentLogWriter?.flush()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to log file", e)
        }
    }
    
    /**
     * Store error in database
     */
    private suspend fun storeInDatabase(errorInfo: ErrorInfo) {
        try {
            val errorLog = ErrorLog(
                id = 0,
                errorId = errorInfo.id,
                level = errorInfo.level.name,
                category = errorInfo.category.name,
                message = errorInfo.message,
                stackTrace = errorInfo.stackTrace,
                timestamp = errorInfo.timestamp,
                context = errorInfo.context,
                userAction = errorInfo.userAction,
                deviceInfo = formatDeviceInfo(errorInfo.deviceInfo),
                appVersion = errorInfo.appVersion,
                resolved = false
            )
            
            database.errorDao().insertErrorLog(errorLog)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error storing to database", e)
        }
    }
    
    /**
     * Update recent errors list
     */
    private fun updateRecentErrors(errorInfo: ErrorInfo) {
        val currentErrors = _recentErrors.value.toMutableList()
        currentErrors.add(0, errorInfo) // Add to beginning
        
        // Keep only last 50 errors
        if (currentErrors.size > 50) {
            currentErrors.removeAt(currentErrors.size - 1)
        }
        
        _recentErrors.value = currentErrors
    }
    
    /**
     * Handle crash scenarios
     */
    private suspend fun handleCrash(errorInfo: ErrorInfo) = withContext(Dispatchers.IO) {
        _errorState.value = ErrorState.CRASH_DETECTED
        
        try {
            // Create crash report file
            val crashFile = File(crashesDir, "${CRASH_FILE_PREFIX}${System.currentTimeMillis()}.txt")
            val crashReport = generateCrashReport(errorInfo)
            
            crashFile.writeText(crashReport)
            
            Log.e(TAG, "Crash detected and logged: ${crashFile.absolutePath}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling crash", e)
        }
    }
    
    /**
     * Setup uncaught exception handler
     */
    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log the crash
            logFatal("UncaughtException", "Uncaught exception in thread: ${thread.name}", throwable, "Thread: ${thread.name}")
            
            // Call default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * Initialize log file
     */
    private fun initializeLogFile() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            currentLogFile = File(logsDir, "${LOG_FILE_PREFIX}${timestamp}.txt")
            currentLogWriter = FileWriter(currentLogFile, true)
            
            Log.d(TAG, "Log file initialized: ${currentLogFile?.absolutePath}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing log file", e)
        }
    }
    
    /**
     * Rotate log file when it gets too large
     */
    private fun rotateLogFile() {
        try {
            currentLogWriter?.close()
            currentLogWriter = null
            currentLogFile = null
            
            initializeLogFile()
            
            Log.d(TAG, "Log file rotated")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating log file", e)
        }
    }
    
    /**
     * Clean up old log files
     */
    private fun cleanupOldLogs() {
        try {
            // Clean up log files
            val logFiles = logsDir.listFiles { _, name -> name.startsWith(LOG_FILE_PREFIX) }
                ?.sortedByDescending { it.lastModified() }
            
            if (logFiles != null && logFiles.size > MAX_LOG_FILES) {
                logFiles.drop(MAX_LOG_FILES).forEach { it.delete() }
            }
            
            // Clean up crash files older than 30 days
            val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
            crashesDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    file.delete()
                }
            }
            
            Log.d(TAG, "Old log files cleaned up")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up logs", e)
        }
    }
    
    /**
     * Load recent errors from database
     */
    private fun loadRecentErrors() {
        scope.launch {
            try {
                val errorLogs = database.errorDao().getRecentErrorLogs(50)
                val errorInfos = errorLogs.map { errorLog ->
                    ErrorInfo(
                        id = errorLog.errorId,
                        level = LogLevel.valueOf(errorLog.level),
                        category = ErrorCategory.valueOf(errorLog.category),
                        message = errorLog.message,
                        stackTrace = errorLog.stackTrace,
                        timestamp = errorLog.timestamp,
                        context = errorLog.context,
                        userAction = errorLog.userAction,
                        deviceInfo = parseDeviceInfo(errorLog.deviceInfo),
                        appVersion = errorLog.appVersion
                    )
                }
                
                _recentErrors.value = errorInfos
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recent errors", e)
            }
        }
    }
    
    /**
     * Categorize error based on exception type
     */
    private fun categorizeError(throwable: Throwable?): ErrorCategory {
        return when (throwable) {
            is java.sql.SQLException, is android.database.SQLException -> ErrorCategory.DATABASE
            is java.net.SocketException, is java.io.IOException -> ErrorCategory.NETWORK
            is SecurityException -> ErrorCategory.SECURITY
            is OutOfMemoryError -> ErrorCategory.PERFORMANCE
            is NullPointerException, is IllegalArgumentException -> ErrorCategory.SYSTEM
            else -> ErrorCategory.UNKNOWN
        }
    }
    
    /**
     * Get current device info
     */
    private fun getCurrentDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            availableMemory = getAvailableMemory(),
            totalMemory = getTotalMemory(),
            batteryLevel = getBatteryLevel()
        )
    }
    
    /**
     * Get app version
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Get stack trace as string
     */
    private fun getStackTrace(throwable: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        return stringWriter.toString()
    }
    
    /**
     * Format log entry
     */
    private fun formatLogEntry(errorInfo: ErrorInfo): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            .format(Date(errorInfo.timestamp))
        
        return buildString {
            appendLine("[$timestamp] [${errorInfo.level.name}] [${errorInfo.category.name}]")
            appendLine("Message: ${errorInfo.message}")
            errorInfo.context?.let { appendLine("Context: $it") }
            errorInfo.userAction?.let { appendLine("User Action: $it") }
            errorInfo.stackTrace?.let { 
                appendLine("Stack Trace:")
                appendLine(it)
            }
            appendLine("Device: ${errorInfo.deviceInfo.manufacturer} ${errorInfo.deviceInfo.model}")
            appendLine("Android: ${errorInfo.deviceInfo.androidVersion} (API ${errorInfo.deviceInfo.apiLevel})")
            appendLine("App: ${errorInfo.appVersion}")
            appendLine("---")
        }
    }
    
    /**
     * Format device info for database storage
     */
    private fun formatDeviceInfo(deviceInfo: DeviceInfo): String {
        return "${deviceInfo.manufacturer}|${deviceInfo.model}|${deviceInfo.androidVersion}|${deviceInfo.apiLevel}|${deviceInfo.availableMemory}|${deviceInfo.totalMemory}|${deviceInfo.batteryLevel}"
    }
    
    /**
     * Parse device info from database
     */
    private fun parseDeviceInfo(deviceInfoString: String): DeviceInfo {
        return try {
            val parts = deviceInfoString.split("|")
            DeviceInfo(
                manufacturer = parts.getOrNull(0) ?: "Unknown",
                model = parts.getOrNull(1) ?: "Unknown",
                androidVersion = parts.getOrNull(2) ?: "Unknown",
                apiLevel = parts.getOrNull(3)?.toIntOrNull() ?: 0,
                availableMemory = parts.getOrNull(4)?.toLongOrNull() ?: 0,
                totalMemory = parts.getOrNull(5)?.toLongOrNull() ?: 0,
                batteryLevel = parts.getOrNull(6)?.toIntOrNull() ?: -1
            )
        } catch (e: Exception) {
            DeviceInfo()
        }
    }
    
    /**
     * Generate crash report
     */
    private fun generateCrashReport(errorInfo: ErrorInfo): String {
        return buildString {
            appendLine("=== AI Soul Private Assistant Crash Report ===")
            appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            appendLine()
            appendLine("Error ID: ${errorInfo.id}")
            appendLine("Level: ${errorInfo.level.name}")
            appendLine("Category: ${errorInfo.category.name}")
            appendLine("Message: ${errorInfo.message}")
            appendLine()
            errorInfo.context?.let {
                appendLine("Context: $it")
                appendLine()
            }
            errorInfo.userAction?.let {
                appendLine("User Action: $it")
                appendLine()
            }
            appendLine("Device Information:")
            appendLine("  Manufacturer: ${errorInfo.deviceInfo.manufacturer}")
            appendLine("  Model: ${errorInfo.deviceInfo.model}")
            appendLine("  Android Version: ${errorInfo.deviceInfo.androidVersion}")
            appendLine("  API Level: ${errorInfo.deviceInfo.apiLevel}")
            appendLine("  Available Memory: ${errorInfo.deviceInfo.availableMemory / 1024 / 1024}MB")
            appendLine("  Total Memory: ${errorInfo.deviceInfo.totalMemory / 1024 / 1024}MB")
            appendLine("  Battery Level: ${errorInfo.deviceInfo.batteryLevel}%")
            appendLine()
            appendLine("App Version: ${errorInfo.appVersion}")
            appendLine()
            errorInfo.stackTrace?.let {
                appendLine("Stack Trace:")
                appendLine(it)
            }
        }
    }
    
    // Helper methods for device info
    
    private fun getAvailableMemory(): Long {
        return try {
            Runtime.getRuntime().freeMemory()
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getTotalMemory(): Long {
        return try {
            Runtime.getRuntime().totalMemory()
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getBatteryLevel(): Int {
        return try {
            // This would require proper battery manager implementation
            -1 // Placeholder
        } catch (e: Exception) {
            -1
        }
    }
    
    /**
     * Get error statistics
     */
    suspend fun getErrorStatistics(): ErrorStatistics {
        return try {
            val errorLogs = database.errorDao().getAllErrorLogs()
            
            val totalErrors = errorLogs.size
            val errorsByLevel = errorLogs.groupBy { it.level }.mapValues { it.value.size }
            val errorsByCategory = errorLogs.groupBy { it.category }.mapValues { it.value.size }
            val recentErrorsCount = errorLogs.count { 
                it.timestamp > System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            }
            
            ErrorStatistics(
                totalErrors = totalErrors,
                errorsByLevel = errorsByLevel,
                errorsByCategory = errorsByCategory,
                recentErrorsCount = recentErrorsCount,
                crashCount = errorLogs.count { it.level == LogLevel.FATAL.name }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting statistics", e)
            ErrorStatistics()
        }
    }
    
    data class ErrorStatistics(
        val totalErrors: Int = 0,
        val errorsByLevel: Map<String, Int> = emptyMap(),
        val errorsByCategory: Map<String, Int> = emptyMap(),
        val recentErrorsCount: Int = 0,
        val crashCount: Int = 0
    )
    
    /**
     * Export logs for debugging
     */
    suspend fun exportLogs(): File? = withContext(Dispatchers.IO) {
        try {
            val exportFile = File(context.externalCacheDir, "aisoul_logs_export_${System.currentTimeMillis()}.txt")
            val exportWriter = FileWriter(exportFile)
            
            // Export recent error logs
            val errorLogs = database.errorDao().getRecentErrorLogs(1000)
            
            exportWriter.appendLine("=== AI Soul Private Assistant Log Export ===")
            exportWriter.appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            exportWriter.appendLine("Total Entries: ${errorLogs.size}")
            exportWriter.appendLine()
            
            errorLogs.forEach { errorLog ->
                exportWriter.appendLine("[$errorLog.timestamp] [${errorLog.level}] [${errorLog.category}]")
                exportWriter.appendLine("Message: ${errorLog.message}")
                errorLog.context?.let { exportWriter.appendLine("Context: $it") }
                errorLog.userAction?.let { exportWriter.appendLine("User Action: $it") }
                errorLog.stackTrace?.let { exportWriter.appendLine("Stack Trace: $it") }
                exportWriter.appendLine("---")
            }
            
            exportWriter.close()
            exportFile
            
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting logs", e)
            null
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            currentLogWriter?.close()
            scope.cancel()
            Log.i(TAG, "Error Handler cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}
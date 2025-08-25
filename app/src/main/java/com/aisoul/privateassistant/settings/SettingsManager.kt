package com.aisoul.privateassistant.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.GeneralSecurityException
import java.io.IOException

/**
 * Advanced Settings and Configuration Manager
 * Handles app preferences, AI settings, privacy controls, and performance configurations
 */
class SettingsManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null
        
        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Preference file names
        private const val PREFS_GENERAL = "aisoul_general_prefs"
        private const val PREFS_AI = "aisoul_ai_prefs"
        private const val PREFS_PRIVACY = "aisoul_privacy_prefs"
        private const val PREFS_PERFORMANCE = "aisoul_performance_prefs"
        private const val PREFS_SECURITY = "aisoul_security_prefs"
        
        // General Settings Keys
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_CRASH_REPORTING = "crash_reporting"
        
        // AI Settings Keys
        private const val KEY_AI_MODEL_TYPE = "ai_model_type"
        private const val KEY_AI_MODEL_SIZE = "ai_model_size"
        private const val KEY_INFERENCE_MODE = "inference_mode"
        private const val KEY_RESPONSE_STYLE = "response_style"
        private const val KEY_CONTEXT_MEMORY = "context_memory"
        private const val KEY_VOICE_ENABLED = "voice_enabled"
        private const val KEY_TTS_SPEED = "tts_speed"
        private const val KEY_TTS_PITCH = "tts_pitch"
        private const val KEY_WAKE_WORD = "wake_word"
        private const val KEY_AI_SUGGESTIONS = "ai_suggestions"
        
        // Privacy Settings Keys
        private const val KEY_DATA_COLLECTION = "data_collection"
        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
        private const val KEY_LOCATION_ACCESS = "location_access"
        private const val KEY_CONTACTS_ACCESS = "contacts_access"
        private const val KEY_SMS_ACCESS = "sms_access"
        private const val KEY_NOTIFICATION_ACCESS = "notification_access"
        private const val KEY_USAGE_STATS_ACCESS = "usage_stats_access"
        private const val KEY_DATA_RETENTION_DAYS = "data_retention_days"
        private const val KEY_AUTO_DELETE_CONVERSATIONS = "auto_delete_conversations"
        private const val KEY_SECURE_MODE = "secure_mode"
        
        // Performance Settings Keys
        private const val KEY_PERFORMANCE_MODE = "performance_mode"
        private const val KEY_BACKGROUND_PROCESSING = "background_processing"
        private const val KEY_CACHE_SIZE = "cache_size"
        private const val KEY_MEMORY_OPTIMIZATION = "memory_optimization"
        private const val KEY_BATTERY_OPTIMIZATION = "battery_optimization"
        private const val KEY_NETWORK_OPTIMIZATION = "network_optimization"
        private const val KEY_DATABASE_OPTIMIZATION = "database_optimization"
        private const val KEY_MAX_CONVERSATION_HISTORY = "max_conversation_history"
        
        // Security Settings Keys
        private const val KEY_ENCRYPTION_ENABLED = "encryption_enabled"
        private const val KEY_BIOMETRIC_AUTH = "biometric_auth"
        private const val KEY_APP_LOCK = "app_lock"
        private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
        private const val KEY_SECURE_KEYBOARD = "secure_keyboard"
        private const val KEY_SCREENSHOT_PROTECTION = "screenshot_protection"
        private const val KEY_INCOGNITO_MODE = "incognito_mode"
    }
    
    // Encrypted preferences for sensitive data
    private val encryptedPrefs: SharedPreferences by lazy {
        createEncryptedSharedPreferences(PREFS_SECURITY)
    }
    
    // Regular preferences for non-sensitive data
    private val generalPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_GENERAL, Context.MODE_PRIVATE)
    }
    
    private val aiPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_AI, Context.MODE_PRIVATE)
    }
    
    private val privacyPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_PRIVACY, Context.MODE_PRIVATE)
    }
    
    private val performancePrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_PERFORMANCE, Context.MODE_PRIVATE)
    }
    
    // State flows for reactive settings
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    data class SettingsState(
        val isFirstLaunch: Boolean = true,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val language: String = "en",
        val notificationsEnabled: Boolean = true,
        val aiModelType: AIModelType = AIModelType.GEMMA_2B,
        val responseStyle: ResponseStyle = ResponseStyle.BALANCED,
        val voiceEnabled: Boolean = false,
        val dataCollectionEnabled: Boolean = false,
        val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
        val encryptionEnabled: Boolean = true,
        val biometricAuthEnabled: Boolean = false
    )
    
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
    
    enum class AIModelType {
        GEMMA_2B, GEMMA_7B, PHI3_MINI, CUSTOM
    }
    
    enum class ResponseStyle {
        CONCISE, BALANCED, DETAILED, CREATIVE, TECHNICAL
    }
    
    enum class PerformanceMode {
        BATTERY_SAVER, BALANCED, PERFORMANCE
    }
    
    enum class InferenceMode {
        LOCAL_ONLY, HYBRID, CLOUD_PREFERRED
    }
    
    /**
     * Initialize settings manager
     */
    fun initialize() {
        loadCurrentSettings()
    }
    
    /**
     * Load current settings and update state
     */
    private fun loadCurrentSettings() {
        val currentState = SettingsState(
            isFirstLaunch = isFirstLaunch(),
            themeMode = getThemeMode(),
            language = getLanguage(),
            notificationsEnabled = areNotificationsEnabled(),
            aiModelType = getAIModelType(),
            responseStyle = getResponseStyle(),
            voiceEnabled = isVoiceEnabled(),
            dataCollectionEnabled = isDataCollectionEnabled(),
            performanceMode = getPerformanceMode(),
            encryptionEnabled = isEncryptionEnabled(),
            biometricAuthEnabled = isBiometricAuthEnabled()
        )
        
        _settingsState.value = currentState
    }
    
    // General Settings
    
    fun isFirstLaunch(): Boolean = generalPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
    
    fun setFirstLaunchCompleted() {
        generalPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        updateSettingsState { it.copy(isFirstLaunch = false) }
    }
    
    fun getThemeMode(): ThemeMode {
        val mode = generalPrefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.valueOf(mode ?: ThemeMode.SYSTEM.name)
    }
    
    fun setThemeMode(mode: ThemeMode) {
        generalPrefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        updateSettingsState { it.copy(themeMode = mode) }
    }
    
    fun getLanguage(): String = generalPrefs.getString(KEY_LANGUAGE, "en") ?: "en"
    
    fun setLanguage(language: String) {
        generalPrefs.edit().putString(KEY_LANGUAGE, language).apply()
        updateSettingsState { it.copy(language = language) }
    }
    
    fun areNotificationsEnabled(): Boolean = generalPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    
    fun setNotificationsEnabled(enabled: Boolean) {
        generalPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        updateSettingsState { it.copy(notificationsEnabled = enabled) }
    }
    
    fun isSoundEnabled(): Boolean = generalPrefs.getBoolean(KEY_SOUND_ENABLED, true)
    
    fun setSoundEnabled(enabled: Boolean) {
        generalPrefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }
    
    fun isVibrationEnabled(): Boolean = generalPrefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    
    fun setVibrationEnabled(enabled: Boolean) {
        generalPrefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }
    
    fun isAutoBackupEnabled(): Boolean = generalPrefs.getBoolean(KEY_AUTO_BACKUP, false)
    
    fun setAutoBackupEnabled(enabled: Boolean) {
        generalPrefs.edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply()
    }
    
    fun isCrashReportingEnabled(): Boolean = generalPrefs.getBoolean(KEY_CRASH_REPORTING, true)
    
    fun setCrashReportingEnabled(enabled: Boolean) {
        generalPrefs.edit().putBoolean(KEY_CRASH_REPORTING, enabled).apply()
    }
    
    // AI Settings
    
    fun getAIModelType(): AIModelType {
        val type = aiPrefs.getString(KEY_AI_MODEL_TYPE, AIModelType.GEMMA_2B.name)
        return AIModelType.valueOf(type ?: AIModelType.GEMMA_2B.name)
    }
    
    fun setAIModelType(type: AIModelType) {
        aiPrefs.edit().putString(KEY_AI_MODEL_TYPE, type.name).apply()
        updateSettingsState { it.copy(aiModelType = type) }
    }
    
    fun getAIModelSize(): String = aiPrefs.getString(KEY_AI_MODEL_SIZE, "2B") ?: "2B"
    
    fun setAIModelSize(size: String) {
        aiPrefs.edit().putString(KEY_AI_MODEL_SIZE, size).apply()
    }
    
    fun getInferenceMode(): InferenceMode {
        val mode = aiPrefs.getString(KEY_INFERENCE_MODE, InferenceMode.LOCAL_ONLY.name)
        return InferenceMode.valueOf(mode ?: InferenceMode.LOCAL_ONLY.name)
    }
    
    fun setInferenceMode(mode: InferenceMode) {
        aiPrefs.edit().putString(KEY_INFERENCE_MODE, mode.name).apply()
    }
    
    fun getResponseStyle(): ResponseStyle {
        val style = aiPrefs.getString(KEY_RESPONSE_STYLE, ResponseStyle.BALANCED.name)
        return ResponseStyle.valueOf(style ?: ResponseStyle.BALANCED.name)
    }
    
    fun setResponseStyle(style: ResponseStyle) {
        aiPrefs.edit().putString(KEY_RESPONSE_STYLE, style.name).apply()
        updateSettingsState { it.copy(responseStyle = style) }
    }
    
    fun getContextMemorySize(): Int = aiPrefs.getInt(KEY_CONTEXT_MEMORY, 10)
    
    fun setContextMemorySize(size: Int) {
        aiPrefs.edit().putInt(KEY_CONTEXT_MEMORY, size).apply()
    }
    
    fun isVoiceEnabled(): Boolean = aiPrefs.getBoolean(KEY_VOICE_ENABLED, false)
    
    fun setVoiceEnabled(enabled: Boolean) {
        aiPrefs.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
        updateSettingsState { it.copy(voiceEnabled = enabled) }
    }
    
    fun getTTSSpeed(): Float = aiPrefs.getFloat(KEY_TTS_SPEED, 1.0f)
    
    fun setTTSSpeed(speed: Float) {
        aiPrefs.edit().putFloat(KEY_TTS_SPEED, speed).apply()
    }
    
    fun getTTSPitch(): Float = aiPrefs.getFloat(KEY_TTS_PITCH, 1.0f)
    
    fun setTTSPitch(pitch: Float) {
        aiPrefs.edit().putFloat(KEY_TTS_PITCH, pitch).apply()
    }
    
    fun getWakeWord(): String = aiPrefs.getString(KEY_WAKE_WORD, "Hey AI") ?: "Hey AI"
    
    fun setWakeWord(wakeWord: String) {
        aiPrefs.edit().putString(KEY_WAKE_WORD, wakeWord).apply()
    }
    
    fun areAISuggestionsEnabled(): Boolean = aiPrefs.getBoolean(KEY_AI_SUGGESTIONS, true)
    
    fun setAISuggestionsEnabled(enabled: Boolean) {
        aiPrefs.edit().putBoolean(KEY_AI_SUGGESTIONS, enabled).apply()
    }
    
    // Privacy Settings
    
    fun isDataCollectionEnabled(): Boolean = privacyPrefs.getBoolean(KEY_DATA_COLLECTION, false)
    
    fun setDataCollectionEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_DATA_COLLECTION, enabled).apply()
        updateSettingsState { it.copy(dataCollectionEnabled = enabled) }
    }
    
    fun isAnalyticsEnabled(): Boolean = privacyPrefs.getBoolean(KEY_ANALYTICS_ENABLED, false)
    
    fun setAnalyticsEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_ANALYTICS_ENABLED, enabled).apply()
    }
    
    fun isLocationAccessEnabled(): Boolean = privacyPrefs.getBoolean(KEY_LOCATION_ACCESS, false)
    
    fun setLocationAccessEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_LOCATION_ACCESS, enabled).apply()
    }
    
    fun isContactsAccessEnabled(): Boolean = privacyPrefs.getBoolean(KEY_CONTACTS_ACCESS, false)
    
    fun setContactsAccessEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_CONTACTS_ACCESS, enabled).apply()
    }
    
    fun isSMSAccessEnabled(): Boolean = privacyPrefs.getBoolean(KEY_SMS_ACCESS, false)
    
    fun setSMSAccessEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_SMS_ACCESS, enabled).apply()
    }
    
    fun isNotificationAccessEnabled(): Boolean = privacyPrefs.getBoolean(KEY_NOTIFICATION_ACCESS, false)
    
    fun setNotificationAccessEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_NOTIFICATION_ACCESS, enabled).apply()
    }
    
    fun isUsageStatsAccessEnabled(): Boolean = privacyPrefs.getBoolean(KEY_USAGE_STATS_ACCESS, false)
    
    fun setUsageStatsAccessEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_USAGE_STATS_ACCESS, enabled).apply()
    }
    
    fun getDataRetentionDays(): Int = privacyPrefs.getInt(KEY_DATA_RETENTION_DAYS, 30)
    
    fun setDataRetentionDays(days: Int) {
        privacyPrefs.edit().putInt(KEY_DATA_RETENTION_DAYS, days).apply()
    }
    
    fun isAutoDeleteConversationsEnabled(): Boolean = privacyPrefs.getBoolean(KEY_AUTO_DELETE_CONVERSATIONS, false)
    
    fun setAutoDeleteConversationsEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_AUTO_DELETE_CONVERSATIONS, enabled).apply()
    }
    
    fun isSecureModeEnabled(): Boolean = privacyPrefs.getBoolean(KEY_SECURE_MODE, false)
    
    fun setSecureModeEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean(KEY_SECURE_MODE, enabled).apply()
    }
    
    // Performance Settings
    
    fun getPerformanceMode(): PerformanceMode {
        val mode = performancePrefs.getString(KEY_PERFORMANCE_MODE, PerformanceMode.BALANCED.name)
        return PerformanceMode.valueOf(mode ?: PerformanceMode.BALANCED.name)
    }
    
    fun setPerformanceMode(mode: PerformanceMode) {
        performancePrefs.edit().putString(KEY_PERFORMANCE_MODE, mode.name).apply()
        updateSettingsState { it.copy(performanceMode = mode) }
    }
    
    fun isBackgroundProcessingEnabled(): Boolean = performancePrefs.getBoolean(KEY_BACKGROUND_PROCESSING, true)
    
    fun setBackgroundProcessingEnabled(enabled: Boolean) {
        performancePrefs.edit().putBoolean(KEY_BACKGROUND_PROCESSING, enabled).apply()
    }
    
    fun getCacheSize(): Int = performancePrefs.getInt(KEY_CACHE_SIZE, 100) // MB
    
    fun setCacheSize(sizeMB: Int) {
        performancePrefs.edit().putInt(KEY_CACHE_SIZE, sizeMB).apply()
    }
    
    fun isMemoryOptimizationEnabled(): Boolean = performancePrefs.getBoolean(KEY_MEMORY_OPTIMIZATION, true)
    
    fun setMemoryOptimizationEnabled(enabled: Boolean) {
        performancePrefs.edit().putBoolean(KEY_MEMORY_OPTIMIZATION, enabled).apply()
    }
    
    fun isBatteryOptimizationEnabled(): Boolean = performancePrefs.getBoolean(KEY_BATTERY_OPTIMIZATION, true)
    
    fun setBatteryOptimizationEnabled(enabled: Boolean) {
        performancePrefs.edit().putBoolean(KEY_BATTERY_OPTIMIZATION, enabled).apply()
    }
    
    fun isNetworkOptimizationEnabled(): Boolean = performancePrefs.getBoolean(KEY_NETWORK_OPTIMIZATION, true)
    
    fun setNetworkOptimizationEnabled(enabled: Boolean) {
        performancePrefs.edit().putBoolean(KEY_NETWORK_OPTIMIZATION, enabled).apply()
    }
    
    fun isDatabaseOptimizationEnabled(): Boolean = performancePrefs.getBoolean(KEY_DATABASE_OPTIMIZATION, true)
    
    fun setDatabaseOptimizationEnabled(enabled: Boolean) {
        performancePrefs.edit().putBoolean(KEY_DATABASE_OPTIMIZATION, enabled).apply()
    }
    
    fun getMaxConversationHistory(): Int = performancePrefs.getInt(KEY_MAX_CONVERSATION_HISTORY, 1000)
    
    fun setMaxConversationHistory(maxHistory: Int) {
        performancePrefs.edit().putInt(KEY_MAX_CONVERSATION_HISTORY, maxHistory).apply()
    }
    
    // Security Settings (stored in encrypted preferences)
    
    fun isEncryptionEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_ENCRYPTION_ENABLED, true)
    
    fun setEncryptionEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_ENCRYPTION_ENABLED, enabled).apply()
        updateSettingsState { it.copy(encryptionEnabled = enabled) }
    }
    
    fun isBiometricAuthEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_BIOMETRIC_AUTH, false)
    
    fun setBiometricAuthEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BIOMETRIC_AUTH, enabled).apply()
        updateSettingsState { it.copy(biometricAuthEnabled = enabled) }
    }
    
    fun isAppLockEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_APP_LOCK, false)
    
    fun setAppLockEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()
    }
    
    fun getAutoLockTimeout(): Long = encryptedPrefs.getLong(KEY_AUTO_LOCK_TIMEOUT, 5 * 60 * 1000) // 5 minutes
    
    fun setAutoLockTimeout(timeoutMs: Long) {
        encryptedPrefs.edit().putLong(KEY_AUTO_LOCK_TIMEOUT, timeoutMs).apply()
    }
    
    fun isSecureKeyboardEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_SECURE_KEYBOARD, false)
    
    fun setSecureKeyboardEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SECURE_KEYBOARD, enabled).apply()
    }
    
    fun isScreenshotProtectionEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_SCREENSHOT_PROTECTION, false)
    
    fun setScreenshotProtectionEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SCREENSHOT_PROTECTION, enabled).apply()
    }
    
    fun isIncognitoModeEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_INCOGNITO_MODE, false)
    
    fun setIncognitoModeEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_INCOGNITO_MODE, enabled).apply()
    }
    
    // Utility methods
    
    private fun updateSettingsState(update: (SettingsState) -> SettingsState) {
        _settingsState.value = update(_settingsState.value)
    }
    
    private fun createEncryptedSharedPreferences(fileName: String): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
                
            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        } catch (e: IOException) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        generalPrefs.edit().clear().apply()
        aiPrefs.edit().clear().apply()
        privacyPrefs.edit().clear().apply()
        performancePrefs.edit().clear().apply()
        encryptedPrefs.edit().clear().apply()
        
        loadCurrentSettings()
    }
    
    /**
     * Export settings for backup
     */
    fun exportSettings(): Map<String, Any> {
        val settings = mutableMapOf<String, Any>()
        
        // Export non-sensitive settings only
        generalPrefs.all.forEach { (key, value) ->
            if (value != null) settings["general_$key"] = value
        }
        
        aiPrefs.all.forEach { (key, value) ->
            if (value != null) settings["ai_$key"] = value
        }
        
        privacyPrefs.all.forEach { (key, value) ->
            if (value != null) settings["privacy_$key"] = value
        }
        
        performancePrefs.all.forEach { (key, value) ->
            if (value != null) settings["performance_$key"] = value
        }
        
        return settings
    }
    
    /**
     * Import settings from backup
     */
    fun importSettings(settings: Map<String, Any>) {
        val generalEditor = generalPrefs.edit()
        val aiEditor = aiPrefs.edit()
        val privacyEditor = privacyPrefs.edit()
        val performanceEditor = performancePrefs.edit()
        
        settings.forEach { (key, value) ->
            when {
                key.startsWith("general_") -> {
                    val realKey = key.removePrefix("general_")
                    when (value) {
                        is Boolean -> generalEditor.putBoolean(realKey, value)
                        is String -> generalEditor.putString(realKey, value)
                        is Int -> generalEditor.putInt(realKey, value)
                        is Long -> generalEditor.putLong(realKey, value)
                        is Float -> generalEditor.putFloat(realKey, value)
                    }
                }
                key.startsWith("ai_") -> {
                    val realKey = key.removePrefix("ai_")
                    when (value) {
                        is Boolean -> aiEditor.putBoolean(realKey, value)
                        is String -> aiEditor.putString(realKey, value)
                        is Int -> aiEditor.putInt(realKey, value)
                        is Long -> aiEditor.putLong(realKey, value)
                        is Float -> aiEditor.putFloat(realKey, value)
                    }
                }
                key.startsWith("privacy_") -> {
                    val realKey = key.removePrefix("privacy_")
                    when (value) {
                        is Boolean -> privacyEditor.putBoolean(realKey, value)
                        is String -> privacyEditor.putString(realKey, value)
                        is Int -> privacyEditor.putInt(realKey, value)
                        is Long -> privacyEditor.putLong(realKey, value)
                        is Float -> privacyEditor.putFloat(realKey, value)
                    }
                }
                key.startsWith("performance_") -> {
                    val realKey = key.removePrefix("performance_")
                    when (value) {
                        is Boolean -> performanceEditor.putBoolean(realKey, value)
                        is String -> performanceEditor.putString(realKey, value)
                        is Int -> performanceEditor.putInt(realKey, value)
                        is Long -> performanceEditor.putLong(realKey, value)
                        is Float -> performanceEditor.putFloat(realKey, value)
                    }
                }
            }
        }
        
        generalEditor.apply()
        aiEditor.apply()
        privacyEditor.apply()
        performanceEditor.apply()
        
        loadCurrentSettings()
    }
}
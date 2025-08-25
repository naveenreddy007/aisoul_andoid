package com.aisoul.privateassistant.core.demo

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Manages demo mode functionality for testing and development.
 * Provides simulated AI responses when real models are not available.
 */
class DemoModeManager private constructor(private val context: Context) {
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("demo_mode", Context.MODE_PRIVATE)
    }
    
    /**
     * Whether demo mode is currently enabled
     */
    var isDemoModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEMO_MODE_ENABLED, true) // Default to true for development
        set(value) {
            prefs.edit().putBoolean(KEY_DEMO_MODE_ENABLED, value).apply()
        }
    
    /**
     * Generate a demo response for the given user input
     */
    suspend fun generateDemoResponse(userInput: String): String {
        // Simulate AI processing time
        val processingTime = Random.nextLong(800, 2000)
        delay(processingTime)
        
        return when {
            userInput.lowercase().contains("hello") || userInput.lowercase().contains("hi") -> {
                "Hello! I'm your AI Soul assistant running in demo mode. ${getDemoContext()}"
            }
            userInput.lowercase().contains("help") -> {
                "I can help you with various tasks! In demo mode, I'm simulating responses. ${getDemoContext()}"
            }
            userInput.lowercase().contains("weather") -> {
                "Demo Mode: I would check the weather for you, but I'm currently simulating responses. ${getDemoContext()}"
            }
            userInput.lowercase().contains("time") -> {
                "Demo Mode: The current time is ${System.currentTimeMillis()}. ${getDemoContext()}"
            }
            userInput.lowercase().contains("notification") -> {
                "Demo Mode: I can analyze notifications when real AI models are installed. ${getDemoContext()}"
            }
            userInput.lowercase().contains("sms") || userInput.lowercase().contains("message") -> {
                "Demo Mode: I can help with SMS analysis and responses when connected to real AI. ${getDemoContext()}"
            }
            userInput.length > 50 -> {
                "Demo Mode: That's quite a detailed message! I'd provide a comprehensive response with real AI models. ${getDemoContext()}"
            }
            userInput.contains("?") -> {
                "Demo Mode: That's a great question! With real AI models, I'd analyze and provide detailed answers. ${getDemoContext()}"
            }
            else -> {
                "Demo Mode: I received your message '$userInput'. ${getDemoContext()}"
            }
        }
    }
    
    /**
     * Get demo context information
     */
    private fun getDemoContext(): String {
        return "Install an AI model from the Models tab to get real, privacy-focused responses processed locally on your device!"
    }
    
    /**
     * Simulate notification analysis
     */
    suspend fun simulateNotificationAnalysis(notificationContent: String): String {
        delay(Random.nextLong(500, 1200))
        return "Demo: Analyzed notification - '$notificationContent'. Real analysis would provide insights about urgency, sentiment, and suggested actions."
    }
    
    /**
     * Simulate SMS analysis
     */
    suspend fun simulateSMSAnalysis(smsContent: String): String {
        delay(Random.nextLong(600, 1500))
        return "Demo: SMS analysis - '$smsContent'. Real AI would detect spam, extract important info, and suggest responses."
    }
    
    /**
     * Get demo statistics
     */
    fun getDemoStats(): Map<String, Any> {
        return mapOf(
            "mode" to "demo",
            "responses_generated" to Random.nextInt(10, 50),
            "avg_response_time" to "${Random.nextInt(800, 2000)}ms",
            "simulated_accuracy" to "N/A - Demo Mode",
            "local_processing" to true,
            "privacy_status" to "Full Privacy - No Data Sent"
        )
    }
    
    /**
     * Check if real AI models are available
     */
    fun hasRealModelsAvailable(): Boolean {
        // In real implementation, this would check for downloaded models
        return false
    }
    
    /**
     * Get suggested demo interactions
     */
    fun getDemoSuggestions(): List<String> {
        return listOf(
            "Hello, how are you?",
            "What's the weather like?",
            "Help me with notifications",
            "Analyze this message",
            "What can you do?",
            "Tell me about privacy"
        )
    }
    
    companion object {
        private const val KEY_DEMO_MODE_ENABLED = "demo_mode_enabled"
        
        @Volatile
        private var INSTANCE: DemoModeManager? = null
        
        /**
         * Get singleton instance of DemoModeManager
         */
        fun getInstance(context: Context): DemoModeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DemoModeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
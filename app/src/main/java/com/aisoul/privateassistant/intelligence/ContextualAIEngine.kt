package com.aisoul.privateassistant.intelligence

import android.content.Context
import com.aisoul.privateassistant.ai.AIInferenceEngine
import com.aisoul.privateassistant.data.database.AppDatabase
import com.aisoul.privateassistant.data.entities.ContextualMemory
import com.aisoul.privateassistant.data.entities.UserInteraction
import com.aisoul.privateassistant.data.entities.ContextualResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * Context-Aware AI Response Generation System
 * Generates intelligent responses by considering user context, history, and patterns
 */
class ContextualAIEngine private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: ContextualAIEngine? = null
        
        fun getInstance(context: Context): ContextualAIEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContextualAIEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val aiInferenceEngine = AIInferenceEngine.getInstance(context)
    private val notificationAnalyzer = NotificationAnalyzer.getInstance(context)
    private val smsIntegrationManager = SmsIntegrationManager.getInstance(context)
    private val appUsageAnalyzer = AppUsageAnalyzer.getInstance(context)
    
    // State management
    private val _responseState = MutableStateFlow(ResponseState.IDLE)
    val responseState: StateFlow<ResponseState> = _responseState.asStateFlow()
    
    private val _contextualMemory = MutableStateFlow<List<ContextualMemory>>(emptyList())
    val contextualMemory: StateFlow<List<ContextualMemory>> = _contextualMemory.asStateFlow()
    
    enum class ResponseState {
        IDLE,
        ANALYZING_CONTEXT,
        GENERATING_RESPONSE,
        COMPLETE,
        ERROR
    }
    
    enum class ContextType {
        TEMPORAL,      // Time-based context
        LOCATION,      // Location-based context (if available)
        ACTIVITY,      // Current activity context
        COMMUNICATION, // Recent communications
        NOTIFICATION,  // Recent notifications
        USAGE_PATTERN, // App usage patterns
        EMOTIONAL,     // Emotional state inference
        TASK,          // Current or recent tasks
        SOCIAL,        // Social interactions
        PREFERENCE     // User preferences and history
    }
    
    data class UserContext(
        val timestamp: Long,
        val timeOfDay: TimeOfDay,
        val dayOfWeek: DayOfWeek,
        val recentActivity: List<String>,
        val recentNotifications: List<String>,
        val recentMessages: List<String>,
        val currentMood: MoodState?,
        val activeApps: List<String>,
        val locationContext: String?,
        val taskContext: List<String>,
        val userPreferences: Map<String, String>
    )
    
    enum class TimeOfDay {
        EARLY_MORNING, // 5-8 AM
        MORNING,       // 8-12 PM
        AFTERNOON,     // 12-5 PM
        EVENING,       // 5-9 PM
        NIGHT,         // 9 PM-12 AM
        LATE_NIGHT     // 12-5 AM
    }
    
    enum class DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
    
    enum class MoodState {
        ENERGETIC,
        FOCUSED,
        RELAXED,
        STRESSED,
        BUSY,
        HAPPY,
        NEUTRAL,
        TIRED
    }
    
    data class ContextualPrompt(
        val userQuery: String,
        val contextualInformation: String,
        val conversationHistory: List<String>,
        val personalizedInstructions: String,
        val responseStyle: ResponseStyle
    )
    
    enum class ResponseStyle {
        HELPFUL_ASSISTANT,
        CASUAL_FRIEND,
        PROFESSIONAL_ADVISOR,
        EMPATHETIC_LISTENER,
        MOTIVATIONAL_COACH,
        EFFICIENT_ORGANIZER
    }
    
    /**
     * Generate context-aware AI response
     */
    suspend fun generateContextualResponse(
        userQuery: String,
        conversationHistory: List<String> = emptyList()
    ): AIInferenceEngine.InferenceResult {
        _responseState.value = ResponseState.ANALYZING_CONTEXT
        
        try {
            // Gather comprehensive user context
            val userContext = gatherUserContext()
            
            // Determine appropriate response style
            val responseStyle = determineResponseStyle(userQuery, userContext)
            
            // Build contextual prompt
            val contextualPrompt = buildContextualPrompt(
                userQuery, 
                userContext, 
                conversationHistory, 
                responseStyle
            )
            
            _responseState.value = ResponseState.GENERATING_RESPONSE
            
            // Generate AI response with context
            val response = aiInferenceEngine.generateResponse(
                input = contextualPrompt.userQuery,
                conversationHistory = contextualPrompt.conversationHistory,
                systemPrompt = buildSystemPrompt(contextualPrompt)
            )
            
            // Store interaction for future context
            storeUserInteraction(userQuery, response, userContext)
            
            // Update contextual memory
            updateContextualMemory(userQuery, userContext, response)
            
            _responseState.value = ResponseState.COMPLETE
            return response
            
        } catch (e: Exception) {
            _responseState.value = ResponseState.ERROR
            throw e
        }
    }
    
    /**
     * Gather comprehensive user context
     */
    private suspend fun gatherUserContext(): UserContext {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        // Time context
        val timeOfDay = determineTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY))
        val dayOfWeek = DayOfWeek.values()[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        
        // Recent activity context
        val recentNotificationInsights = notificationAnalyzer.getContextualInsights("")
        val recentUsageInsights = appUsageAnalyzer.getContextualUsageInsights()
        
        // Recent communications (if permission granted)
        val recentMessages = try {
            // This would get recent SMS context if available
            listOf("Recent message context available")
        } catch (e: Exception) {
            emptyList()
        }
        
        // Mood inference from recent activity
        val currentMood = inferMoodFromActivity()
        
        // Active apps context (simplified)
        val activeApps = listOf("AI Soul Assistant")
        
        // Task context from recent interactions
        val taskContext = getRecentTaskContext()
        
        // User preferences
        val userPreferences = getUserPreferences()
        
        return UserContext(
            timestamp = currentTime,
            timeOfDay = timeOfDay,
            dayOfWeek = dayOfWeek,
            recentActivity = recentUsageInsights,
            recentNotifications = recentNotificationInsights,
            recentMessages = recentMessages,
            currentMood = currentMood,
            activeApps = activeApps,
            locationContext = null, // Could be added with location permission
            taskContext = taskContext,
            userPreferences = userPreferences
        )
    }
    
    /**
     * Determine appropriate response style based on context
     */
    private fun determineResponseStyle(userQuery: String, userContext: UserContext): ResponseStyle {
        val query = userQuery.lowercase()
        
        return when {
            // Time-based style adjustments
            userContext.timeOfDay == TimeOfDay.EARLY_MORNING || userContext.timeOfDay == TimeOfDay.LATE_NIGHT -> {
                ResponseStyle.EMPATHETIC_LISTENER
            }
            
            // Mood-based adjustments
            userContext.currentMood == MoodState.STRESSED || userContext.currentMood == MoodState.BUSY -> {
                ResponseStyle.EFFICIENT_ORGANIZER
            }
            userContext.currentMood == MoodState.TIRED -> {
                ResponseStyle.EMPATHETIC_LISTENER
            }
            
            // Query content analysis
            query.contains("help") || query.contains("how") || query.contains("what") -> {
                ResponseStyle.HELPFUL_ASSISTANT
            }
            query.contains("schedule") || query.contains("organize") || query.contains("plan") -> {
                ResponseStyle.EFFICIENT_ORGANIZER
            }
            query.contains("feel") || query.contains("stress") || query.contains("worry") -> {
                ResponseStyle.EMPATHETIC_LISTENER
            }
            query.contains("motivat") || query.contains("encourage") || query.contains("goal") -> {
                ResponseStyle.MOTIVATIONAL_COACH
            }
            query.contains("work") || query.contains("business") || query.contains("professional") -> {
                ResponseStyle.PROFESSIONAL_ADVISOR
            }
            
            // Day-based adjustments
            userContext.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> {
                ResponseStyle.CASUAL_FRIEND
            }
            
            else -> ResponseStyle.HELPFUL_ASSISTANT
        }
    }
    
    /**
     * Build contextual prompt with all relevant information
     */
    private fun buildContextualPrompt(
        userQuery: String,
        userContext: UserContext,
        conversationHistory: List<String>,
        responseStyle: ResponseStyle
    ): ContextualPrompt {
        // Build contextual information string
        val contextualInfo = buildString {
            append("Current Context:\n")
            append("- Time: ${userContext.timeOfDay.name.lowercase().replace('_', ' ')} on ${userContext.dayOfWeek.name.lowercase()}\n")
            
            if (userContext.currentMood != null) {
                append("- User mood: ${userContext.currentMood.name.lowercase()}\n")
            }
            
            if (userContext.recentActivity.isNotEmpty()) {
                append("- Recent activity: ${userContext.recentActivity.joinToString("; ")}\n")
            }
            
            if (userContext.recentNotifications.isNotEmpty()) {
                append("- Recent notifications: ${userContext.recentNotifications.joinToString("; ")}\n")
            }
            
            if (userContext.taskContext.isNotEmpty()) {
                append("- Current tasks: ${userContext.taskContext.joinToString("; ")}\n")
            }
            
            append("- Response style: ${responseStyle.name.lowercase().replace('_', ' ')}")
        }
        
        // Build personalized instructions
        val personalizedInstructions = buildPersonalizedInstructions(userContext, responseStyle)
        
        return ContextualPrompt(
            userQuery = userQuery,
            contextualInformation = contextualInfo,
            conversationHistory = conversationHistory,
            personalizedInstructions = personalizedInstructions,
            responseStyle = responseStyle
        )
    }
    
    /**
     * Build system prompt with contextual information
     */
    private fun buildSystemPrompt(contextualPrompt: ContextualPrompt): String {
        return buildString {
            append("You are an AI personal assistant with access to the user's context. ")
            append("Respond in a ${contextualPrompt.responseStyle.name.lowercase().replace('_', ' ')} manner.\n\n")
            
            append("${contextualPrompt.contextualInformation}\n\n")
            
            append("Personalized Instructions:\n")
            append("${contextualPrompt.personalizedInstructions}\n\n")
            
            append("Please provide a helpful, contextually appropriate response that considers:")
            append("1. The current time and day context\n")
            append("2. The user's recent activity and mood\n")
            append("3. Any relevant recent notifications or tasks\n")
            append("4. The conversation history\n")
            append("5. The appropriate response style for the situation\n\n")
            
            append("Keep responses concise but thoughtful, and offer actionable suggestions when appropriate.")
        }
    }
    
    /**
     * Build personalized instructions based on context
     */
    private fun buildPersonalizedInstructions(userContext: UserContext, responseStyle: ResponseStyle): String {
        return buildString {
            when (responseStyle) {
                ResponseStyle.HELPFUL_ASSISTANT -> {
                    append("Provide clear, helpful information and suggest practical next steps.")
                }
                ResponseStyle.CASUAL_FRIEND -> {
                    append("Be warm, friendly, and conversational. Use a relaxed tone.")
                }
                ResponseStyle.PROFESSIONAL_ADVISOR -> {
                    append("Maintain a professional tone with structured, actionable advice.")
                }
                ResponseStyle.EMPATHETIC_LISTENER -> {
                    append("Show understanding and empathy. Acknowledge feelings and provide gentle support.")
                }
                ResponseStyle.MOTIVATIONAL_COACH -> {
                    append("Be encouraging and positive. Focus on goals and achievements.")
                }
                ResponseStyle.EFFICIENT_ORGANIZER -> {
                    append("Be concise and action-oriented. Focus on solutions and organization.")
                }
            }
            
            // Add time-specific instructions
            when (userContext.timeOfDay) {
                TimeOfDay.EARLY_MORNING -> append(" Consider that it's early morning - be gentle and gradually energizing.")
                TimeOfDay.MORNING -> append(" It's morning time - be upbeat and help set a positive tone for the day.")
                TimeOfDay.AFTERNOON -> append(" It's afternoon - be productive and focused on getting things done.")
                TimeOfDay.EVENING -> append(" It's evening - help wind down and reflect on the day.")
                TimeOfDay.NIGHT -> append(" It's nighttime - be calming and supportive.")
                TimeOfDay.LATE_NIGHT -> append(" It's late at night - be especially empathetic and gentle.")
            }
            
            // Add mood-specific instructions
            userContext.currentMood?.let { mood ->
                when (mood) {
                    MoodState.STRESSED, MoodState.BUSY -> append(" The user seems stressed/busy - be calming and efficient.")
                    MoodState.TIRED -> append(" The user seems tired - be gentle and supportive.")
                    MoodState.ENERGETIC -> append(" The user seems energetic - match their energy level.")
                    MoodState.FOCUSED -> append(" The user seems focused - be direct and helpful.")
                    MoodState.HAPPY -> append(" The user seems happy - maintain the positive mood.")
                    else -> append(" Be aware of the user's current emotional state.")
                }
            }
        }
    }
    
    /**
     * Store user interaction for future context
     */
    private suspend fun storeUserInteraction(
        userQuery: String,
        aiResponse: AIInferenceEngine.InferenceResult,
        userContext: UserContext
    ) {
        val interaction = UserInteraction(
            id = 0,
            userQuery = userQuery,
            aiResponse = when (aiResponse) {
                is AIInferenceEngine.InferenceResult.Success -> aiResponse.response
                else -> "Error generating response"
            },
            contextData = buildContextString(userContext),
            timestamp = System.currentTimeMillis(),
            responseStyle = determineResponseStyle(userQuery, userContext).name,
            userSatisfaction = null // Could be set later through feedback
        )
        
        database.contextualDao().insertUserInteraction(interaction)
    }
    
    /**
     * Update contextual memory with new information
     */
    private suspend fun updateContextualMemory(
        userQuery: String,
        userContext: UserContext,
        aiResponse: AIInferenceEngine.InferenceResult
    ) {
        // Extract key concepts from the query
        val concepts = extractKeyConcepts(userQuery)
        
        concepts.forEach { concept ->
            val existingMemory = database.contextualDao().getContextualMemoryByConcept(concept)
            
            if (existingMemory != null) {
                // Update existing memory
                val updatedMemory = existingMemory.copy(
                    frequency = existingMemory.frequency + 1,
                    lastAccessed = System.currentTimeMillis(),
                    contextData = updateContextData(existingMemory.contextData, userContext)
                )
                database.contextualDao().updateContextualMemory(updatedMemory)
            } else {
                // Create new memory
                val newMemory = ContextualMemory(
                    id = 0,
                    concept = concept,
                    contextType = ContextType.PREFERENCE.name,
                    contextData = buildContextString(userContext),
                    frequency = 1,
                    lastAccessed = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis()
                )
                database.contextualDao().insertContextualMemory(newMemory)
            }
        }
        
        // Update state
        _contextualMemory.value = database.contextualDao().getAllContextualMemories()
    }
    
    /**
     * Utility methods for context analysis
     */
    private fun determineTimeOfDay(hour: Int): TimeOfDay {
        return when (hour) {
            in 5..7 -> TimeOfDay.EARLY_MORNING
            in 8..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..20 -> TimeOfDay.EVENING
            in 21..23 -> TimeOfDay.NIGHT
            else -> TimeOfDay.LATE_NIGHT
        }
    }
    
    private suspend fun inferMoodFromActivity(): MoodState? {
        // Simplified mood inference based on recent app usage
        val recentUsage = appUsageAnalyzer.getContextualUsageInsights()
        
        return when {
            recentUsage.any { it.contains("high-priority") || it.contains("urgent") } -> MoodState.BUSY
            recentUsage.any { it.contains("productivity") && it.contains("high") } -> MoodState.FOCUSED
            recentUsage.any { it.contains("entertainment") || it.contains("social") } -> MoodState.RELAXED
            else -> MoodState.NEUTRAL
        }
    }
    
    private suspend fun getRecentTaskContext(): List<String> {
        // Get recent interactions that suggest tasks or goals
        val recentInteractions = database.contextualDao().getRecentUserInteractions(5)
        
        return recentInteractions.mapNotNull { interaction ->
            if (interaction.userQuery.contains("task") || 
                interaction.userQuery.contains("todo") || 
                interaction.userQuery.contains("remind")) {
                interaction.userQuery
            } else null
        }
    }
    
    private fun getUserPreferences(): Map<String, String> {
        // This would load user preferences from settings or previous interactions
        return mapOf(
            "response_length" to "concise",
            "formality" to "casual",
            "time_format" to "12_hour"
        )
    }
    
    private fun extractKeyConcepts(userQuery: String): List<String> {
        // Simple keyword extraction
        return userQuery.lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 3 }
            .filter { !isStopWord(it) }
            .distinct()
    }
    
    private fun buildContextString(userContext: UserContext): String {
        return buildString {
            append("time:${userContext.timeOfDay.name};")
            append("day:${userContext.dayOfWeek.name};")
            userContext.currentMood?.let { append("mood:${it.name};") }
            if (userContext.recentActivity.isNotEmpty()) {
                append("activity:${userContext.recentActivity.joinToString(",")};")
            }
        }
    }
    
    private fun updateContextData(existingData: String, newContext: UserContext): String {
        // Merge existing context data with new context
        // This is a simplified implementation
        return "${existingData}|${buildContextString(newContext)}"
    }
    
    private fun isStopWord(word: String): Boolean {
        val stopWords = setOf("the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from", "this", "that", "these", "those", "you", "your", "have", "has", "had", "will", "would", "could", "should", "can", "may", "might", "must", "shall", "i", "me", "my", "we", "us", "our", "they", "them", "their")
        return stopWords.contains(word.lowercase())
    }
    
    /**
     * Get personalized suggestions based on context
     */
    suspend fun getPersonalizedSuggestions(): List<String> {
        val userContext = gatherUserContext()
        val suggestions = mutableListOf<String>()
        
        // Time-based suggestions
        when (userContext.timeOfDay) {
            TimeOfDay.MORNING -> {
                suggestions.add("Good morning! Ready to plan your day?")
                suggestions.add("Check your schedule for today")
                suggestions.add("Review your daily goals")
            }
            TimeOfDay.AFTERNOON -> {
                suggestions.add("How's your day going?")
                suggestions.add("Need help staying productive?")
                suggestions.add("Time for a quick break?")
            }
            TimeOfDay.EVENING -> {
                suggestions.add("How was your day?")
                suggestions.add("Want to review what you accomplished?")
                suggestions.add("Plan for tomorrow?")
            }
            else -> {
                suggestions.add("How can I help you today?")
            }
        }
        
        // Activity-based suggestions
        if (userContext.recentActivity.any { it.contains("productivity") }) {
            suggestions.add("Keep up the good work on your productivity!")
            suggestions.add("Want tips to maintain your focus?")
        }
        
        if (userContext.recentNotifications.isNotEmpty()) {
            suggestions.add("I notice you have some notifications - need help prioritizing?")
        }
        
        return suggestions.take(3)
    }
}
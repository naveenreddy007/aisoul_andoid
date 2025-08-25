package com.aisoul.privateassistant.intelligence

import android.content.Context
import com.aisoul.privateassistant.data.database.AppDatabase
import com.aisoul.privateassistant.data.entities.SearchSession
import com.aisoul.privateassistant.data.entities.SearchAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*

/**
 * Semantic Search Manager
 * Provides high-level semantic search capabilities across all app data
 */
class SemanticSearchManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: SemanticSearchManager? = null
        
        fun getInstance(context: Context): SemanticSearchManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SemanticSearchManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val vectorDatabase = VectorDatabase.getInstance(context)
    private val contextualAI = ContextualAIEngine.getInstance(context)
    private val notificationAnalyzer = NotificationAnalyzer.getInstance(context)
    private val smsIntegration = SmsIntegrationManager.getInstance(context)
    private val appUsageAnalyzer = AppUsageAnalyzer.getInstance(context)
    
    // State management
    private val _searchState = MutableStateFlow(SearchState.IDLE)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    private val _searchResults = MutableStateFlow<SearchResults>(SearchResults.empty())
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()
    
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()
    
    enum class SearchState {
        IDLE,
        SEARCHING,
        PROCESSING,
        COMPLETE,
        ERROR
    }
    
    enum class SearchScope {
        ALL,
        CONVERSATIONS,
        NOTIFICATIONS,
        DOCUMENTS,
        APP_USAGE,
        CONTEXTUAL_MEMORY
    }
    
    data class SearchResults(
        val query: String,
        val totalResults: Int,
        val conversationResults: List<ConversationSearchResult>,
        val notificationResults: List<NotificationSearchResult>,
        val documentResults: List<DocumentSearchResult>,
        val usageResults: List<UsageSearchResult>,
        val contextualResults: List<ContextualSearchResult>,
        val executionTime: Long,
        val suggestions: List<String>
    ) {
        companion object {
            fun empty() = SearchResults("", 0, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 0L, emptyList())
        }
    }
    
    data class ConversationSearchResult(
        val messageId: String,
        val content: String,
        val timestamp: Long,
        val context: String,
        val score: Float
    )
    
    data class NotificationSearchResult(
        val notificationId: String,
        val title: String,
        val content: String,
        val appName: String,
        val timestamp: Long,
        val score: Float
    )
    
    data class DocumentSearchResult(
        val documentId: String,
        val title: String,
        val content: String,
        val source: String,
        val timestamp: Long,
        val score: Float
    )
    
    data class UsageSearchResult(
        val appName: String,
        val category: String,
        val usageTime: Long,
        val context: String,
        val score: Float
    )
    
    data class ContextualSearchResult(
        val concept: String,
        val context: String,
        val frequency: Int,
        val lastAccessed: Long,
        val score: Float
    )
    
    /**
     * Initialize semantic search system
     */
    suspend fun initialize(): Boolean {
        try {
            // Initialize vector database
            vectorDatabase.initialize()
            
            // Index existing data
            indexExistingData()
            
            // Generate initial search suggestions
            generateSearchSuggestions()
            
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Perform comprehensive semantic search
     */
    suspend fun search(
        query: String,
        scope: SearchScope = SearchScope.ALL,
        maxResults: Int = 20
    ): SearchResults {
        _searchState.value = SearchState.SEARCHING
        val startTime = System.currentTimeMillis()
        
        try {
            val results = when (scope) {
                SearchScope.ALL -> searchAll(query, maxResults)
                SearchScope.CONVERSATIONS -> searchConversations(query, maxResults)
                SearchScope.NOTIFICATIONS -> searchNotifications(query, maxResults)
                SearchScope.DOCUMENTS -> searchDocuments(query, maxResults)
                SearchScope.APP_USAGE -> searchAppUsage(query, maxResults)
                SearchScope.CONTEXTUAL_MEMORY -> searchContextualMemory(query, maxResults)
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            val finalResults = results.copy(
                query = query,
                executionTime = executionTime
            )
            
            // Store search session for analytics
            storeSearchSession(query, scope, finalResults)
            
            // Update suggestions based on search
            updateSearchSuggestions(query)
            
            _searchResults.value = finalResults
            _searchState.value = SearchState.COMPLETE
            
            return finalResults
            
        } catch (e: Exception) {
            _searchState.value = SearchState.ERROR
            return SearchResults.empty()
        }
    }
    
    /**
     * Search across all data sources
     */
    private suspend fun searchAll(query: String, maxResults: Int): SearchResults = coroutineScope {
        val perScopeResults = maxResults / 5 // Distribute across 5 scopes
        
        // Search all scopes in parallel
        val conversationsDeferred = async { searchConversationsInternal(query, perScopeResults) }
        val notificationsDeferred = async { searchNotificationsInternal(query, perScopeResults) }
        val documentsDeferred = async { searchDocumentsInternal(query, perScopeResults) }
        val usageDeferred = async { searchAppUsageInternal(query, perScopeResults) }
        val contextualDeferred = async { searchContextualMemoryInternal(query, perScopeResults) }
        
        // Collect results
        val conversationResults = conversationsDeferred.await()
        val notificationResults = notificationsDeferred.await()
        val documentResults = documentsDeferred.await()
        val usageResults = usageDeferred.await()
        val contextualResults = contextualDeferred.await()
        
        val totalResults = conversationResults.size + notificationResults.size + 
                          documentResults.size + usageResults.size + contextualResults.size
        
        val suggestions = generateQuerySuggestions(query)
        
        SearchResults(
            query = query,
            totalResults = totalResults,
            conversationResults = conversationResults,
            notificationResults = notificationResults,
            documentResults = documentResults,
            usageResults = usageResults,
            contextualResults = contextualResults,
            executionTime = 0L,
            suggestions = suggestions
        )
    }
    
    /**
     * Search conversations
     */
    private suspend fun searchConversations(query: String, maxResults: Int): SearchResults {
        val results = searchConversationsInternal(query, maxResults)
        return SearchResults(
            query = query,
            totalResults = results.size,
            conversationResults = results,
            notificationResults = emptyList(),
            documentResults = emptyList(),
            usageResults = emptyList(),
            contextualResults = emptyList(),
            executionTime = 0L,
            suggestions = generateQuerySuggestions(query)
        )
    }
    
    private suspend fun searchConversationsInternal(query: String, maxResults: Int): List<ConversationSearchResult> {
        // Search chat messages using vector similarity
        val vectorResults = vectorDatabase.search(query, maxResults, threshold = 0.3f)
        
        return vectorResults.mapNotNull { result ->
            if (result.documentId.startsWith("chat_")) {
                ConversationSearchResult(
                    messageId = result.chunkId,
                    content = result.content,
                    timestamp = System.currentTimeMillis(), // Would be from actual message
                    context = result.metadata["context"] ?: "",
                    score = result.score
                )
            } else null
        }
    }
    
    /**
     * Search notifications
     */
    private suspend fun searchNotifications(query: String, maxResults: Int): SearchResults {
        val results = searchNotificationsInternal(query, maxResults)
        return SearchResults(
            query = query,
            totalResults = results.size,
            conversationResults = emptyList(),
            notificationResults = results,
            documentResults = emptyList(),
            usageResults = emptyList(),
            contextualResults = emptyList(),
            executionTime = 0L,
            suggestions = generateQuerySuggestions(query)
        )
    }
    
    private suspend fun searchNotificationsInternal(query: String, maxResults: Int): List<NotificationSearchResult> {
        // Search notification records
        val notifications = database.notificationDao().searchNotifications("%$query%", maxResults)
        
        return notifications.map { notification ->
            val score = calculateTextSimilarity(query, "${notification.title} ${notification.text}")
            NotificationSearchResult(
                notificationId = notification.id.toString(),
                title = notification.title,
                content = notification.text,
                appName = notification.packageName,
                timestamp = notification.timestamp,
                score = score
            )
        }
    }
    
    /**
     * Search documents
     */
    private suspend fun searchDocuments(query: String, maxResults: Int): SearchResults {
        val results = searchDocumentsInternal(query, maxResults)
        return SearchResults(
            query = query,
            totalResults = results.size,
            conversationResults = emptyList(),
            notificationResults = emptyList(),
            documentResults = results,
            usageResults = emptyList(),
            contextualResults = emptyList(),
            executionTime = 0L,
            suggestions = generateQuerySuggestions(query)
        )
    }
    
    private suspend fun searchDocumentsInternal(query: String, maxResults: Int): List<DocumentSearchResult> {
        // Search documents using vector database
        val vectorResults = vectorDatabase.search(query, maxResults, threshold = 0.4f)
        
        return vectorResults.filter { !it.documentId.startsWith("chat_") }
            .map { result ->
                DocumentSearchResult(
                    documentId = result.documentId,
                    title = result.metadata["title"] ?: result.documentId,
                    content = result.content,
                    source = result.metadata["source"] ?: "unknown",
                    timestamp = result.metadata["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
                    score = result.score
                )
            }
    }
    
    /**
     * Search app usage patterns
     */
    private suspend fun searchAppUsage(query: String, maxResults: Int): SearchResults {
        val results = searchAppUsageInternal(query, maxResults)
        return SearchResults(
            query = query,
            totalResults = results.size,
            conversationResults = emptyList(),
            notificationResults = emptyList(),
            documentResults = emptyList(),
            usageResults = results,
            contextualResults = emptyList(),
            executionTime = 0L,
            suggestions = generateQuerySuggestions(query)
        )
    }
    
    private suspend fun searchAppUsageInternal(query: String, maxResults: Int): List<UsageSearchResult> {
        // Search app usage records
        val usageRecords = database.appUsageDao().searchUsageRecords("%$query%", maxResults)
        
        return usageRecords.map { usage ->
            val score = calculateTextSimilarity(query, "${usage.appName} ${usage.category}")
            UsageSearchResult(
                appName = usage.appName,
                category = usage.category,
                usageTime = usage.totalTime,
                context = "Used for ${formatDuration(usage.totalTime)} on ${usage.date}",
                score = score
            )
        }
    }
    
    /**
     * Search contextual memory
     */
    private suspend fun searchContextualMemory(query: String, maxResults: Int): SearchResults {
        val results = searchContextualMemoryInternal(query, maxResults)
        return SearchResults(
            query = query,
            totalResults = results.size,
            conversationResults = emptyList(),
            notificationResults = emptyList(),
            documentResults = emptyList(),
            usageResults = emptyList(),
            contextualResults = results,
            executionTime = 0L,
            suggestions = generateQuerySuggestions(query)
        )
    }
    
    private suspend fun searchContextualMemoryInternal(query: String, maxResults: Int): List<ContextualSearchResult> {
        // Search contextual memories
        val memories = database.contextualDao().searchContextualMemories("%$query%", maxResults)
        
        return memories.map { memory ->
            val score = calculateTextSimilarity(query, "${memory.concept} ${memory.contextData}")
            ContextualSearchResult(
                concept = memory.concept,
                context = memory.contextData,
                frequency = memory.frequency,
                lastAccessed = memory.lastAccessed,
                score = score
            )
        }
    }
    
    /**
     * Get search suggestions based on user context
     */
    suspend fun getSearchSuggestions(): List<String> {
        return _searchSuggestions.value
    }
    
    /**
     * Get personalized search suggestions
     */
    suspend fun getPersonalizedSuggestions(context: String = ""): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Recent activity suggestions
        val recentUsage = appUsageAnalyzer.getContextualUsageInsights()
        recentUsage.forEach { insight ->
            if (insight.contains("app:")) {
                val appName = insight.substringAfter("app:").substringBefore(" ")
                suggestions.add("Find usage of $appName")
            }
        }
        
        // Recent notification suggestions
        val recentNotifications = notificationAnalyzer.getContextualInsights("")
        recentNotifications.forEach { insight ->
            if (insight.contains("messages")) {
                suggestions.add("Show recent messages")
            }
            if (insight.contains("notifications")) {
                suggestions.add("Find important notifications")
            }
        }
        
        // Time-based suggestions
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 6..10 -> {
                suggestions.add("Morning routines")
                suggestions.add("Today's schedule")
            }
            in 11..13 -> {
                suggestions.add("Lunch time activities")
                suggestions.add("Midday productivity")
            }
            in 14..17 -> {
                suggestions.add("Afternoon work")
                suggestions.add("Meeting summaries")
            }
            in 18..22 -> {
                suggestions.add("Evening activities")
                suggestions.add("Day recap")
            }
        }
        
        return suggestions.distinct().take(5)
    }
    
    /**
     * Add document to search index
     */
    suspend fun addDocument(
        id: String,
        title: String,
        content: String,
        source: String,
        metadata: Map<String, String> = emptyMap()
    ): Boolean {
        return vectorDatabase.addDocument(id, title, content, source, metadata)
    }
    
    /**
     * Add conversation message to search index
     */
    suspend fun addConversationMessage(
        messageId: String,
        content: String,
        context: String,
        timestamp: Long
    ): Boolean {
        val chatDocId = "chat_$messageId"
        val metadata = mapOf(
            "type" to "conversation",
            "context" to context,
            "timestamp" to timestamp.toString()
        )
        
        return vectorDatabase.addDocument(chatDocId, "Chat Message", content, "conversation", metadata)
    }
    
    /**
     * Remove document from search index
     */
    suspend fun removeDocument(id: String): Boolean {
        return vectorDatabase.removeDocument(id)
    }
    
    /**
     * Get search analytics
     */
    suspend fun getSearchAnalytics(): SearchAnalytics {
        val recentSessions = database.vectorDao().getRecentSearchSessions(30) // Last 30 days
        
        val totalSearches = recentSessions.size
        val avgResultsPerSearch = if (totalSearches > 0) {
            recentSessions.map { it.resultsCount }.average().toFloat()
        } else 0f
        
        val topQueries = recentSessions.groupBy { it.query }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(10)
            .map { it.first }
        
        val avgExecutionTime = if (totalSearches > 0) {
            recentSessions.map { it.executionTime }.average().toLong()
        } else 0L
        
        return SearchAnalytics(
            id = 0,
            totalSearches = totalSearches,
            avgResultsPerSearch = avgResultsPerSearch,
            avgExecutionTime = avgExecutionTime,
            topQueries = topQueries.joinToString(","),
            period = "30_days",
            timestamp = System.currentTimeMillis()
        )
    }
    
    // Private helper methods
    
    /**
     * Index existing data for search
     */
    private suspend fun indexExistingData() {
        // Index existing chat messages
        indexChatMessages()
        
        // Index notification content
        indexNotifications()
        
        // Index usage insights
        indexUsageInsights()
    }
    
    private suspend fun indexChatMessages() {
        // TODO: Implement message indexing when MessageDao has getAllMessages method
        // val messages = database.messageDao().getAllMessages()
        // messages.forEach { message ->
        //     addConversationMessage(
        //         messageId = message.id.toString(),
        //         content = message.content,
        //         context = "chat",
        //         timestamp = message.timestamp
        //     )
        // }
    }
    
    private suspend fun indexNotifications() {
        val notifications = database.notificationDao().getAllNotifications()
        notifications.forEach { notification ->
            val docId = "notification_${notification.id}"
            val content = "${notification.title} ${notification.text}"
            val metadata = mapOf(
                "type" to "notification",
                "app" to notification.packageName,
                "category" to notification.category,
                "timestamp" to notification.timestamp.toString()
            )
            
            vectorDatabase.addDocument(docId, notification.title, content, "notification", metadata)
        }
    }
    
    private suspend fun indexUsageInsights() {
        val insights = database.appUsageDao().getAllUsageInsights()
        insights.forEach { insight ->
            val docId = "insight_${insight.id}"
            val content = "${insight.title} ${insight.description}"
            val metadata = mapOf(
                "type" to "insight",
                "category" to insight.type,
                "timestamp" to insight.timestamp.toString()
            )
            
            vectorDatabase.addDocument(docId, insight.title, content, "usage_insight", metadata)
        }
    }
    
    /**
     * Generate search suggestions
     */
    private suspend fun generateSearchSuggestions() {
        val suggestions = mutableListOf<String>()
        
        // Popular queries
        suggestions.addAll(listOf(
            "Show my productivity today",
            "Recent important notifications",
            "Messages from this week",
            "Most used apps",
            "Focus time analysis",
            "Communication patterns",
            "Daily activity summary"
        ))
        
        // Add contextual suggestions
        suggestions.addAll(getPersonalizedSuggestions())
        
        _searchSuggestions.value = suggestions.distinct().take(10)
    }
    
    /**
     * Update search suggestions based on query
     */
    private suspend fun updateSearchSuggestions(query: String) {
        val currentSuggestions = _searchSuggestions.value.toMutableList()
        
        // Add related suggestions
        val relatedSuggestions = generateQuerySuggestions(query)
        currentSuggestions.addAll(relatedSuggestions)
        
        _searchSuggestions.value = currentSuggestions.distinct().take(10)
    }
    
    /**
     * Generate query-specific suggestions
     */
    private fun generateQuerySuggestions(query: String): List<String> {
        val suggestions = mutableListOf<String>()
        val queryLower = query.lowercase()
        
        when {
            queryLower.contains("message") || queryLower.contains("chat") -> {
                suggestions.addAll(listOf(
                    "Recent messages",
                    "Important conversations",
                    "Message frequency analysis"
                ))
            }
            queryLower.contains("app") || queryLower.contains("usage") -> {
                suggestions.addAll(listOf(
                    "App usage patterns",
                    "Screen time analysis",
                    "Productivity apps"
                ))
            }
            queryLower.contains("notification") -> {
                suggestions.addAll(listOf(
                    "Important notifications",
                    "Notification patterns",
                    "App notification frequency"
                ))
            }
            queryLower.contains("today") || queryLower.contains("day") -> {
                suggestions.addAll(listOf(
                    "Today's activity",
                    "Daily summary",
                    "Today's productivity"
                ))
            }
        }
        
        return suggestions
    }
    
    /**
     * Store search session for analytics
     */
    private suspend fun storeSearchSession(query: String, scope: SearchScope, results: SearchResults) {
        val session = SearchSession(
            id = 0,
            query = query,
            scope = scope.name,
            resultsCount = results.totalResults,
            executionTime = results.executionTime,
            timestamp = System.currentTimeMillis()
        )
        
        database.vectorDao().insertSearchSession(session)
    }
    
    /**
     * Calculate text similarity (simplified)
     */
    private fun calculateTextSimilarity(query: String, text: String): Float {
        val queryWords = query.lowercase().split(Regex("\\W+")).toSet()
        val textWords = text.lowercase().split(Regex("\\W+")).toSet()
        
        val intersection = queryWords.intersect(textWords).size
        val union = queryWords.union(textWords).size
        
        return if (union > 0) intersection.toFloat() / union else 0f
    }
    
    /**
     * Format duration for display
     */
    private fun formatDuration(millis: Long): String {
        val hours = millis / (60 * 60 * 1000)
        val minutes = (millis % (60 * 60 * 1000)) / (60 * 1000)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }
}
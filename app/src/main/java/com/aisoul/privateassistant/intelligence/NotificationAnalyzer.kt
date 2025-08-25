package com.aisoul.privateassistant.intelligence

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import com.aisoul.privateassistant.data.database.AppDatabase
import com.aisoul.privateassistant.data.entities.NotificationRecord
import com.aisoul.privateassistant.data.entities.NotificationInsight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import kotlin.collections.HashMap

/**
 * Advanced notification analysis and insights system
 * Provides intelligent analysis of user notifications for AI assistant context
 */
class NotificationAnalyzer private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: NotificationAnalyzer? = null
        
        fun getInstance(context: Context): NotificationAnalyzer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationAnalyzer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val packageManager = context.packageManager
    
    // Analysis state
    private val _analysisState = MutableStateFlow(AnalysisState.IDLE)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()
    
    private val _insights = MutableStateFlow<List<NotificationInsight>>(emptyList())
    val insights: StateFlow<List<NotificationInsight>> = _insights.asStateFlow()
    
    // Notification categories for analysis
    enum class NotificationCategory {
        COMMUNICATION,
        SOCIAL_MEDIA,
        PRODUCTIVITY,
        ENTERTAINMENT,
        SYSTEM,
        FINANCE,
        SHOPPING,
        NEWS,
        TRAVEL,
        HEALTH,
        UNKNOWN
    }
    
    enum class NotificationPriority {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        SPAM
    }
    
    enum class AnalysisState {
        IDLE,
        ANALYZING,
        COMPLETE,
        ERROR
    }
    
    data class NotificationAnalysis(
        val category: NotificationCategory,
        val priority: NotificationPriority,
        val sentiment: Float, // -1.0 to 1.0
        val urgency: Float,   // 0.0 to 1.0
        val keywords: List<String>,
        val suggestedActions: List<String>,
        val relatedApps: List<String>
    )
    
    /**
     * Analyze a single notification and extract insights
     */
    suspend fun analyzeNotification(notification: StatusBarNotification): NotificationAnalysis {
        val packageName = notification.packageName
        val title = getNotificationTitle(notification)
        val text = getNotificationText(notification)
        val timestamp = notification.postTime
        
        // Store notification record
        val record = NotificationRecord(
            id = 0,
            packageName = packageName,
            title = title,
            text = text,
            timestamp = timestamp,
            category = categorizeNotification(packageName, title, text).name,
            priority = assessPriority(notification).name
        )
        
        database.notificationDao().insertNotification(record)
        
        // Perform detailed analysis
        return performDetailedAnalysis(notification, title, text)
    }
    
    /**
     * Analyze notification patterns and generate insights
     */
    suspend fun generateInsights(): List<NotificationInsight> {
        _analysisState.value = AnalysisState.ANALYZING
        
        try {
            val recentNotifications = database.notificationDao()
                .getNotificationsAfter(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) // Last 7 days
            
            val insights = mutableListOf<NotificationInsight>()
            
            // App usage pattern analysis
            insights.add(analyzeAppUsagePatterns(recentNotifications))
            
            // Time-based analysis
            insights.add(analyzeNotificationTiming(recentNotifications))
            
            // Category distribution analysis
            insights.add(analyzeCategoryDistribution(recentNotifications))
            
            // Priority analysis
            insights.add(analyzePriorityPatterns(recentNotifications))
            
            // Communication analysis
            insights.add(analyzeCommunicationPatterns(recentNotifications))
            
            _insights.value = insights
            _analysisState.value = AnalysisState.COMPLETE
            
            return insights
        } catch (e: Exception) {
            _analysisState.value = AnalysisState.ERROR
            throw e
        }
    }
    
    /**
     * Get notification insights for AI context
     */
    suspend fun getContextualInsights(query: String): List<String> {
        val recentNotifications = database.notificationDao()
            .getNotificationsAfter(System.currentTimeMillis() - 24 * 60 * 60 * 1000) // Last 24 hours
        
        val contextualInsights = mutableListOf<String>()
        
        // Recent activity context
        if (recentNotifications.isNotEmpty()) {
            val topApps = recentNotifications.groupBy { it.packageName }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(3)
            
            contextualInsights.add("Recent app activity: ${topApps.joinToString(", ") { "${getAppName(it.first)} (${it.second} notifications)" }}")
        }
        
        // Communication context
        val commNotifications = recentNotifications.filter { 
            it.category == NotificationCategory.COMMUNICATION.name 
        }
        if (commNotifications.isNotEmpty()) {
            contextualInsights.add("Recent communication: ${commNotifications.size} messages from ${commNotifications.distinctBy { it.packageName }.size} apps")
        }
        
        // Priority context
        val criticalNotifications = recentNotifications.filter { 
            it.priority == NotificationPriority.CRITICAL.name || it.priority == NotificationPriority.HIGH.name
        }
        if (criticalNotifications.isNotEmpty()) {
            contextualInsights.add("Important notifications: ${criticalNotifications.size} high-priority items need attention")
        }
        
        return contextualInsights
    }
    
    /**
     * Categorize notification based on package name and content
     */
    private fun categorizeNotification(packageName: String, title: String, text: String): NotificationCategory {
        // App-based categorization
        when {
            packageName.contains("whatsapp") || 
            packageName.contains("telegram") || 
            packageName.contains("messenger") ||
            packageName.contains("sms") ||
            packageName.contains("messages") -> return NotificationCategory.COMMUNICATION
            
            packageName.contains("facebook") ||
            packageName.contains("instagram") ||
            packageName.contains("twitter") ||
            packageName.contains("linkedin") ||
            packageName.contains("snapchat") -> return NotificationCategory.SOCIAL_MEDIA
            
            packageName.contains("gmail") ||
            packageName.contains("outlook") ||
            packageName.contains("calendar") ||
            packageName.contains("task") ||
            packageName.contains("note") -> return NotificationCategory.PRODUCTIVITY
            
            packageName.contains("youtube") ||
            packageName.contains("netflix") ||
            packageName.contains("spotify") ||
            packageName.contains("game") -> return NotificationCategory.ENTERTAINMENT
            
            packageName.contains("bank") ||
            packageName.contains("payment") ||
            packageName.contains("wallet") -> return NotificationCategory.FINANCE
            
            packageName.contains("shop") ||
            packageName.contains("amazon") ||
            packageName.contains("ebay") -> return NotificationCategory.SHOPPING
            
            packageName.contains("news") ||
            packageName.contains("reddit") -> return NotificationCategory.NEWS
            
            packageName.contains("maps") ||
            packageName.contains("uber") ||
            packageName.contains("booking") -> return NotificationCategory.TRAVEL
            
            packageName.contains("health") ||
            packageName.contains("fitness") ||
            packageName.contains("medical") -> return NotificationCategory.HEALTH
            
            packageName.startsWith("com.android") ||
            packageName.startsWith("android") -> return NotificationCategory.SYSTEM
        }
        
        // Content-based categorization
        val combinedText = "$title $text".lowercase()
        when {
            combinedText.contains("message") || combinedText.contains("chat") -> return NotificationCategory.COMMUNICATION
            combinedText.contains("like") || combinedText.contains("comment") || combinedText.contains("follow") -> return NotificationCategory.SOCIAL_MEDIA
            combinedText.contains("meeting") || combinedText.contains("task") || combinedText.contains("reminder") -> return NotificationCategory.PRODUCTIVITY
            combinedText.contains("update") || combinedText.contains("system") -> return NotificationCategory.SYSTEM
            else -> return NotificationCategory.UNKNOWN
        }
    }
    
    /**
     * Assess notification priority
     */
    private fun assessPriority(notification: StatusBarNotification): NotificationPriority {
        val androidNotification = notification.notification
        
        // System-level priority
        when (androidNotification.priority) {
            Notification.PRIORITY_MAX -> return NotificationPriority.CRITICAL
            Notification.PRIORITY_HIGH -> return NotificationPriority.HIGH
            Notification.PRIORITY_DEFAULT -> return NotificationPriority.MEDIUM
            Notification.PRIORITY_LOW -> return NotificationPriority.LOW
            Notification.PRIORITY_MIN -> return NotificationPriority.SPAM
        }
        
        // Additional priority assessment
        val title = getNotificationTitle(notification).lowercase()
        val text = getNotificationText(notification).lowercase()
        
        when {
            title.contains("urgent") || title.contains("emergency") || title.contains("critical") -> return NotificationPriority.CRITICAL
            title.contains("important") || text.contains("action required") -> return NotificationPriority.HIGH
            title.contains("reminder") || text.contains("don't forget") -> return NotificationPriority.MEDIUM
            title.contains("promo") || title.contains("sale") || title.contains("offer") -> return NotificationPriority.LOW
            else -> return NotificationPriority.MEDIUM
        }
    }
    
    /**
     * Perform detailed analysis of notification content
     */
    private fun performDetailedAnalysis(notification: StatusBarNotification, title: String, text: String): NotificationAnalysis {
        val category = categorizeNotification(notification.packageName, title, text)
        val priority = assessPriority(notification)
        
        // Sentiment analysis (simplified)
        val sentiment = analyzeSentiment(title, text)
        
        // Urgency assessment
        val urgency = assessUrgency(title, text, notification.postTime)
        
        // Extract keywords
        val keywords = extractKeywords(title, text)
        
        // Generate suggested actions
        val suggestedActions = generateSuggestedActions(category, priority, title, text)
        
        // Find related apps
        val relatedApps = findRelatedApps(category, notification.packageName)
        
        return NotificationAnalysis(
            category = category,
            priority = priority,
            sentiment = sentiment,
            urgency = urgency,
            keywords = keywords,
            suggestedActions = suggestedActions,
            relatedApps = relatedApps
        )
    }
    
    /**
     * Simple sentiment analysis
     */
    private fun analyzeSentiment(title: String, text: String): Float {
        val combinedText = "$title $text".lowercase()
        
        val positiveWords = listOf("good", "great", "excellent", "success", "win", "congratulations", "happy", "love", "like", "amazing")
        val negativeWords = listOf("error", "failed", "problem", "issue", "urgent", "warning", "critical", "emergency", "hate", "dislike", "angry")
        
        val positiveCount = positiveWords.count { combinedText.contains(it) }
        val negativeCount = negativeWords.count { combinedText.contains(it) }
        
        return when {
            positiveCount > negativeCount -> 0.5f + (positiveCount - negativeCount) * 0.1f
            negativeCount > positiveCount -> -0.5f - (negativeCount - positiveCount) * 0.1f
            else -> 0.0f
        }.coerceIn(-1.0f, 1.0f)
    }
    
    /**
     * Assess notification urgency
     */
    private fun assessUrgency(title: String, text: String, timestamp: Long): Float {
        val combinedText = "$title $text".lowercase()
        val age = System.currentTimeMillis() - timestamp
        val ageHours = age / (60 * 60 * 1000)
        
        var urgency = 0.5f
        
        // Content-based urgency
        when {
            combinedText.contains("urgent") || combinedText.contains("emergency") -> urgency += 0.4f
            combinedText.contains("asap") || combinedText.contains("immediately") -> urgency += 0.3f
            combinedText.contains("important") || combinedText.contains("action required") -> urgency += 0.2f
            combinedText.contains("reminder") -> urgency += 0.1f
        }
        
        // Time-based urgency (decreases over time)
        urgency -= (ageHours * 0.05f)
        
        return urgency.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Extract keywords from notification content
     */
    private fun extractKeywords(title: String, text: String): List<String> {
        val combinedText = "$title $text"
        val words = combinedText.split(Regex("\\W+"))
            .filter { it.length > 3 }
            .map { it.lowercase() }
            .filter { !isStopWord(it) }
        
        return words.groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
    }
    
    /**
     * Check if word is a stop word
     */
    private fun isStopWord(word: String): Boolean {
        val stopWords = setOf("the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from", "this", "that", "these", "those", "you", "your", "have", "has", "had", "will", "would", "could", "should")
        return stopWords.contains(word)
    }
    
    /**
     * Generate suggested actions based on notification analysis
     */
    private fun generateSuggestedActions(category: NotificationCategory, priority: NotificationPriority, title: String, text: String): List<String> {
        val actions = mutableListOf<String>()
        
        when (category) {
            NotificationCategory.COMMUNICATION -> {
                actions.add("Reply to message")
                actions.add("Mark as read")
                if (priority == NotificationPriority.HIGH || priority == NotificationPriority.CRITICAL) {
                    actions.add("Call sender")
                }
            }
            NotificationCategory.PRODUCTIVITY -> {
                actions.add("Open calendar")
                actions.add("Set reminder")
                actions.add("Add to tasks")
            }
            NotificationCategory.SOCIAL_MEDIA -> {
                actions.add("View post")
                actions.add("Like")
                actions.add("Share")
            }
            else -> {
                actions.add("Open app")
                actions.add("Dismiss")
            }
        }
        
        return actions
    }
    
    /**
     * Find related apps based on category
     */
    private fun findRelatedApps(category: NotificationCategory, currentPackage: String): List<String> {
        // This would ideally scan installed apps and categorize them
        // For now, return common related apps
        return when (category) {
            NotificationCategory.COMMUNICATION -> listOf("WhatsApp", "Telegram", "Messages")
            NotificationCategory.SOCIAL_MEDIA -> listOf("Facebook", "Instagram", "Twitter")
            NotificationCategory.PRODUCTIVITY -> listOf("Gmail", "Calendar", "Keep")
            else -> emptyList()
        }.filter { !it.equals(getAppName(currentPackage), ignoreCase = true) }
    }
    
    // Analysis methods for insights generation
    
    private suspend fun analyzeAppUsagePatterns(notifications: List<NotificationRecord>): NotificationInsight {
        val appUsage = notifications.groupBy { it.packageName }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
        
        val topApp = if (appUsage.isNotEmpty()) getAppName(appUsage.first().first) else "None"
        val totalApps = appUsage.size
        val totalNotifications = notifications.size
        
        return NotificationInsight(
            id = 0,
            type = "app_usage",
            title = "App Usage Patterns",
            description = "Most active app: $topApp with ${if (appUsage.isNotEmpty()) appUsage.first().second else 0} notifications. Total: $totalNotifications notifications from $totalApps apps.",
            actionable = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun analyzeNotificationTiming(notifications: List<NotificationRecord>): NotificationInsight {
        val hourlyDistribution = notifications.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }
        
        val peakHour = hourlyDistribution.maxByOrNull { it.value.size }?.key ?: 12
        val nightNotifications = notifications.filter {
            val hour = Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
            hour in 22..23 || hour in 0..6
        }.size
        
        return NotificationInsight(
            id = 0,
            type = "timing",
            title = "Notification Timing",
            description = "Peak activity at ${peakHour}:00. ${nightNotifications} notifications during night hours (10PM-6AM).",
            actionable = nightNotifications > 5,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun analyzeCategoryDistribution(notifications: List<NotificationRecord>): NotificationInsight {
        val categoryDistribution = notifications.groupBy { it.category }
            .mapValues { it.value.size }
        
        val topCategory = categoryDistribution.maxByOrNull { it.value }
        val categoryCount = categoryDistribution.size
        
        return NotificationInsight(
            id = 0,
            type = "category",
            title = "Category Distribution",
            description = "Top category: ${topCategory?.key ?: "None"} (${topCategory?.value ?: 0} notifications). Active in $categoryCount different categories.",
            actionable = false,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun analyzePriorityPatterns(notifications: List<NotificationRecord>): NotificationInsight {
        val priorityDistribution = notifications.groupBy { it.priority }
            .mapValues { it.value.size }
        
        val highPriorityCount = (priorityDistribution[NotificationPriority.HIGH.name] ?: 0) + 
                              (priorityDistribution[NotificationPriority.CRITICAL.name] ?: 0)
        
        return NotificationInsight(
            id = 0,
            type = "priority",
            title = "Priority Analysis",
            description = "$highPriorityCount high-priority notifications out of ${notifications.size} total. Consider reviewing notification settings if too many low-priority alerts.",
            actionable = highPriorityCount > notifications.size * 0.3,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun analyzeCommunicationPatterns(notifications: List<NotificationRecord>): NotificationInsight {
        val commNotifications = notifications.filter { it.category == NotificationCategory.COMMUNICATION.name }
        val uniqueApps = commNotifications.distinctBy { it.packageName }.size
        
        return NotificationInsight(
            id = 0,
            type = "communication",
            title = "Communication Analysis",
            description = "${commNotifications.size} messages across $uniqueApps communication apps. Consider consolidating messaging apps for better focus.",
            actionable = uniqueApps > 3,
            timestamp = System.currentTimeMillis()
        )
    }
    
    // Utility methods
    
    private fun getNotificationTitle(notification: StatusBarNotification): String {
        return notification.notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
    }
    
    private fun getNotificationText(notification: StatusBarNotification): String {
        return notification.notification.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
package com.aisoul.privateassistant.intelligence

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.aisoul.privateassistant.data.database.AppDatabase
import com.aisoul.privateassistant.data.entities.AppUsageRecord
import com.aisoul.privateassistant.data.entities.UsageInsight
import com.aisoul.privateassistant.data.entities.ProductivityScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * App Usage Pattern Analysis and Reporting System
 * Analyzes user app usage patterns and provides productivity insights
 */
class AppUsageAnalyzer private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: AppUsageAnalyzer? = null
        
        fun getInstance(context: Context): AppUsageAnalyzer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppUsageAnalyzer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val packageManager = context.packageManager
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    
    // State management
    private val _analysisState = MutableStateFlow(AnalysisState.IDLE)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()
    
    private val _usageInsights = MutableStateFlow<List<UsageInsight>>(emptyList())
    val usageInsights: StateFlow<List<UsageInsight>> = _usageInsights.asStateFlow()
    
    private val _productivityScore = MutableStateFlow<ProductivityScore?>(null)
    val productivityScore: StateFlow<ProductivityScore?> = _productivityScore.asStateFlow()
    
    enum class AnalysisState {
        IDLE,
        PERMISSION_REQUIRED,
        ANALYZING,
        COMPLETE,
        ERROR
    }
    
    enum class AppCategory {
        PRODUCTIVITY,
        COMMUNICATION,
        SOCIAL_MEDIA,
        ENTERTAINMENT,
        GAMES,
        EDUCATION,
        UTILITIES,
        FINANCE,
        SHOPPING,
        HEALTH_FITNESS,
        NEWS,
        TRAVEL,
        PHOTOGRAPHY,
        MUSIC,
        SYSTEM,
        UNKNOWN
    }
    
    data class AppUsageData(
        val packageName: String,
        val appName: String,
        val category: AppCategory,
        val totalTimeInForeground: Long,
        val lastTimeUsed: Long,
        val launchCount: Int,
        val averageSessionTime: Long,
        val dailyUsage: Map<String, Long>, // Date to usage time
        val hourlyPattern: Map<Int, Long>, // Hour to usage time
        val weeklyPattern: Map<Int, Long> // Day of week to usage time
    )
    
    data class UsagePattern(
        val peakUsageHour: Int,
        val averageDailyUsage: Long,
        val mostActiveDay: Int,
        val sessionFrequency: Float,
        val averageSessionDuration: Long,
        val multitaskingScore: Float,
        val focusScore: Float
    )
    
    data class ProductivityAnalysis(
        val productiveTime: Long,
        val distractiveTime: Long,
        val neutralTime: Long,
        val productivityRatio: Float,
        val focusTimeBlocks: List<TimeBlock>,
        val distractionTriggers: List<String>,
        val recommendations: List<String>
    )
    
    data class TimeBlock(
        val startTime: Long,
        val endTime: Long,
        val category: AppCategory,
        val duration: Long,
        val isProductiveBlock: Boolean
    )
    
    /**
     * Initialize usage analysis and check permissions
     */
    suspend fun initialize(): Boolean {
        if (!hasUsageStatsPermission()) {
            _analysisState.value = AnalysisState.PERMISSION_REQUIRED
            return false
        }
        
        _analysisState.value = AnalysisState.ANALYZING
        
        try {
            // Analyze app usage patterns
            analyzeUsagePatterns()
            
            // Generate productivity insights
            generateProductivityInsights()
            
            _analysisState.value = AnalysisState.COMPLETE
            return true
        } catch (e: Exception) {
            _analysisState.value = AnalysisState.ERROR
            return false
        }
    }
    
    /**
     * Analyze app usage patterns over specified time period
     */
    suspend fun analyzeUsagePatterns(days: Int = 7): List<AppUsageData> {
        if (!hasUsageStatsPermission()) return emptyList()
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days * 24 * 60 * 60 * 1000L)
        
        val usageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyList()
        
        val appUsageList = mutableListOf<AppUsageData>()
        
        for (usageStat in usageStats) {
            if (usageStat.totalTimeInForeground > 0) {
                val appName = getAppName(usageStat.packageName)
                val category = categorizeApp(usageStat.packageName, appName)
                
                // Get detailed usage patterns
                val dailyUsage = getDailyUsagePattern(usageStat.packageName, startTime, endTime)
                val hourlyPattern = getHourlyUsagePattern(usageStat.packageName, startTime, endTime)
                val weeklyPattern = getWeeklyUsagePattern(usageStat.packageName, startTime, endTime)
                val sessionData = getSessionData(usageStat.packageName, startTime, endTime)
                
                val appUsageData = AppUsageData(
                    packageName = usageStat.packageName,
                    appName = appName,
                    category = category,
                    totalTimeInForeground = usageStat.totalTimeInForeground,
                    lastTimeUsed = usageStat.lastTimeUsed,
                    launchCount = sessionData.first,
                    averageSessionTime = sessionData.second,
                    dailyUsage = dailyUsage,
                    hourlyPattern = hourlyPattern,
                    weeklyPattern = weeklyPattern
                )
                
                appUsageList.add(appUsageData)
                
                // Store usage record
                val usageRecord = AppUsageRecord(
                    id = 0,
                    packageName = usageStat.packageName,
                    appName = appName,
                    category = category.name,
                    totalTime = usageStat.totalTimeInForeground,
                    launchCount = sessionData.first,
                    lastUsed = usageStat.lastTimeUsed,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    timestamp = System.currentTimeMillis()
                )
                
                database.appUsageDao().insertUsageRecord(usageRecord)
            }
        }
        
        return appUsageList.sortedByDescending { it.totalTimeInForeground }
    }
    
    /**
     * Generate productivity insights from usage data
     */
    suspend fun generateProductivityInsights(): List<UsageInsight> {
        val usageData = analyzeUsagePatterns()
        val insights = mutableListOf<UsageInsight>()
        
        // Calculate productivity metrics
        val productivityAnalysis = calculateProductivityAnalysis(usageData)
        
        // Screen time insight
        val totalScreenTime = usageData.sumOf { it.totalTimeInForeground }
        insights.add(UsageInsight(
            id = 0,
            type = "screen_time",
            title = "Weekly Screen Time",
            description = "Total: ${formatDuration(totalScreenTime)}. Average: ${formatDuration(totalScreenTime / 7)} per day.",
            value = totalScreenTime.toFloat(),
            trend = calculateScreenTimeTrend(),
            actionable = totalScreenTime > 8 * 60 * 60 * 1000 * 7, // More than 8h/day
            timestamp = System.currentTimeMillis()
        ))
        
        // Productivity ratio insight
        insights.add(UsageInsight(
            id = 0,
            type = "productivity",
            title = "Productivity Score",
            description = "Productive apps: ${(productivityAnalysis.productivityRatio * 100).roundToInt()}% of usage time. ${generateProductivityRecommendation(productivityAnalysis.productivityRatio)}",
            value = productivityAnalysis.productivityRatio,
            trend = calculateProductivityTrend(),
            actionable = productivityAnalysis.productivityRatio < 0.3f,
            timestamp = System.currentTimeMillis()
        ))
        
        // App distribution insight
        val topApps = usageData.take(5)
        insights.add(UsageInsight(
            id = 0,
            type = "app_distribution",
            title = "Top Apps",
            description = "Most used: ${topApps.joinToString(", ") { "${it.appName} (${formatDuration(it.totalTimeInForeground)})" }}",
            value = topApps.sumOf { it.totalTimeInForeground }.toFloat(),
            trend = 0f,
            actionable = false,
            timestamp = System.currentTimeMillis()
        ))
        
        // Usage pattern insight
        val usagePattern = analyzeUsagePattern(usageData)
        insights.add(UsageInsight(
            id = 0,
            type = "usage_pattern",
            title = "Usage Patterns",
            description = "Peak usage: ${usagePattern.peakUsageHour}:00. Average session: ${formatDuration(usagePattern.averageSessionDuration)}. Focus score: ${(usagePattern.focusScore * 100).roundToInt()}%",
            value = usagePattern.focusScore,
            trend = calculateFocusTrend(),
            actionable = usagePattern.focusScore < 0.6f,
            timestamp = System.currentTimeMillis()
        ))
        
        // Distraction insight
        val distractiveApps = usageData.filter { it.category in listOf(AppCategory.SOCIAL_MEDIA, AppCategory.GAMES, AppCategory.ENTERTAINMENT) }
        val distractionTime = distractiveApps.sumOf { it.totalTimeInForeground }
        if (distractionTime > 0) {
            insights.add(UsageInsight(
                id = 0,
                type = "distractions",
                title = "Distraction Analysis",
                description = "Potentially distracting apps used for ${formatDuration(distractionTime)} (${(distractionTime.toFloat() / totalScreenTime * 100).roundToInt()}% of total time).",
                value = distractionTime.toFloat(),
                trend = calculateDistractionTrend(),
                actionable = distractionTime > totalScreenTime * 0.4f,
                timestamp = System.currentTimeMillis()
            ))
        }
        
        // Focus time blocks insight
        val focusBlocks = identifyFocusTimeBlocks(usageData)
        if (focusBlocks.isNotEmpty()) {
            val averageFocusBlock = focusBlocks.map { it.duration }.average().toLong()
            insights.add(UsageInsight(
                id = 0,
                type = "focus_blocks",
                title = "Focus Time Analysis",
                description = "${focusBlocks.size} focus blocks identified. Average focus duration: ${formatDuration(averageFocusBlock)}.",
                value = averageFocusBlock.toFloat(),
                trend = 0f,
                actionable = focusBlocks.size < 3,
                timestamp = System.currentTimeMillis()
            ))
        }
        
        // Store insights
        database.appUsageDao().insertUsageInsights(insights)
        _usageInsights.value = insights
        
        // Calculate and store productivity score
        val productivityScore = ProductivityScore(
            id = 0,
            score = productivityAnalysis.productivityRatio,
            productiveTime = productivityAnalysis.productiveTime,
            distractiveTime = productivityAnalysis.distractiveTime,
            focusScore = usagePattern.focusScore,
            sessionQuality = calculateSessionQuality(usageData),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            timestamp = System.currentTimeMillis()
        )
        
        database.appUsageDao().insertProductivityScore(productivityScore)
        _productivityScore.value = productivityScore
        
        return insights
    }
    
    /**
     * Get contextual usage insights for AI responses
     */
    suspend fun getContextualUsageInsights(): List<String> {
        val recentUsage = database.appUsageDao().getRecentUsageRecords(24) // Last 24 hours
        val insights = mutableListOf<String>()
        
        if (recentUsage.isNotEmpty()) {
            val totalTime = recentUsage.sumOf { it.totalTime }
            val topApp = recentUsage.maxByOrNull { it.totalTime }
            
            insights.add("Today's screen time: ${formatDuration(totalTime)}")
            
            if (topApp != null) {
                insights.add("Most used app today: ${topApp.appName} (${formatDuration(topApp.totalTime)})")
            }
            
            val productiveApps = recentUsage.filter { 
                AppCategory.valueOf(it.category) in listOf(AppCategory.PRODUCTIVITY, AppCategory.EDUCATION, AppCategory.UTILITIES)
            }
            val productiveTime = productiveApps.sumOf { it.totalTime }
            
            if (productiveTime > 0) {
                val productivityRatio = productiveTime.toFloat() / totalTime
                insights.add("Productivity focus: ${(productivityRatio * 100).roundToInt()}% of usage time")
            }
        }
        
        return insights
    }
    
    /**
     * Categorize app based on package name and app name
     */
    private fun categorizeApp(packageName: String, appName: String): AppCategory {
        val name = appName.lowercase()
        val pkg = packageName.lowercase()
        
        return when {
            // Productivity
            pkg.contains("office") || pkg.contains("docs") || pkg.contains("sheets") || 
            pkg.contains("slides") || pkg.contains("notion") || pkg.contains("evernote") ||
            pkg.contains("calendar") || pkg.contains("task") || pkg.contains("todo") ||
            name.contains("office") || name.contains("word") || name.contains("excel") ||
            name.contains("powerpoint") || name.contains("calendar") -> AppCategory.PRODUCTIVITY
            
            // Communication
            pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("messenger") ||
            pkg.contains("slack") || pkg.contains("teams") || pkg.contains("zoom") ||
            pkg.contains("skype") || pkg.contains("discord") || pkg.contains("gmail") ||
            pkg.contains("outlook") || pkg.contains("mail") -> AppCategory.COMMUNICATION
            
            // Social Media
            pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("twitter") ||
            pkg.contains("snapchat") || pkg.contains("tiktok") || pkg.contains("linkedin") ||
            pkg.contains("reddit") || pkg.contains("pinterest") -> AppCategory.SOCIAL_MEDIA
            
            // Entertainment
            pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("prime") ||
            pkg.contains("disney") || pkg.contains("hulu") || pkg.contains("twitch") ||
            name.contains("video") || name.contains("tv") -> AppCategory.ENTERTAINMENT
            
            // Games
            pkg.contains("game") || pkg.contains("play") && pkg.contains("games") ||
            name.contains("game") || name.contains("puzzle") || name.contains("adventure") -> AppCategory.GAMES
            
            // Education
            pkg.contains("khan") || pkg.contains("coursera") || pkg.contains("udemy") ||
            pkg.contains("duolingo") || pkg.contains("education") || 
            name.contains("learn") || name.contains("study") -> AppCategory.EDUCATION
            
            // Finance
            pkg.contains("bank") || pkg.contains("wallet") || pkg.contains("pay") ||
            pkg.contains("finance") || name.contains("bank") || name.contains("wallet") -> AppCategory.FINANCE
            
            // Shopping
            pkg.contains("amazon") || pkg.contains("shop") || pkg.contains("store") ||
            pkg.contains("ebay") || name.contains("shop") -> AppCategory.SHOPPING
            
            // Health & Fitness
            pkg.contains("health") || pkg.contains("fitness") || pkg.contains("workout") ||
            name.contains("health") || name.contains("fitness") || name.contains("step") -> AppCategory.HEALTH_FITNESS
            
            // News
            pkg.contains("news") || pkg.contains("times") || pkg.contains("post") ||
            name.contains("news") || name.contains("times") -> AppCategory.NEWS
            
            // Travel
            pkg.contains("maps") || pkg.contains("uber") || pkg.contains("lyft") ||
            pkg.contains("booking") || pkg.contains("airbnb") || pkg.contains("travel") -> AppCategory.TRAVEL
            
            // Photography
            pkg.contains("camera") || pkg.contains("photo") || pkg.contains("instagram") ||
            name.contains("camera") || name.contains("photo") -> AppCategory.PHOTOGRAPHY
            
            // Music
            pkg.contains("spotify") || pkg.contains("music") || pkg.contains("pandora") ||
            name.contains("music") || name.contains("radio") -> AppCategory.MUSIC
            
            // System
            pkg.startsWith("com.android") || pkg.startsWith("android") ||
            pkg.contains("system") || name.contains("settings") -> AppCategory.SYSTEM
            
            // Utilities
            pkg.contains("util") || pkg.contains("tool") || pkg.contains("file") ||
            name.contains("util") || name.contains("tool") || name.contains("manager") -> AppCategory.UTILITIES
            
            else -> AppCategory.UNKNOWN
        }
    }
    
    /**
     * Calculate productivity analysis from usage data
     */
    private fun calculateProductivityAnalysis(usageData: List<AppUsageData>): ProductivityAnalysis {
        val productiveCategories = setOf(AppCategory.PRODUCTIVITY, AppCategory.EDUCATION, AppCategory.UTILITIES)
        val distractiveCategories = setOf(AppCategory.SOCIAL_MEDIA, AppCategory.GAMES, AppCategory.ENTERTAINMENT)
        
        val productiveTime = usageData.filter { it.category in productiveCategories }.sumOf { it.totalTimeInForeground }
        val distractiveTime = usageData.filter { it.category in distractiveCategories }.sumOf { it.totalTimeInForeground }
        val neutralTime = usageData.sumOf { it.totalTimeInForeground } - productiveTime - distractiveTime
        
        val totalTime = productiveTime + distractiveTime + neutralTime
        val productivityRatio = if (totalTime > 0) productiveTime.toFloat() / totalTime else 0f
        
        val focusBlocks = identifyFocusTimeBlocks(usageData)
        val distractionTriggers = identifyDistractionTriggers(usageData)
        val recommendations = generateProductivityRecommendations(productivityRatio, distractiveTime, totalTime)
        
        return ProductivityAnalysis(
            productiveTime = productiveTime,
            distractiveTime = distractiveTime,
            neutralTime = neutralTime,
            productivityRatio = productivityRatio,
            focusTimeBlocks = focusBlocks,
            distractionTriggers = distractionTriggers,
            recommendations = recommendations
        )
    }
    
    /**
     * Analyze overall usage pattern
     */
    private fun analyzeUsagePattern(usageData: List<AppUsageData>): UsagePattern {
        // Aggregate hourly patterns
        val hourlyUsage = mutableMapOf<Int, Long>()
        usageData.forEach { app ->
            app.hourlyPattern.forEach { (hour, time) ->
                hourlyUsage[hour] = (hourlyUsage[hour] ?: 0) + time
            }
        }
        
        val peakUsageHour = hourlyUsage.maxByOrNull { it.value }?.key ?: 12
        
        // Calculate daily averages
        val totalUsage = usageData.sumOf { it.totalTimeInForeground }
        val averageDailyUsage = totalUsage / 7 // Assuming 7-day analysis
        
        // Weekly pattern
        val weeklyUsage = mutableMapOf<Int, Long>()
        usageData.forEach { app ->
            app.weeklyPattern.forEach { (day, time) ->
                weeklyUsage[day] = (weeklyUsage[day] ?: 0) + time
            }
        }
        val mostActiveDay = weeklyUsage.maxByOrNull { it.value }?.key ?: Calendar.SUNDAY
        
        // Session analysis
        val totalSessions = usageData.sumOf { it.launchCount }
        val sessionFrequency = totalSessions.toFloat() / 7f // sessions per day
        val averageSessionDuration = if (totalSessions > 0) totalUsage / totalSessions else 0L
        
        // Calculate focus metrics
        val multitaskingScore = calculateMultitaskingScore(usageData)
        val focusScore = calculateFocusScore(usageData, averageSessionDuration)
        
        return UsagePattern(
            peakUsageHour = peakUsageHour,
            averageDailyUsage = averageDailyUsage,
            mostActiveDay = mostActiveDay,
            sessionFrequency = sessionFrequency,
            averageSessionDuration = averageSessionDuration,
            multitaskingScore = multitaskingScore,
            focusScore = focusScore
        )
    }
    
    /**
     * Identify focus time blocks (periods of sustained productive app usage)
     */
    private fun identifyFocusTimeBlocks(usageData: List<AppUsageData>): List<TimeBlock> {
        // This is a simplified implementation
        // In reality, you'd need detailed session timing data
        val focusBlocks = mutableListOf<TimeBlock>()
        
        usageData.filter { it.category == AppCategory.PRODUCTIVITY }.forEach { app ->
            if (app.averageSessionTime > 30 * 60 * 1000) { // Sessions longer than 30 minutes
                val blockDuration = app.averageSessionTime
                focusBlocks.add(TimeBlock(
                    startTime = app.lastTimeUsed - blockDuration,
                    endTime = app.lastTimeUsed,
                    category = app.category,
                    duration = blockDuration,
                    isProductiveBlock = true
                ))
            }
        }
        
        return focusBlocks.sortedByDescending { it.duration }
    }
    
    /**
     * Calculate various trend indicators
     */
    private suspend fun calculateScreenTimeTrend(): Float {
        val currentWeek = database.appUsageDao().getUsageRecordsForPeriod(7)
        val previousWeek = database.appUsageDao().getUsageRecordsForPeriod(14, 7)
        
        val currentTotal = currentWeek.sumOf { it.totalTime }
        val previousTotal = previousWeek.sumOf { it.totalTime }
        
        return if (previousTotal > 0) {
            ((currentTotal - previousTotal).toFloat() / previousTotal).coerceIn(-1f, 1f)
        } else 0f
    }
    
    private suspend fun calculateProductivityTrend(): Float {
        // Similar calculation for productivity ratio trend
        return 0f // Simplified for now
    }
    
    private suspend fun calculateFocusTrend(): Float {
        // Similar calculation for focus score trend
        return 0f // Simplified for now
    }
    
    private suspend fun calculateDistractionTrend(): Float {
        // Similar calculation for distraction time trend
        return 0f // Simplified for now
    }
    
    /**
     * Helper methods for detailed analysis
     */
    private fun getDailyUsagePattern(packageName: String, startTime: Long, endTime: Long): Map<String, Long> {
        // This would require more detailed usage stats API calls
        // Simplified implementation
        return emptyMap()
    }
    
    private fun getHourlyUsagePattern(packageName: String, startTime: Long, endTime: Long): Map<Int, Long> {
        // This would require more detailed usage stats API calls
        // Simplified implementation
        return emptyMap()
    }
    
    private fun getWeeklyUsagePattern(packageName: String, startTime: Long, endTime: Long): Map<Int, Long> {
        // This would require more detailed usage stats API calls
        // Simplified implementation
        return emptyMap()
    }
    
    private fun getSessionData(packageName: String, startTime: Long, endTime: Long): Pair<Int, Long> {
        // This would require session analysis from usage events
        // Simplified implementation returns estimated values
        return Pair(10, 5 * 60 * 1000) // 10 sessions, 5 min average
    }
    
    private fun calculateMultitaskingScore(usageData: List<AppUsageData>): Float {
        // Higher score indicates more app switching behavior
        val totalSessions = usageData.sumOf { it.launchCount }
        val uniqueApps = usageData.size
        return if (uniqueApps > 0) (totalSessions.toFloat() / uniqueApps).coerceIn(0f, 10f) / 10f else 0f
    }
    
    private fun calculateFocusScore(usageData: List<AppUsageData>, averageSessionDuration: Long): Float {
        // Higher score indicates better focus (longer sessions, fewer switches)
        val longSessions = usageData.count { it.averageSessionTime > 20 * 60 * 1000 } // > 20 minutes
        val totalApps = usageData.size
        return if (totalApps > 0) longSessions.toFloat() / totalApps else 0f
    }
    
    private fun calculateSessionQuality(usageData: List<AppUsageData>): Float {
        // Quality based on productive vs distractive app usage
        val productiveTime = usageData.filter { it.category == AppCategory.PRODUCTIVITY }.sumOf { it.totalTimeInForeground }
        val totalTime = usageData.sumOf { it.totalTimeInForeground }
        return if (totalTime > 0) productiveTime.toFloat() / totalTime else 0f
    }
    
    private fun identifyDistractionTriggers(usageData: List<AppUsageData>): List<String> {
        return usageData.filter { it.category in setOf(AppCategory.SOCIAL_MEDIA, AppCategory.GAMES) }
            .sortedByDescending { it.launchCount }
            .take(3)
            .map { it.appName }
    }
    
    private fun generateProductivityRecommendations(productivityRatio: Float, distractiveTime: Long, totalTime: Long): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (productivityRatio < 0.3f) {
            recommendations.add("Consider using app timers to limit distracting apps")
            recommendations.add("Schedule dedicated focus time blocks for productive work")
        }
        
        if (distractiveTime > totalTime * 0.5f) {
            recommendations.add("Review your social media and entertainment app usage")
            recommendations.add("Try the 'Do Not Disturb' mode during work hours")
        }
        
        recommendations.add("Consider using productivity apps to track and improve focus")
        
        return recommendations
    }
    
    private fun generateProductivityRecommendation(ratio: Float): String {
        return when {
            ratio >= 0.7f -> "Excellent productivity focus!"
            ratio >= 0.5f -> "Good productivity balance."
            ratio >= 0.3f -> "Consider increasing focus on productive apps."
            else -> "Try to reduce time on distracting apps."
        }
    }
    
    // Utility methods
    
    private fun hasUsageStatsPermission(): Boolean {
        // Check if app has usage stats permission
        // This requires a more complex check in practice
        return usageStatsManager != null
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
    
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
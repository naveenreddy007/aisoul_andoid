package com.aisoul.privateassistant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * App usage record entity for usage pattern analysis
 */
@Entity(tableName = "app_usage_records")
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val category: String,
    val totalTime: Long,
    val launchCount: Int,
    val lastUsed: Long,
    val date: String,
    val timestamp: Long
)

/**
 * Usage insight entity for analysis results
 */
@Entity(tableName = "usage_insights")
data class UsageInsight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val title: String,
    val description: String,
    val value: Float,
    val trend: Float,
    val actionable: Boolean,
    val timestamp: Long
)

/**
 * Productivity score entity for tracking user productivity
 */
@Entity(tableName = "productivity_scores")
data class ProductivityScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Float,
    val productiveTime: Long,
    val distractiveTime: Long,
    val focusScore: Float,
    val sessionQuality: Float,
    val date: String,
    val timestamp: Long
)
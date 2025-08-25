package com.aisoul.privateassistant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Notification record entity for notification analysis
 */
@Entity(tableName = "notification_records")
data class NotificationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val category: String,
    val priority: String
)

/**
 * Notification insight entity for analysis results
 */
@Entity(tableName = "notification_insights")
data class NotificationInsight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val title: String,
    val description: String,
    val actionable: Boolean,
    val timestamp: Long
)
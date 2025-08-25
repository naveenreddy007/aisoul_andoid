package com.aisoul.privateassistant.data.dao

import androidx.room.*
import com.aisoul.privateassistant.data.entities.NotificationRecord
import com.aisoul.privateassistant.data.entities.NotificationInsight

/**
 * DAO for notification analysis operations
 */
@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notification_records ORDER BY timestamp DESC")
    suspend fun getAllNotifications(): List<NotificationRecord>
    
    @Query("SELECT * FROM notification_records WHERE timestamp > :afterTimestamp ORDER BY timestamp DESC")
    suspend fun getNotificationsAfter(afterTimestamp: Long): List<NotificationRecord>
    
    @Query("SELECT * FROM notification_records WHERE packageName = :packageName ORDER BY timestamp DESC")
    suspend fun getNotificationsByPackage(packageName: String): List<NotificationRecord>
    
    @Query("SELECT * FROM notification_records WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getNotificationsByCategory(category: String): List<NotificationRecord>
    
    @Query("SELECT * FROM notification_records WHERE priority = :priority ORDER BY timestamp DESC")
    suspend fun getNotificationsByPriority(priority: String): List<NotificationRecord>
    
    @Query("SELECT * FROM notification_records WHERE title LIKE :searchTerm OR text LIKE :searchTerm ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchNotifications(searchTerm: String, limit: Int): List<NotificationRecord>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationRecord): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationRecord>)
    
    @Update
    suspend fun updateNotification(notification: NotificationRecord)
    
    @Delete
    suspend fun deleteNotification(notification: NotificationRecord)
    
    @Query("DELETE FROM notification_records WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldNotifications(beforeTimestamp: Long): Int
    
    // Notification Insights
    @Query("SELECT * FROM notification_insights ORDER BY timestamp DESC")
    suspend fun getAllInsights(): List<NotificationInsight>
    
    @Query("SELECT * FROM notification_insights WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getInsightsByType(type: String): List<NotificationInsight>
    
    @Query("SELECT * FROM notification_insights WHERE actionable = 1 ORDER BY timestamp DESC")
    suspend fun getActionableInsights(): List<NotificationInsight>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: NotificationInsight): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsights(insights: List<NotificationInsight>)
    
    @Update
    suspend fun updateInsight(insight: NotificationInsight)
    
    @Delete
    suspend fun deleteInsight(insight: NotificationInsight)
    
    @Query("DELETE FROM notification_insights WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldInsights(beforeTimestamp: Long): Int
}
package com.aisoul.privateassistant.data.dao

import androidx.room.*
import com.aisoul.privateassistant.data.entities.AppUsageRecord
import com.aisoul.privateassistant.data.entities.UsageInsight
import com.aisoul.privateassistant.data.entities.ProductivityScore

/**
 * DAO for app usage analysis operations
 */
@Dao
interface AppUsageDao {
    
    // App Usage Records
    @Query("SELECT * FROM app_usage_records ORDER BY timestamp DESC")
    suspend fun getAllUsageRecords(): List<AppUsageRecord>
    
    @Query("SELECT * FROM app_usage_records WHERE date = :date ORDER BY totalTime DESC")
    suspend fun getUsageRecordsByDate(date: String): List<AppUsageRecord>
    
    @Query("SELECT * FROM app_usage_records WHERE packageName = :packageName ORDER BY timestamp DESC")
    suspend fun getUsageRecordsByPackage(packageName: String): List<AppUsageRecord>
    
    @Query("SELECT * FROM app_usage_records WHERE category = :category ORDER BY totalTime DESC")
    suspend fun getUsageRecordsByCategory(category: String): List<AppUsageRecord>
    
    @Query("SELECT * FROM app_usage_records WHERE timestamp > :hoursAgo ORDER BY timestamp DESC")
    suspend fun getRecentUsageRecords(hoursAgo: Long): List<AppUsageRecord>
    
    @Query("SELECT * FROM app_usage_records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getUsageRecordsForPeriod(startTime: Long, endTime: Long): List<AppUsageRecord>
    
    @Query("SELECT * FROM app_usage_records WHERE timestamp > :sinceTime ORDER BY timestamp DESC LIMIT :days")
    suspend fun getUsageRecordsForPeriod(days: Int, sinceTime: Long = 0): List<AppUsageRecord>
    
    @Query("SELECT * FROM app_usage_records WHERE appName LIKE :searchTerm OR packageName LIKE :searchTerm ORDER BY totalTime DESC LIMIT :limit")
    suspend fun searchUsageRecords(searchTerm: String, limit: Int): List<AppUsageRecord>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecord(usageRecord: AppUsageRecord): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecords(usageRecords: List<AppUsageRecord>)
    
    @Update
    suspend fun updateUsageRecord(usageRecord: AppUsageRecord)
    
    @Delete
    suspend fun deleteUsageRecord(usageRecord: AppUsageRecord)
    
    @Query("DELETE FROM app_usage_records WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldUsageRecords(beforeTimestamp: Long): Int
    
    // Usage Insights
    @Query("SELECT * FROM usage_insights ORDER BY timestamp DESC")
    suspend fun getAllUsageInsights(): List<UsageInsight>
    
    @Query("SELECT * FROM usage_insights WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getUsageInsightsByType(type: String): List<UsageInsight>
    
    @Query("SELECT * FROM usage_insights WHERE actionable = 1 ORDER BY timestamp DESC")
    suspend fun getActionableUsageInsights(): List<UsageInsight>
    
    @Query("SELECT * FROM usage_insights WHERE timestamp > :afterTimestamp ORDER BY timestamp DESC")
    suspend fun getRecentUsageInsights(afterTimestamp: Long): List<UsageInsight>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageInsight(usageInsight: UsageInsight): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageInsights(usageInsights: List<UsageInsight>)
    
    @Update
    suspend fun updateUsageInsight(usageInsight: UsageInsight)
    
    @Delete
    suspend fun deleteUsageInsight(usageInsight: UsageInsight)
    
    @Query("DELETE FROM usage_insights WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldUsageInsights(beforeTimestamp: Long): Int
    
    // Productivity Scores
    @Query("SELECT * FROM productivity_scores ORDER BY timestamp DESC")
    suspend fun getAllProductivityScores(): List<ProductivityScore>
    
    @Query("SELECT * FROM productivity_scores WHERE date = :date")
    suspend fun getProductivityScoreByDate(date: String): ProductivityScore?
    
    @Query("SELECT * FROM productivity_scores WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getProductivityScoresForPeriod(startTime: Long, endTime: Long): List<ProductivityScore>
    
    @Query("SELECT * FROM productivity_scores ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentProductivityScores(limit: Int): List<ProductivityScore>
    
    @Query("SELECT AVG(score) FROM productivity_scores WHERE timestamp > :afterTimestamp")
    suspend fun getAverageProductivityScore(afterTimestamp: Long): Float?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductivityScore(productivityScore: ProductivityScore): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductivityScores(productivityScores: List<ProductivityScore>)
    
    @Update
    suspend fun updateProductivityScore(productivityScore: ProductivityScore)
    
    @Delete
    suspend fun deleteProductivityScore(productivityScore: ProductivityScore)
    
    @Query("DELETE FROM productivity_scores WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldProductivityScores(beforeTimestamp: Long): Int
}
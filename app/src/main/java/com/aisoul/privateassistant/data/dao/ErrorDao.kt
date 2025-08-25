package com.aisoul.privateassistant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aisoul.privateassistant.data.entities.ErrorLog

/**
 * DAO for error log operations
 */
@Dao
interface ErrorDao {
    @Insert
    suspend fun insertErrorLog(errorLog: ErrorLog)
    
    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentErrorLogs(limit: Int): List<ErrorLog>
    
    @Query("SELECT * FROM error_logs WHERE level = :level ORDER BY timestamp DESC")
    suspend fun getErrorLogsByLevel(level: String): List<ErrorLog>
    
    @Query("SELECT * FROM error_logs WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getErrorLogsByCategory(category: String): List<ErrorLog>
    
    @Query("SELECT * FROM error_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getErrorLogsSince(startTime: Long): List<ErrorLog>
    
    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC")
    suspend fun getAllErrorLogs(): List<ErrorLog>
    
    @Query("UPDATE error_logs SET resolved = :resolved WHERE id = :id")
    suspend fun updateErrorResolved(id: Long, resolved: Boolean)
    
    @Query("DELETE FROM error_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldErrorLogs(cutoffTime: Long)
    
    @Query("SELECT COUNT(*) FROM error_logs WHERE level = 'FATAL'")
    suspend fun getCrashCount(): Int
    
    @Query("SELECT COUNT(*) FROM error_logs WHERE timestamp >= :startTime")
    suspend fun getErrorCountSince(startTime: Long): Int
}
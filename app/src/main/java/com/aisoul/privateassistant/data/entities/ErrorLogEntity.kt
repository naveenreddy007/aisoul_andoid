package com.aisoul.privateassistant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Error log entity for storing application errors and crashes
 */
@Entity(tableName = "error_logs")
data class ErrorLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val errorId: String,
    val level: String,
    val category: String,
    val message: String,
    val stackTrace: String?,
    val timestamp: Long,
    val context: String?,
    val userAction: String?,
    val deviceInfo: String,
    val appVersion: String,
    val resolved: Boolean = false
)
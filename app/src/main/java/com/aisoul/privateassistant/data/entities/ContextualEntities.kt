package com.aisoul.privateassistant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Contextual memory entity for AI learning
 */
@Entity(tableName = "contextual_memories")
data class ContextualMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val concept: String,
    val contextType: String,
    val contextData: String,
    val frequency: Int,
    val lastAccessed: Long,
    val createdAt: Long
)

/**
 * User interaction entity for conversation tracking
 */
@Entity(tableName = "user_interactions")
data class UserInteraction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userQuery: String,
    val aiResponse: String,
    val contextData: String,
    val responseStyle: String,
    val userSatisfaction: Float?,
    val timestamp: Long
)

/**
 * Contextual response entity for response tracking
 */
@Entity(tableName = "contextual_responses")
data class ContextualResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val response: String,
    val contextType: String,
    val confidence: Float,
    val userFeedback: String?,
    val timestamp: Long
)
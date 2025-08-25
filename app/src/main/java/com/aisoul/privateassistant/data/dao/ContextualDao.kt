package com.aisoul.privateassistant.data.dao

import androidx.room.*
import com.aisoul.privateassistant.data.entities.ContextualMemory
import com.aisoul.privateassistant.data.entities.UserInteraction
import com.aisoul.privateassistant.data.entities.ContextualResponse

/**
 * DAO for contextual AI operations
 */
@Dao
interface ContextualDao {
    
    // Contextual Memory
    @Query("SELECT * FROM contextual_memories ORDER BY frequency DESC, lastAccessed DESC")
    suspend fun getAllContextualMemories(): List<ContextualMemory>
    
    @Query("SELECT * FROM contextual_memories WHERE concept = :concept")
    suspend fun getContextualMemoryByConcept(concept: String): ContextualMemory?
    
    @Query("SELECT * FROM contextual_memories WHERE contextType = :contextType ORDER BY frequency DESC")
    suspend fun getContextualMemoriesByType(contextType: String): List<ContextualMemory>
    
    @Query("SELECT * FROM contextual_memories WHERE concept LIKE :searchTerm OR contextData LIKE :searchTerm ORDER BY frequency DESC LIMIT :limit")
    suspend fun searchContextualMemories(searchTerm: String, limit: Int): List<ContextualMemory>
    
    @Query("SELECT * FROM contextual_memories WHERE lastAccessed > :afterTimestamp ORDER BY lastAccessed DESC")
    suspend fun getRecentContextualMemories(afterTimestamp: Long): List<ContextualMemory>
    
    @Query("SELECT * FROM contextual_memories ORDER BY frequency DESC LIMIT :limit")
    suspend fun getTopContextualMemories(limit: Int): List<ContextualMemory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContextualMemory(contextualMemory: ContextualMemory): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContextualMemories(contextualMemories: List<ContextualMemory>)
    
    @Update
    suspend fun updateContextualMemory(contextualMemory: ContextualMemory)
    
    @Delete
    suspend fun deleteContextualMemory(contextualMemory: ContextualMemory)
    
    @Query("DELETE FROM contextual_memories WHERE lastAccessed < :beforeTimestamp AND frequency < :minFrequency")
    suspend fun cleanupOldMemories(beforeTimestamp: Long, minFrequency: Int): Int
    
    // User Interactions
    @Query("SELECT * FROM user_interactions ORDER BY timestamp DESC")
    suspend fun getAllUserInteractions(): List<UserInteraction>
    
    @Query("SELECT * FROM user_interactions WHERE timestamp > :afterTimestamp ORDER BY timestamp DESC")
    suspend fun getRecentUserInteractions(afterTimestamp: Long): List<UserInteraction>
    
    @Query("SELECT * FROM user_interactions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentUserInteractions(limit: Int): List<UserInteraction>
    
    @Query("SELECT * FROM user_interactions WHERE responseStyle = :responseStyle ORDER BY timestamp DESC")
    suspend fun getUserInteractionsByStyle(responseStyle: String): List<UserInteraction>
    
    @Query("SELECT * FROM user_interactions WHERE userQuery LIKE :searchTerm OR aiResponse LIKE :searchTerm ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchUserInteractions(searchTerm: String, limit: Int): List<UserInteraction>
    
    @Query("SELECT * FROM user_interactions WHERE userSatisfaction IS NOT NULL ORDER BY userSatisfaction DESC")
    suspend fun getUserInteractionsWithFeedback(): List<UserInteraction>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserInteraction(userInteraction: UserInteraction): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserInteractions(userInteractions: List<UserInteraction>)
    
    @Update
    suspend fun updateUserInteraction(userInteraction: UserInteraction)
    
    @Delete
    suspend fun deleteUserInteraction(userInteraction: UserInteraction)
    
    @Query("DELETE FROM user_interactions WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldUserInteractions(beforeTimestamp: Long): Int
    
    // Contextual Responses
    @Query("SELECT * FROM contextual_responses ORDER BY timestamp DESC")
    suspend fun getAllContextualResponses(): List<ContextualResponse>
    
    @Query("SELECT * FROM contextual_responses WHERE contextType = :contextType ORDER BY confidence DESC")
    suspend fun getContextualResponsesByType(contextType: String): List<ContextualResponse>
    
    @Query("SELECT * FROM contextual_responses WHERE query LIKE :searchTerm ORDER BY confidence DESC LIMIT :limit")
    suspend fun searchContextualResponses(searchTerm: String, limit: Int): List<ContextualResponse>
    
    @Query("SELECT * FROM contextual_responses WHERE userFeedback IS NOT NULL ORDER BY timestamp DESC")
    suspend fun getContextualResponsesWithFeedback(): List<ContextualResponse>
    
    @Query("SELECT * FROM contextual_responses ORDER BY confidence DESC LIMIT :limit")
    suspend fun getTopContextualResponses(limit: Int): List<ContextualResponse>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContextualResponse(contextualResponse: ContextualResponse): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContextualResponses(contextualResponses: List<ContextualResponse>)
    
    @Update
    suspend fun updateContextualResponse(contextualResponse: ContextualResponse)
    
    @Delete
    suspend fun deleteContextualResponse(contextualResponse: ContextualResponse)
    
    @Query("DELETE FROM contextual_responses WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldContextualResponses(beforeTimestamp: Long): Int
}
package com.aisoul.privateassistant.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.aisoul.privateassistant.data.entities.Conversation

@Dao
interface ConversationDao {
    
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<Conversation>>
    
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): Conversation?
    
    @Insert
    suspend fun insertConversation(conversation: Conversation): Long
    
    @Update
    suspend fun updateConversation(conversation: Conversation)
    
    @Delete
    suspend fun deleteConversation(conversation: Conversation)
    
    @Query("DELETE FROM conversations WHERE isArchived = 1")
    suspend fun deleteArchivedConversations()
    
    @Query("UPDATE conversations SET isArchived = 1 WHERE id = :id")
    suspend fun archiveConversation(id: Long)
    
    @Query("UPDATE conversations SET messageCount = messageCount + 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementMessageCount(id: Long, timestamp: Long = System.currentTimeMillis())
}
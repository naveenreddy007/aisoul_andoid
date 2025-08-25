package com.aisoul.privateassistant.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.aisoul.privateassistant.data.entities.Message

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): Message?
    
    @Insert
    suspend fun insertMessage(message: Message): Long
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Delete
    suspend fun deleteMessage(message: Message)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCountForConversation(conversationId: Long): Int
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForConversation(conversationId: Long): Message?
    
    // Search functionality
    @Query("SELECT * FROM messages WHERE content LIKE '%' || :searchTerm || '%' ORDER BY timestamp DESC")
    fun searchMessages(searchTerm: String): Flow<List<Message>>
}
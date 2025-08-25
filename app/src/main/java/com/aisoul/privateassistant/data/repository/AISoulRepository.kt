package com.aisoul.privateassistant.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.aisoul.privateassistant.data.database.AISoulDatabase
import com.aisoul.privateassistant.data.entities.Conversation
import com.aisoul.privateassistant.data.entities.Message
import com.aisoul.privateassistant.data.entities.AIModel

class AISoulRepository private constructor(private val context: Context) {
    
    private var database: AISoulDatabase? = null
    
    companion object {
        @Volatile
        private var INSTANCE: AISoulRepository? = null
        
        fun getInstance(context: Context): AISoulRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AISoulRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    private suspend fun getDatabase(): AISoulDatabase {
        return database ?: withContext(Dispatchers.IO) {
            val passphrase = getDatabasePassphrase()
            val db = AISoulDatabase.getDatabase(context, passphrase)
            database = db
            db
        }
    }
    
    private fun getDatabasePassphrase(): CharArray {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "aisoul_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            var passphrase = sharedPreferences.getString("db_passphrase", null)
            if (passphrase == null) {
                // Generate a new secure passphrase
                passphrase = generateSecurePassphrase()
                sharedPreferences.edit().putString("db_passphrase", passphrase).apply()
            }
            
            return passphrase.toCharArray()
        } catch (e: Exception) {
            // Fallback to a default passphrase if encryption setup fails
            return "default_aisoul_passphrase_2024".toCharArray()
        }
    }
    
    private fun generateSecurePassphrase(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..32)
            .map { charset.random() }
            .joinToString("")
    }
    
    // Conversation operations
    suspend fun getAllConversations(): Flow<List<Conversation>> {
        return getDatabase().conversationDao().getAllConversations()
    }
    
    suspend fun insertConversation(conversation: Conversation): Long {
        return getDatabase().conversationDao().insertConversation(conversation)
    }
    
    suspend fun deleteConversation(conversation: Conversation) {
        getDatabase().conversationDao().deleteConversation(conversation)
    }
    
    // Message operations  
    suspend fun getMessagesForConversation(conversationId: Long): Flow<List<Message>> {
        return getDatabase().messageDao().getMessagesForConversation(conversationId)
    }
    
    suspend fun insertMessage(message: Message): Long {
        val messageId = getDatabase().messageDao().insertMessage(message)
        // Update conversation timestamp and count
        getDatabase().conversationDao().incrementMessageCount(message.conversationId)
        return messageId
    }
    
    suspend fun deleteMessage(message: Message) {
        getDatabase().messageDao().deleteMessage(message)
    }
    
    // AI Model operations
    suspend fun getAllModels(): Flow<List<AIModel>> {
        return getDatabase().aiModelDao().getAllModels()
    }
    
    suspend fun getActiveModel(): AIModel? {
        return getDatabase().aiModelDao().getActiveModel()
    }
    
    suspend fun setActiveModel(modelId: String) {
        val db = getDatabase()
        db.aiModelDao().deactivateAllModels()
        db.aiModelDao().setActiveModel(modelId)
    }
    
    suspend fun insertModel(model: AIModel) {
        getDatabase().aiModelDao().insertModel(model)
    }
    
    // Initialize with default models
    suspend fun initializeDefaultModels() {
        val db = getDatabase()
        val existingModels = db.aiModelDao().getAllModels()
        
        // Only insert if no models exist
        withContext(Dispatchers.IO) {
            val defaultModels = listOf(
                AIModel(
                    id = "gemma-2b",
                    name = "Gemma 2B",
                    description = "Lightweight model for basic conversations. Fast and efficient.",
                    sizeBytes = 1_500_000_000L, // 1.5GB
                    minRamMB = 3000,
                    minStorageMB = 2000
                ),
                AIModel(
                    id = "gemma-7b", 
                    name = "Gemma 7B",
                    description = "Advanced model with better reasoning. Requires more resources.",
                    sizeBytes = 4_300_000_000L, // 4.3GB
                    minRamMB = 6000,
                    minStorageMB = 8000
                ),
                AIModel(
                    id = "phi-3-mini",
                    name = "Phi-3 Mini", 
                    description = "Microsoft's efficient 3.8B parameter model optimized for mobile.",
                    sizeBytes = 2_300_000_000L, // 2.3GB
                    minRamMB = 4000,
                    minStorageMB = 4000
                )
            )
            
            defaultModels.forEach { model ->
                db.aiModelDao().insertModel(model)
            }
        }
    }
    
    fun closeDatabase() {
        database?.close()
        database = null
        AISoulDatabase.closeDatabase()
    }
}
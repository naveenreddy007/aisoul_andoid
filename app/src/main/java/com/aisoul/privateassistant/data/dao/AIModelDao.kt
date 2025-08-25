package com.aisoul.privateassistant.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.aisoul.privateassistant.data.entities.AIModel

@Dao
interface AIModelDao {
    
    @Query("SELECT * FROM ai_models ORDER BY name ASC")
    fun getAllModels(): Flow<List<AIModel>>
    
    @Query("SELECT * FROM ai_models WHERE isDownloaded = 1 ORDER BY name ASC")
    fun getDownloadedModels(): Flow<List<AIModel>>
    
    @Query("SELECT * FROM ai_models WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveModel(): AIModel?
    
    @Query("SELECT * FROM ai_models WHERE id = :id")
    suspend fun getModelById(id: String): AIModel?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: AIModel)
    
    @Update
    suspend fun updateModel(model: AIModel)
    
    @Delete
    suspend fun deleteModel(model: AIModel)
    
    @Query("UPDATE ai_models SET isActive = 0")
    suspend fun deactivateAllModels()
    
    @Query("UPDATE ai_models SET isActive = 1 WHERE id = :modelId")
    suspend fun setActiveModel(modelId: String)
    
    @Query("UPDATE ai_models SET isDownloaded = 1, downloadPath = :path, downloadedAt = :timestamp, checksumSha256 = :checksum WHERE id = :modelId")
    suspend fun markModelAsDownloaded(modelId: String, path: String, timestamp: Long, checksum: String)
    
    @Query("DELETE FROM ai_models WHERE isDownloaded = 0")
    suspend fun deleteUndownloadedModels()
}
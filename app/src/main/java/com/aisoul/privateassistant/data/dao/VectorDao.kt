package com.aisoul.privateassistant.data.dao

import androidx.room.*
import com.aisoul.privateassistant.data.entities.*

/**
 * DAO for vector database operations
 */
@Dao
interface VectorDao {
    
    // Vector Embeddings
    @Query("SELECT * FROM vector_embeddings ORDER BY timestamp DESC")
    suspend fun getAllVectorEmbeddings(): List<VectorEmbedding>
    
    @Query("SELECT * FROM vector_embeddings WHERE documentId = :documentId ORDER BY timestamp DESC")
    suspend fun getVectorEmbeddingsByDocument(documentId: String): List<VectorEmbedding>
    
    @Query("SELECT * FROM vector_embeddings WHERE vectorId = :vectorId")
    suspend fun getVectorEmbeddingById(vectorId: String): VectorEmbedding?
    
    @Query("SELECT * FROM vector_embeddings WHERE dimension = :dimension ORDER BY timestamp DESC")
    suspend fun getVectorEmbeddingsByDimension(dimension: Int): List<VectorEmbedding>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVectorEmbedding(vectorEmbedding: VectorEmbedding): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVectorEmbeddings(vectorEmbeddings: List<VectorEmbedding>)
    
    @Update
    suspend fun updateVectorEmbedding(vectorEmbedding: VectorEmbedding)
    
    @Delete
    suspend fun deleteVectorEmbedding(vectorEmbedding: VectorEmbedding)
    
    @Query("DELETE FROM vector_embeddings WHERE vectorId = :vectorId")
    suspend fun deleteVectorEmbedding(vectorId: String): Int
    
    @Query("DELETE FROM vector_embeddings WHERE documentId = :documentId")
    suspend fun deleteVectorEmbeddingsByDocument(documentId: String): Int
    
    // Document Chunks
    @Query("SELECT * FROM document_chunks ORDER BY timestamp DESC")
    suspend fun getAllDocumentChunks(): List<DocumentChunk>
    
    @Query("SELECT * FROM document_chunks WHERE documentId = :documentId ORDER BY startPosition ASC")
    suspend fun getDocumentChunksByDocument(documentId: String): List<DocumentChunk>
    
    @Query("SELECT * FROM document_chunks WHERE chunkId = :chunkId")
    suspend fun getDocumentChunkByChunkId(chunkId: String): DocumentChunk?
    
    @Query("SELECT * FROM document_chunks WHERE content LIKE :searchTerm ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchDocumentChunks(searchTerm: String, limit: Int): List<DocumentChunk>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentChunk(documentChunk: DocumentChunk): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentChunks(documentChunks: List<DocumentChunk>)
    
    @Update
    suspend fun updateDocumentChunk(documentChunk: DocumentChunk)
    
    @Delete
    suspend fun deleteDocumentChunk(documentChunk: DocumentChunk)
    
    @Query("DELETE FROM document_chunks WHERE chunkId = :chunkId")
    suspend fun deleteDocumentChunk(chunkId: String): Int
    
    @Query("DELETE FROM document_chunks WHERE documentId = :documentId")
    suspend fun deleteDocumentChunksByDocument(documentId: String): Int
    
    // Semantic Search Results
    @Query("SELECT * FROM semantic_search_results ORDER BY timestamp DESC")
    suspend fun getAllSearchResults(): List<SemanticSearchResult>
    
    @Query("SELECT * FROM semantic_search_results WHERE query = :query ORDER BY timestamp DESC")
    suspend fun getSearchResultsByQuery(query: String): List<SemanticSearchResult>
    
    @Query("SELECT * FROM semantic_search_results WHERE timestamp > :afterTimestamp ORDER BY timestamp DESC")
    suspend fun getRecentSearchResults(afterTimestamp: Long): List<SemanticSearchResult>
    
    @Query("SELECT * FROM semantic_search_results ORDER BY topScore DESC LIMIT :limit")
    suspend fun getTopSearchResults(limit: Int): List<SemanticSearchResult>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResult(searchResult: SemanticSearchResult): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<SemanticSearchResult>)
    
    @Update
    suspend fun updateSearchResult(searchResult: SemanticSearchResult)
    
    @Delete
    suspend fun deleteSearchResult(searchResult: SemanticSearchResult)
    
    @Query("DELETE FROM semantic_search_results WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldSearchResults(beforeTimestamp: Long): Int
    
    // Search Sessions
    @Query("SELECT * FROM search_sessions ORDER BY timestamp DESC")
    suspend fun getAllSearchSessions(): List<SearchSession>
    
    @Query("SELECT * FROM search_sessions WHERE scope = :scope ORDER BY timestamp DESC")
    suspend fun getSearchSessionsByScope(scope: String): List<SearchSession>
    
    @Query("SELECT * FROM search_sessions WHERE timestamp > :daysAgo ORDER BY timestamp DESC")
    suspend fun getRecentSearchSessions(daysAgo: Long): List<SearchSession>
    
    @Query("SELECT * FROM search_sessions WHERE query LIKE :searchTerm ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchSessions(searchTerm: String, limit: Int): List<SearchSession>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchSession(searchSession: SearchSession): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchSessions(searchSessions: List<SearchSession>)
    
    @Update
    suspend fun updateSearchSession(searchSession: SearchSession)
    
    @Delete
    suspend fun deleteSearchSession(searchSession: SearchSession)
    
    @Query("DELETE FROM search_sessions WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldSearchSessions(beforeTimestamp: Long): Int
    
    // Search Analytics
    @Query("SELECT * FROM search_analytics ORDER BY timestamp DESC")
    suspend fun getAllSearchAnalytics(): List<SearchAnalytics>
    
    @Query("SELECT * FROM search_analytics WHERE period = :period ORDER BY timestamp DESC")
    suspend fun getSearchAnalyticsByPeriod(period: String): List<SearchAnalytics>
    
    @Query("SELECT * FROM search_analytics ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSearchAnalytics(): SearchAnalytics?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchAnalytics(searchAnalytics: SearchAnalytics): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchAnalyticsList(searchAnalyticsList: List<SearchAnalytics>)
    
    @Update
    suspend fun updateSearchAnalytics(searchAnalytics: SearchAnalytics)
    
    @Delete
    suspend fun deleteSearchAnalytics(searchAnalytics: SearchAnalytics)
    
    @Query("DELETE FROM search_analytics WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldSearchAnalytics(beforeTimestamp: Long): Int
}
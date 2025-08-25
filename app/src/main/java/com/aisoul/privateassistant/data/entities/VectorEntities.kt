package com.aisoul.privateassistant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Vector embedding entity for semantic search
 */
@Entity(tableName = "vector_embeddings")
data class VectorEmbedding(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vectorId: String,
    val documentId: String,
    val embedding: String, // Comma-separated float values
    val dimension: Int,
    val timestamp: Long
)

/**
 * Document chunk entity for vector storage
 */
@Entity(tableName = "document_chunks")
data class DocumentChunk(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chunkId: String,
    val documentId: String,
    val content: String,
    val startPosition: Int,
    val endPosition: Int,
    val metadata: String,
    val timestamp: Long
)

/**
 * Semantic search result entity for analytics
 */
@Entity(tableName = "semantic_search_results")
data class SemanticSearchResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val resultsCount: Int,
    val topScore: Float,
    val executionTime: Long,
    val timestamp: Long
)

/**
 * Search session entity for tracking user searches
 */
@Entity(tableName = "search_sessions")
data class SearchSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val scope: String,
    val resultsCount: Int,
    val executionTime: Long,
    val timestamp: Long
)

/**
 * Search analytics entity for performance tracking
 */
@Entity(tableName = "search_analytics")
data class SearchAnalytics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val totalSearches: Int,
    val avgResultsPerSearch: Float,
    val avgExecutionTime: Long,
    val topQueries: String,
    val period: String,
    val timestamp: Long
)
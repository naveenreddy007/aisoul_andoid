package com.aisoul.privateassistant.intelligence

import android.content.Context
import com.aisoul.privateassistant.ai.AIInferenceEngine
import com.aisoul.privateassistant.data.database.AppDatabase
import com.aisoul.privateassistant.data.entities.VectorEmbedding
import com.aisoul.privateassistant.data.entities.DocumentChunk
import com.aisoul.privateassistant.data.entities.SemanticSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import kotlin.math.*

/**
 * Vector Database Implementation with Local FAISS-like functionality
 * Provides semantic search capabilities for AI assistant context retrieval
 */
class VectorDatabase private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: VectorDatabase? = null
        
        fun getInstance(context: Context): VectorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VectorDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val EMBEDDING_DIMENSION = 384 // Lightweight model dimension
        private const val MAX_CHUNK_SIZE = 512 // Maximum tokens per chunk
        private const val OVERLAP_SIZE = 50 // Overlap between chunks
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val aiInferenceEngine = AIInferenceEngine.getInstance(context)
    
    // In-memory vector index (simplified FAISS alternative)
    private var vectorIndex: MutableMap<String, FloatArray> = mutableMapOf()
    private var documentMapping: MutableMap<String, DocumentMetadata> = mutableMapOf()
    
    // State management
    private val _indexState = MutableStateFlow(IndexState.IDLE)
    val indexState: StateFlow<IndexState> = _indexState.asStateFlow()
    
    private val _indexStats = MutableStateFlow(IndexStats(0, 0, 0L))
    val indexStats: StateFlow<IndexStats> = _indexStats.asStateFlow()
    
    enum class IndexState {
        IDLE,
        BUILDING,
        UPDATING,
        SEARCHING,
        COMPLETE,
        ERROR
    }
    
    data class IndexStats(
        val totalVectors: Int,
        val totalDocuments: Int,
        val indexSize: Long
    )
    
    data class DocumentMetadata(
        val id: String,
        val title: String,
        val content: String,
        val source: String,
        val timestamp: Long,
        val chunkIds: List<String>
    )
    
    data class SearchQuery(
        val text: String,
        val embedding: FloatArray,
        val filters: Map<String, String> = emptyMap(),
        val topK: Int = 10,
        val threshold: Float = 0.5f
    )
    
    data class SearchResult(
        val documentId: String,
        val chunkId: String,
        val content: String,
        val score: Float,
        val metadata: Map<String, String>
    )
    
    /**
     * Initialize vector database and load existing index
     */
    suspend fun initialize() {
        _indexState.value = IndexState.BUILDING
        
        try {
            // Load existing vectors from database
            loadExistingVectors()
            
            // Build in-memory index
            buildInMemoryIndex()
            
            _indexState.value = IndexState.COMPLETE
            updateIndexStats()
            
        } catch (e: Exception) {
            _indexState.value = IndexState.ERROR
            throw e
        }
    }
    
    /**
     * Add document to vector database
     */
    suspend fun addDocument(
        id: String,
        title: String,
        content: String,
        source: String,
        metadata: Map<String, String> = emptyMap()
    ): Boolean {
        _indexState.value = IndexState.UPDATING
        
        try {
            // Split document into chunks
            val chunks = chunkDocument(content)
            val chunkIds = mutableListOf<String>()
            
            // Process each chunk
            chunks.forEachIndexed { index, chunk ->
                val chunkId = "${id}_chunk_$index"
                chunkIds.add(chunkId)
                
                // Generate embedding for chunk
                val embedding = generateEmbedding(chunk.text)
                
                if (embedding != null) {
                    // Store in database
                    val vectorEmbedding = VectorEmbedding(
                        id = 0,
                        vectorId = chunkId,
                        documentId = id,
                        embedding = embedding.joinToString(","),
                        dimension = embedding.size,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    val documentChunk = DocumentChunk(
                        id = 0,
                        chunkId = chunkId,
                        documentId = id,
                        content = chunk.text,
                        startPosition = chunk.startPosition,
                        endPosition = chunk.endPosition,
                        metadata = metadata.map { "${it.key}:${it.value}" }.joinToString(";"),
                        timestamp = System.currentTimeMillis()
                    )
                    
                    database.vectorDao().insertVectorEmbedding(vectorEmbedding)
                    database.vectorDao().insertDocumentChunk(documentChunk)
                    
                    // Add to in-memory index
                    vectorIndex[chunkId] = embedding
                }
            }
            
            // Store document metadata
            documentMapping[id] = DocumentMetadata(
                id = id,
                title = title,
                content = content,
                source = source,
                timestamp = System.currentTimeMillis(),
                chunkIds = chunkIds
            )
            
            _indexState.value = IndexState.COMPLETE
            updateIndexStats()
            
            return true
            
        } catch (e: Exception) {
            _indexState.value = IndexState.ERROR
            return false
        }
    }
    
    /**
     * Perform semantic search
     */
    suspend fun search(
        query: String,
        topK: Int = 10,
        threshold: Float = 0.5f,
        filters: Map<String, String> = emptyMap()
    ): List<SearchResult> {
        _indexState.value = IndexState.SEARCHING
        
        try {
            // Generate query embedding
            val queryEmbedding = generateEmbedding(query)
            if (queryEmbedding == null) {
                _indexState.value = IndexState.ERROR
                return emptyList()
            }
            
            // Search similar vectors
            val similarResults = findSimilarVectors(queryEmbedding, topK * 2, threshold)
            
            // Apply filters and prepare results
            val filteredResults = applyFilters(similarResults, filters)
                .take(topK)
                .map { (chunkId, score) ->
                    val chunk = database.vectorDao().getDocumentChunkByChunkId(chunkId)
                    SearchResult(
                        documentId = chunk?.documentId ?: "",
                        chunkId = chunkId,
                        content = chunk?.content ?: "",
                        score = score,
                        metadata = parseMetadata(chunk?.metadata ?: "")
                    )
                }
            
            // Store search result for analytics
            val searchResult = SemanticSearchResult(
                id = 0,
                query = query,
                resultsCount = filteredResults.size,
                topScore = filteredResults.maxOfOrNull { it.score } ?: 0f,
                executionTime = 0L, // Could be measured
                timestamp = System.currentTimeMillis()
            )
            database.vectorDao().insertSearchResult(searchResult)
            
            _indexState.value = IndexState.COMPLETE
            return filteredResults
            
        } catch (e: Exception) {
            _indexState.value = IndexState.ERROR
            return emptyList()
        }
    }
    
    /**
     * Find similar documents based on content
     */
    suspend fun findSimilarDocuments(
        documentId: String,
        topK: Int = 5
    ): List<SearchResult> {
        val document = documentMapping[documentId] ?: return emptyList()
        
        // Use document content as query
        return search(document.content, topK)
            .filter { it.documentId != documentId } // Exclude self
    }
    
    /**
     * Get document recommendations based on user context
     */
    suspend fun getRecommendations(
        userContext: String,
        topK: Int = 5
    ): List<SearchResult> {
        return search(userContext, topK, threshold = 0.3f)
    }
    
    /**
     * Update document in vector database
     */
    suspend fun updateDocument(
        id: String,
        title: String,
        content: String,
        source: String,
        metadata: Map<String, String> = emptyMap()
    ): Boolean {
        // Remove existing document
        removeDocument(id)
        
        // Add updated document
        return addDocument(id, title, content, source, metadata)
    }
    
    /**
     * Remove document from vector database
     */
    suspend fun removeDocument(id: String): Boolean {
        try {
            val document = documentMapping[id] ?: return false
            
            // Remove chunks from database
            document.chunkIds.forEach { chunkId ->
                database.vectorDao().deleteVectorEmbedding(chunkId)
                database.vectorDao().deleteDocumentChunk(chunkId)
                vectorIndex.remove(chunkId)
            }
            
            // Remove document metadata
            documentMapping.remove(id)
            
            updateIndexStats()
            return true
            
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Get index statistics
     */
    fun getIndexStatistics(): IndexStats {
        val totalVectors = vectorIndex.size
        val totalDocuments = documentMapping.size
        val indexSize = calculateIndexSize()
        
        return IndexStats(totalVectors, totalDocuments, indexSize)
    }
    
    /**
     * Optimize index (remove duplicates, compress, etc.)
     */
    suspend fun optimizeIndex(): Boolean {
        _indexState.value = IndexState.UPDATING
        
        try {
            // Remove duplicate vectors
            removeDuplicateVectors()
            
            // Compress index if needed
            compressIndex()
            
            _indexState.value = IndexState.COMPLETE
            updateIndexStats()
            
            return true
        } catch (e: Exception) {
            _indexState.value = IndexState.ERROR
            return false
        }
    }
    
    /**
     * Export index to file
     */
    suspend fun exportIndex(filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                val outputStream = FileOutputStream(file)
                
                // Export vector index
                vectorIndex.forEach { (id, embedding) ->
                    val line = "$id:${embedding.joinToString(",")}\n"
                    outputStream.write(line.toByteArray())
                }
                
                outputStream.close()
                true
            } catch (e: IOException) {
                false
            }
        }
    }
    
    /**
     * Import index from file
     */
    suspend fun importIndex(filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) return@withContext false
                
                file.readLines().forEach { line ->
                    val parts = line.split(":")
                    if (parts.size == 2) {
                        val id = parts[0]
                        val embedding = parts[1].split(",").map { it.toFloat() }.toFloatArray()
                        vectorIndex[id] = embedding
                    }
                }
                
                updateIndexStats()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Private helper methods
    
    /**
     * Load existing vectors from database
     */
    private suspend fun loadExistingVectors() {
        val embeddings = database.vectorDao().getAllVectorEmbeddings()
        
        embeddings.forEach { embedding ->
            val vector = embedding.embedding.split(",").map { it.toFloat() }.toFloatArray()
            vectorIndex[embedding.vectorId] = vector
        }
        
        // Load document metadata
        val chunks = database.vectorDao().getAllDocumentChunks()
        val documentsData = chunks.groupBy { it.documentId }
        
        documentsData.forEach { (docId, docChunks) ->
            val firstChunk = docChunks.first()
            documentMapping[docId] = DocumentMetadata(
                id = docId,
                title = docId, // Could be stored separately
                content = docChunks.joinToString(" ") { it.content },
                source = "database",
                timestamp = firstChunk.timestamp,
                chunkIds = docChunks.map { it.chunkId }
            )
        }
    }
    
    /**
     * Build in-memory index for fast search
     */
    private fun buildInMemoryIndex() {
        // The vectorIndex map already serves as our in-memory index
        // For more advanced indexing, we could implement:
        // - LSH (Locality Sensitive Hashing)
        // - Product Quantization
        // - Hierarchical clustering
    }
    
    /**
     * Split document into chunks
     */
    private fun chunkDocument(content: String): List<TextChunk> {
        val words = content.split(Regex("\\s+"))
        val chunks = mutableListOf<TextChunk>()
        
        var currentChunk = mutableListOf<String>()
        var startPosition = 0
        
        for ((index, word) in words.withIndex()) {
            currentChunk.add(word)
            
            if (currentChunk.size >= MAX_CHUNK_SIZE) {
                val chunkText = currentChunk.joinToString(" ")
                chunks.add(TextChunk(
                    text = chunkText,
                    startPosition = startPosition,
                    endPosition = startPosition + chunkText.length
                ))
                
                // Create overlap
                val overlapStart = maxOf(0, currentChunk.size - OVERLAP_SIZE)
                currentChunk = currentChunk.subList(overlapStart, currentChunk.size).toMutableList()
                startPosition += chunkText.length - currentChunk.joinToString(" ").length
            }
        }
        
        // Add remaining words as final chunk
        if (currentChunk.isNotEmpty()) {
            val chunkText = currentChunk.joinToString(" ")
            chunks.add(TextChunk(
                text = chunkText,
                startPosition = startPosition,
                endPosition = startPosition + chunkText.length
            ))
        }
        
        return chunks
    }
    
    data class TextChunk(
        val text: String,
        val startPosition: Int,
        val endPosition: Int
    )
    
    /**
     * Generate embedding for text (simplified implementation)
     */
    private suspend fun generateEmbedding(text: String): FloatArray? {
        try {
            // This is a simplified embedding generation
            // In a real implementation, you would use:
            // - Sentence transformers
            // - Word2Vec/GloVe
            // - BERT embeddings
            // - Custom neural network
            
            return generateSimpleEmbedding(text)
            
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Generate simple embedding based on text features
     */
    private fun generateSimpleEmbedding(text: String): FloatArray {
        val embedding = FloatArray(EMBEDDING_DIMENSION)
        val words = text.lowercase().split(Regex("\\W+")).filter { it.isNotEmpty() }
        
        // Simple TF-IDF like features
        val wordCounts = words.groupingBy { it }.eachCount()
        val totalWords = words.size
        
        // Hash words to embedding dimensions
        wordCounts.forEach { (word, count) ->
            val hash = word.hashCode()
            val index = abs(hash) % EMBEDDING_DIMENSION
            val tf = count.toFloat() / totalWords
            embedding[index] += tf
        }
        
        // Add some text statistics as features
        if (EMBEDDING_DIMENSION > 10) {
            embedding[EMBEDDING_DIMENSION - 10] = text.length.toFloat() / 1000f // Length feature
            embedding[EMBEDDING_DIMENSION - 9] = words.size.toFloat() / 100f // Word count feature
            embedding[EMBEDDING_DIMENSION - 8] = words.distinct().size.toFloat() / words.size // Diversity feature
            
            // Character-level features
            embedding[EMBEDDING_DIMENSION - 7] = text.count { it.isUpperCase() }.toFloat() / text.length
            embedding[EMBEDDING_DIMENSION - 6] = text.count { it.isDigit() }.toFloat() / text.length
            embedding[EMBEDDING_DIMENSION - 5] = text.count { it in "!?." }.toFloat() / text.length
        }
        
        // Normalize embedding
        val norm = sqrt(embedding.sumOf { (it * it).toDouble() }.toFloat())
        if (norm > 0) {
            for (i in embedding.indices) {
                embedding[i] = embedding[i] / norm
            }
        }
        
        return embedding
    }
    
    /**
     * Find similar vectors using cosine similarity
     */
    private fun findSimilarVectors(
        queryEmbedding: FloatArray,
        topK: Int,
        threshold: Float
    ): List<Pair<String, Float>> {
        val similarities = mutableListOf<Pair<String, Float>>()
        
        vectorIndex.forEach { (id, embedding) ->
            val similarity = cosineSimilarity(queryEmbedding, embedding)
            if (similarity >= threshold) {
                similarities.add(id to similarity)
            }
        }
        
        return similarities.sortedByDescending { it.second }.take(topK)
    }
    
    /**
     * Calculate cosine similarity between two vectors
     */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        
        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0) dotProduct / denominator else 0f
    }
    
    /**
     * Apply filters to search results
     */
    private suspend fun applyFilters(
        results: List<Pair<String, Float>>,
        filters: Map<String, String>
    ): List<Pair<String, Float>> {
        if (filters.isEmpty()) return results
        
        return results.filter { (chunkId, _) ->
            val chunk = database.vectorDao().getDocumentChunkByChunkId(chunkId)
            val metadata = parseMetadata(chunk?.metadata ?: "")
            
            filters.all { (key, value) ->
                metadata[key]?.equals(value, ignoreCase = true) == true
            }
        }
    }
    
    /**
     * Parse metadata string
     */
    private fun parseMetadata(metadataString: String): Map<String, String> {
        if (metadataString.isEmpty()) return emptyMap()
        
        return metadataString.split(";")
            .mapNotNull { pair ->
                val parts = pair.split(":")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
    }
    
    /**
     * Update index statistics
     */
    private fun updateIndexStats() {
        val stats = getIndexStatistics()
        _indexStats.value = stats
    }
    
    /**
     * Calculate index size in bytes
     */
    private fun calculateIndexSize(): Long {
        var size = 0L
        
        // Vector index size
        vectorIndex.forEach { (id, embedding) ->
            size += id.length * 2 // String overhead
            size += embedding.size * 4 // Float size
        }
        
        // Document mapping size
        documentMapping.forEach { (id, metadata) ->
            size += id.length * 2
            size += metadata.title.length * 2
            size += metadata.content.length * 2
            size += metadata.source.length * 2
            size += metadata.chunkIds.sumOf { it.length * 2 }
        }
        
        return size
    }
    
    /**
     * Remove duplicate vectors
     */
    private fun removeDuplicateVectors() {
        val uniqueVectors = mutableMapOf<String, String>()
        val toRemove = mutableListOf<String>()
        
        vectorIndex.forEach { (id, embedding) ->
            val embeddingHash = embedding.contentHashCode().toString()
            val existingId = uniqueVectors[embeddingHash]
            
            if (existingId != null) {
                toRemove.add(id)
            } else {
                uniqueVectors[embeddingHash] = id
            }
        }
        
        toRemove.forEach { vectorIndex.remove(it) }
    }
    
    /**
     * Compress index (placeholder for future implementation)
     */
    private fun compressIndex() {
        // Could implement:
        // - Quantization
        // - Dimensionality reduction
        // - Sparse representation
    }
    
    /**
     * Generate hash for content deduplication
     */
    private fun generateContentHash(content: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(content.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
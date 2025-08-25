package com.aisoul.privateassistant.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import com.aisoul.privateassistant.data.entities.*
import com.aisoul.privateassistant.data.dao.*

@Database(
    entities = [
        Conversation::class, 
        Message::class, 
        AIModel::class,
        NotificationRecord::class,
        NotificationInsight::class,
        SmsRecord::class,
        ContactInfo::class,
        SmartResponse::class,
        AppUsageRecord::class,
        UsageInsight::class,
        ProductivityScore::class,
        ContextualMemory::class,
        UserInteraction::class,
        ContextualResponse::class,
        VectorEmbedding::class,
        DocumentChunk::class,
        SemanticSearchResult::class,
        SearchSession::class,
        SearchAnalytics::class,
        ErrorLog::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AISoulDatabase : RoomDatabase() {
    
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun aiModelDao(): AIModelDao
    abstract fun notificationDao(): NotificationDao
    abstract fun smsDao(): SmsDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun contextualDao(): ContextualDao
    abstract fun vectorDao(): VectorDao
    abstract fun errorDao(): ErrorDao
    
    companion object {
        @Volatile
        private var INSTANCE: AISoulDatabase? = null
        
        fun getDatabase(context: Context, passphrase: CharArray): AISoulDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase))
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AISoulDatabase::class.java,
                    "aisoul_database"
                )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with available AI models
                        // This will be called on a background thread
                    }
                })
                .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new tables for Sprint 3 features
                
                // Notification tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        packageName TEXT NOT NULL,
                        title TEXT NOT NULL,
                        text TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        priority TEXT NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_insights (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        actionable INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                // SMS tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sms_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        messageId INTEGER NOT NULL,
                        threadId INTEGER NOT NULL,
                        address TEXT NOT NULL,
                        body TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        type INTEGER NOT NULL,
                        contactName TEXT
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS contact_info (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        phoneNumber TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        communicationFrequency INTEGER NOT NULL,
                        lastContact INTEGER NOT NULL,
                        relationship TEXT
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS smart_responses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        threadId INTEGER NOT NULL,
                        contactNumber TEXT NOT NULL,
                        responseText TEXT NOT NULL,
                        responseType TEXT NOT NULL,
                        confidence REAL NOT NULL,
                        context TEXT NOT NULL,
                        tone TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                // App usage tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_usage_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        packageName TEXT NOT NULL,
                        appName TEXT NOT NULL,
                        category TEXT NOT NULL,
                        totalTime INTEGER NOT NULL,
                        launchCount INTEGER NOT NULL,
                        lastUsed INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS usage_insights (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        value REAL NOT NULL,
                        trend REAL NOT NULL,
                        actionable INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS productivity_scores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        score REAL NOT NULL,
                        productiveTime INTEGER NOT NULL,
                        distractiveTime INTEGER NOT NULL,
                        focusScore REAL NOT NULL,
                        sessionQuality REAL NOT NULL,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                // Contextual AI tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS contextual_memories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        concept TEXT NOT NULL,
                        contextType TEXT NOT NULL,
                        contextData TEXT NOT NULL,
                        frequency INTEGER NOT NULL,
                        lastAccessed INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_interactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userQuery TEXT NOT NULL,
                        aiResponse TEXT NOT NULL,
                        contextData TEXT NOT NULL,
                        responseStyle TEXT NOT NULL,
                        userSatisfaction REAL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS contextual_responses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        query TEXT NOT NULL,
                        response TEXT NOT NULL,
                        contextType TEXT NOT NULL,
                        confidence REAL NOT NULL,
                        userFeedback TEXT,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                // Vector database tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS vector_embeddings (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        vectorId TEXT NOT NULL,
                        documentId TEXT NOT NULL,
                        embedding TEXT NOT NULL,
                        dimension INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS document_chunks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        chunkId TEXT NOT NULL,
                        documentId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        startPosition INTEGER NOT NULL,
                        endPosition INTEGER NOT NULL,
                        metadata TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS semantic_search_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        query TEXT NOT NULL,
                        resultsCount INTEGER NOT NULL,
                        topScore REAL NOT NULL,
                        executionTime INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS search_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        query TEXT NOT NULL,
                        scope TEXT NOT NULL,
                        resultsCount INTEGER NOT NULL,
                        executionTime INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS search_analytics (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        totalSearches INTEGER NOT NULL,
                        avgResultsPerSearch REAL NOT NULL,
                        avgExecutionTime INTEGER NOT NULL,
                        topQueries TEXT NOT NULL,
                        period TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create error logs table for Sprint 4
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS error_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        errorId TEXT NOT NULL,
                        level TEXT NOT NULL,
                        category TEXT NOT NULL,
                        message TEXT NOT NULL,
                        stackTrace TEXT,
                        timestamp INTEGER NOT NULL,
                        context TEXT,
                        userAction TEXT,
                        deviceInfo TEXT NOT NULL,
                        appVersion TEXT NOT NULL,
                        resolved INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}
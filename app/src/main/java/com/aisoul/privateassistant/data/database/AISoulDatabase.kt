package com.aisoul.privateassistant.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import com.aisoul.privateassistant.data.entities.Conversation
import com.aisoul.privateassistant.data.entities.Message
import com.aisoul.privateassistant.data.entities.AIModel
import com.aisoul.privateassistant.data.dao.ConversationDao
import com.aisoul.privateassistant.data.dao.MessageDao
import com.aisoul.privateassistant.data.dao.AIModelDao

@Database(
    entities = [Conversation::class, Message::class, AIModel::class],
    version = 1,
    exportSchema = false
)
abstract class AISoulDatabase : RoomDatabase() {
    
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun aiModelDao(): AIModelDao
    
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
    }
}
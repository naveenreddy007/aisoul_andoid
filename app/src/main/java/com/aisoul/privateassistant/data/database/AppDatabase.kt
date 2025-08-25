package com.aisoul.privateassistant.data.database

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Application-level database singleton wrapper
 * Provides easy access to the encrypted database throughout the app
 */
object AppDatabase {
    
    @Volatile
    private var database: AISoulDatabase? = null
    
    private const val PREF_NAME = "database_prefs"
    private const val PASSPHRASE_KEY = "db_passphrase"
    
    /**
     * Get database instance with automatic passphrase management
     */
    fun getDatabase(context: Context): AISoulDatabase {
        return database ?: synchronized(this) {
            database ?: createDatabase(context).also { database = it }
        }
    }
    
    /**
     * Create encrypted database with secure passphrase
     */
    private fun createDatabase(context: Context): AISoulDatabase {
        val passphrase = getOrCreatePassphrase(context)
        return AISoulDatabase.getDatabase(context, passphrase)
    }
    
    /**
     * Get or create secure database passphrase
     */
    private fun getOrCreatePassphrase(context: Context): CharArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        var passphrase = sharedPreferences.getString(PASSPHRASE_KEY, null)
        
        if (passphrase == null) {
            // Generate new secure passphrase
            passphrase = generateSecurePassphrase()
            sharedPreferences.edit()
                .putString(PASSPHRASE_KEY, passphrase)
                .apply()
        }
        
        return passphrase.toCharArray()
    }
    
    /**
     * Generate cryptographically secure passphrase
     */
    private fun generateSecurePassphrase(): String {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(32) // 256-bit key
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Close database connection
     */
    fun closeDatabase() {
        database?.close()
        database = null
        AISoulDatabase.closeDatabase()
    }
}
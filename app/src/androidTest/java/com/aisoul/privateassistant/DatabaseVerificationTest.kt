package com.aisoul.privateassistant

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aisoul.privateassistant.data.database.DatabaseVerification
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test to verify Sprint 1 database functionality.
 * 
 * This test runs on the Android device/emulator and verifies that:
 * - Encrypted database is properly initialized
 * - Basic CRUD operations work as expected
 * - Demo mode is functional
 */
@RunWith(AndroidJUnit4::class)
class DatabaseVerificationTest {

    @Test
    fun testDatabaseVerification() = runTest {
        // Context of the app under test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Verify that the app package is correct
        assertEquals("com.aisoul.privateassistant", appContext.packageName)
        
        // Run database verification
        val result = DatabaseVerification.verifyDatabase(appContext)
        
        // Assert that database verification was successful
        assertTrue("Database verification should succeed", result.success)
        assertNotNull("Verification message should not be null", result.message)
        assertTrue("Should have verification details", result.details.isNotEmpty())
        
        // Verify that details contain expected checks
        val detailsText = result.details.joinToString(" ")
        assertTrue("Should verify encrypted database", detailsText.contains("Encrypted database"))
        assertTrue("Should verify CRUD operations", detailsText.contains("CRUD operations"))
        
        // Test health check as well
        val healthCheck = DatabaseVerification.getHealthCheck(appContext)
        assertTrue("Database should be encrypted", healthCheck.isDatabaseEncrypted)
        assertTrue("Database connection should be healthy", healthCheck.isConnectionHealthy)
    }
    
    @Test
    fun testAppLaunchAndNavigation() {
        // Context of the app under test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Verify the app name is correctly set
        val appName = appContext.getString(R.string.app_name)
        assertEquals("AI Soul", appName)
        
        // Verify navigation strings are available
        val chatTitle = appContext.getString(R.string.title_chat)
        val modelsTitle = appContext.getString(R.string.title_models)
        val privacyTitle = appContext.getString(R.string.title_privacy)
        val devPanelTitle = appContext.getString(R.string.title_dev_panel)
        
        assertEquals("Chat", chatTitle)
        assertEquals("Models", modelsTitle)
        assertEquals("Privacy", privacyTitle)
        assertEquals("Dev Panel", devPanelTitle)
    }
}
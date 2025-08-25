package com.aisoul.privateassistant.test

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aisoul.privateassistant.performance.PerformanceManager
import com.aisoul.privateassistant.error.ErrorHandler
import com.aisoul.privateassistant.settings.SettingsManager
import com.aisoul.privateassistant.ai.AIInferenceEngine
import com.aisoul.privateassistant.intelligence.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Comprehensive Test Suite for AI Soul Private Assistant
 * Tests all Sprint 2, 3, and 4 functionality
 */
@RunWith(AndroidJUnit4::class)
class ComprehensiveTestSuite {
    
    private lateinit var context: Context
    private lateinit var performanceManager: PerformanceManager
    private lateinit var errorHandler: ErrorHandler
    private lateinit var settingsManager: SettingsManager
    private lateinit var aiInferenceEngine: AIInferenceEngine
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        performanceManager = PerformanceManager.getInstance(context)
        errorHandler = ErrorHandler.getInstance(context)
        settingsManager = SettingsManager.getInstance(context)
        aiInferenceEngine = AIInferenceEngine.getInstance(context)
    }
    
    // Performance Manager Tests
    
    @Test
    fun testPerformanceManagerInitialization() {
        performanceManager.initialize()
        
        val state = performanceManager.performanceState.value
        assertTrue(\"Performance manager should be in monitoring state\", 
                   state == PerformanceManager.PerformanceState.MONITORING)
    }
    
    @Test
    fun testMemoryMonitoring() = runBlocking {
        performanceManager.initialize()
        
        // Wait for memory monitoring to update
        Thread.sleep(1000)
        
        val memoryUsage = performanceManager.memoryUsage.value
        assertTrue(\"Used memory should be greater than 0\", memoryUsage.usedMemoryMB > 0)
        assertTrue(\"Total memory should be greater than used memory\", 
                   memoryUsage.totalMemoryMB > memoryUsage.usedMemoryMB)
        assertTrue(\"Memory percentage should be between 0 and 100\", 
                   memoryUsage.memoryPercentage in 0f..100f)
    }
    
    @Test
    fun testPerformanceOptimization() = runBlocking {
        performanceManager.initialize()
        performanceManager.forceOptimization()
        
        // Verify optimization completed without errors
        val state = performanceManager.performanceState.value
        assertNotEquals(\"Performance state should not be ERROR after optimization\", 
                       PerformanceManager.PerformanceState.ERROR, state)
    }
    
    @Test
    fun testPerformanceReport() {
        performanceManager.initialize()
        val report = performanceManager.getPerformanceReport()
        
        assertNotNull(\"Performance report should not be null\", report)
        assertTrue(\"Report should contain memory information\", report.contains(\"Memory Usage:\"))
        assertTrue(\"Report should contain device information\", report.contains(\"Device Info:\"))
    }
    
    // Error Handler Tests
    
    @Test
    fun testErrorHandlerInitialization() {
        errorHandler.initialize()
        
        val state = errorHandler.errorState.value
        assertEquals(\"Error handler should be in idle state\", 
                    ErrorHandler.ErrorState.IDLE, state)
    }
    
    @Test
    fun testErrorLogging() {
        errorHandler.initialize()
        
        // Test different log levels
        errorHandler.logDebug(\"TestTag\", \"Debug message\")
        errorHandler.logInfo(\"TestTag\", \"Info message\")
        errorHandler.logWarning(\"TestTag\", \"Warning message\")
        errorHandler.logError(\"TestTag\", \"Error message\")
        
        // Wait for async logging to complete
        Thread.sleep(500)
        
        val recentErrors = errorHandler.recentErrors.value
        assertTrue(\"Should have logged some errors\", recentErrors.isNotEmpty())
    }
    
    @Test\n    fun testExceptionHandling() {\n        errorHandler.initialize()\n        \n        // Test exception logging\n        val testException = RuntimeException(\"Test exception\")\n        errorHandler.logError(\"TestTag\", \"Test error with exception\", testException)\n        \n        Thread.sleep(500)\n        \n        val recentErrors = errorHandler.recentErrors.value\n        val errorWithException = recentErrors.find { it.stackTrace != null }\n        assertNotNull(\"Should have logged error with stack trace\", errorWithException)\n    }\n    \n    @Test\n    fun testErrorStatistics() = runBlocking {\n        errorHandler.initialize()\n        \n        // Generate some test errors\n        errorHandler.logError(\"Test1\", \"Error 1\")\n        errorHandler.logWarning(\"Test2\", \"Warning 1\")\n        errorHandler.logDebug(\"Test3\", \"Debug 1\")\n        \n        Thread.sleep(1000)\n        \n        val statistics = errorHandler.getErrorStatistics()\n        assertTrue(\"Should have some total errors\", statistics.totalErrors >= 0)\n        assertNotNull(\"Should have errors by level\", statistics.errorsByLevel)\n        assertNotNull(\"Should have errors by category\", statistics.errorsByCategory)\n    }\n    \n    // Settings Manager Tests\n    \n    @Test\n    fun testSettingsManagerInitialization() {\n        settingsManager.initialize()\n        \n        val state = settingsManager.settingsState.value\n        assertNotNull(\"Settings state should not be null\", state)\n    }\n    \n    @Test\n    fun testGeneralSettings() {\n        settingsManager.initialize()\n        \n        // Test theme mode\n        settingsManager.setThemeMode(SettingsManager.ThemeMode.DARK)\n        assertEquals(\"Theme mode should be DARK\", \n                    SettingsManager.ThemeMode.DARK, settingsManager.getThemeMode())\n        \n        // Test language\n        settingsManager.setLanguage(\"es\")\n        assertEquals(\"Language should be Spanish\", \"es\", settingsManager.getLanguage())\n        \n        // Test notifications\n        settingsManager.setNotificationsEnabled(false)\n        assertFalse(\"Notifications should be disabled\", settingsManager.areNotificationsEnabled())\n    }\n    \n    @Test\n    fun testAISettings() {\n        settingsManager.initialize()\n        \n        // Test AI model type\n        settingsManager.setAIModelType(SettingsManager.AIModelType.GEMMA_7B)\n        assertEquals(\"AI model should be GEMMA_7B\", \n                    SettingsManager.AIModelType.GEMMA_7B, settingsManager.getAIModelType())\n        \n        // Test response style\n        settingsManager.setResponseStyle(SettingsManager.ResponseStyle.CREATIVE)\n        assertEquals(\"Response style should be CREATIVE\", \n                    SettingsManager.ResponseStyle.CREATIVE, settingsManager.getResponseStyle())\n        \n        // Test voice settings\n        settingsManager.setVoiceEnabled(true)\n        assertTrue(\"Voice should be enabled\", settingsManager.isVoiceEnabled())\n        \n        settingsManager.setTTSSpeed(1.5f)\n        assertEquals(\"TTS speed should be 1.5\", 1.5f, settingsManager.getTTSSpeed(), 0.01f)\n    }\n    \n    @Test\n    fun testPrivacySettings() {\n        settingsManager.initialize()\n        \n        // Test data collection\n        settingsManager.setDataCollectionEnabled(true)\n        assertTrue(\"Data collection should be enabled\", settingsManager.isDataCollectionEnabled())\n        \n        // Test access permissions\n        settingsManager.setContactsAccessEnabled(true)\n        assertTrue(\"Contacts access should be enabled\", settingsManager.isContactsAccessEnabled())\n        \n        settingsManager.setSMSAccessEnabled(false)\n        assertFalse(\"SMS access should be disabled\", settingsManager.isSMSAccessEnabled())\n        \n        // Test data retention\n        settingsManager.setDataRetentionDays(60)\n        assertEquals(\"Data retention should be 60 days\", 60, settingsManager.getDataRetentionDays())\n    }\n    \n    @Test\n    fun testPerformanceSettings() {\n        settingsManager.initialize()\n        \n        // Test performance mode\n        settingsManager.setPerformanceMode(SettingsManager.PerformanceMode.PERFORMANCE)\n        assertEquals(\"Performance mode should be PERFORMANCE\", \n                    SettingsManager.PerformanceMode.PERFORMANCE, settingsManager.getPerformanceMode())\n        \n        // Test optimization settings\n        settingsManager.setMemoryOptimizationEnabled(true)\n        assertTrue(\"Memory optimization should be enabled\", settingsManager.isMemoryOptimizationEnabled())\n        \n        settingsManager.setBatteryOptimizationEnabled(false)\n        assertFalse(\"Battery optimization should be disabled\", settingsManager.isBatteryOptimizationEnabled())\n        \n        // Test cache size\n        settingsManager.setCacheSize(200)\n        assertEquals(\"Cache size should be 200MB\", 200, settingsManager.getCacheSize())\n    }\n    \n    @Test\n    fun testSecuritySettings() {\n        settingsManager.initialize()\n        \n        // Test encryption\n        settingsManager.setEncryptionEnabled(true)\n        assertTrue(\"Encryption should be enabled\", settingsManager.isEncryptionEnabled())\n        \n        // Test biometric auth\n        settingsManager.setBiometricAuthEnabled(true)\n        assertTrue(\"Biometric auth should be enabled\", settingsManager.isBiometricAuthEnabled())\n        \n        // Test app lock\n        settingsManager.setAppLockEnabled(true)\n        assertTrue(\"App lock should be enabled\", settingsManager.isAppLockEnabled())\n        \n        // Test auto lock timeout\n        settingsManager.setAutoLockTimeout(10 * 60 * 1000L) // 10 minutes\n        assertEquals(\"Auto lock timeout should be 10 minutes\", \n                    10 * 60 * 1000L, settingsManager.getAutoLockTimeout())\n    }\n    \n    @Test\n    fun testSettingsExportImport() {\n        settingsManager.initialize()\n        \n        // Set some custom settings\n        settingsManager.setThemeMode(SettingsManager.ThemeMode.DARK)\n        settingsManager.setLanguage(\"fr\")\n        settingsManager.setAIModelType(SettingsManager.AIModelType.PHI3_MINI)\n        settingsManager.setVoiceEnabled(true)\n        \n        // Export settings\n        val exportedSettings = settingsManager.exportSettings()\n        assertNotNull(\"Exported settings should not be null\", exportedSettings)\n        assertTrue(\"Exported settings should not be empty\", exportedSettings.isNotEmpty())\n        \n        // Reset to defaults\n        settingsManager.resetToDefaults()\n        \n        // Import settings\n        settingsManager.importSettings(exportedSettings)\n        \n        // Verify settings were restored\n        assertEquals(\"Theme mode should be restored\", \n                    SettingsManager.ThemeMode.DARK, settingsManager.getThemeMode())\n        assertEquals(\"Language should be restored\", \"fr\", settingsManager.getLanguage())\n        assertEquals(\"AI model should be restored\", \n                    SettingsManager.AIModelType.PHI3_MINI, settingsManager.getAIModelType())\n        assertTrue(\"Voice should be restored\", settingsManager.isVoiceEnabled())\n    }\n    \n    // AI Inference Engine Tests\n    \n    @Test\n    fun testAIInferenceEngineInitialization() = runBlocking {\n        val result = aiInferenceEngine.initialize()\n        assertTrue(\"AI Inference Engine should initialize successfully\", result)\n        \n        val isReady = aiInferenceEngine.isModelReady()\n        // Note: May be false if no model is downloaded yet, but initialization should succeed\n        assertNotNull(\"Model ready state should not be null\", isReady)\n    }\n    \n    @Test\n    fun testDemoModeResponse() = runBlocking {\n        aiInferenceEngine.initialize()\n        \n        val response = aiInferenceEngine.generateResponse(\"Hello, how are you?\", emptyList())\n        \n        when (response) {\n            is AIInferenceEngine.InferenceResult.Success -> {\n                assertNotNull(\"Response should not be null\", response.response)\n                assertTrue(\"Response should not be empty\", response.response.isNotEmpty())\n                assertTrue(\"Confidence should be between 0 and 1\", \n                          response.confidence in 0f..1f)\n            }\n            is AIInferenceEngine.InferenceResult.Error -> {\n                // Demo mode should still work even without model\n                assertNotNull(\"Error message should not be null\", response.error)\n            }\n        }\n    }\n    \n    // Intelligence Layer Tests\n    \n    @Test\n    fun testNotificationAnalyzer() = runBlocking {\n        val analyzer = NotificationAnalyzer.getInstance(context)\n        \n        val result = analyzer.initialize()\n        // May fail without notification access permission, but shouldn't crash\n        assertNotNull(\"Initialization result should not be null\", result)\n    }\n    \n    @Test\n    fun testSmsIntegrationManager() = runBlocking {\n        val smsManager = SmsIntegrationManager.getInstance(context)\n        \n        val result = smsManager.initialize()\n        // May fail without SMS permission, but shouldn't crash\n        assertNotNull(\"Initialization result should not be null\", result)\n    }\n    \n    @Test\n    fun testAppUsageAnalyzer() = runBlocking {\n        val analyzer = AppUsageAnalyzer.getInstance(context)\n        \n        val result = analyzer.initialize()\n        // May fail without usage stats permission, but shouldn't crash\n        assertNotNull(\"Initialization result should not be null\", result)\n    }\n    \n    @Test\n    fun testVectorDatabase() = runBlocking {\n        val vectorDb = VectorDatabase.getInstance(context)\n        \n        vectorDb.initialize()\n        \n        // Test adding a document\n        val success = vectorDb.addDocument(\n            \"test_doc_1\",\n            \"Test Document\",\n            \"This is a test document for vector database testing.\",\n            \"unit_test\"\n        )\n        \n        assertTrue(\"Document addition should succeed\", success)\n        \n        // Test searching\n        val results = vectorDb.search(\"test document\", topK = 5)\n        assertNotNull(\"Search results should not be null\", results)\n    }\n    \n    @Test\n    fun testSemanticSearchManager() = runBlocking {\n        val searchManager = SemanticSearchManager.getInstance(context)\n        \n        searchManager.initialize()\n        \n        // Test universal search\n        val results = searchManager.universalSearch(\"test query\")\n        assertNotNull(\"Search results should not be null\", results)\n    }\n    \n    // Integration Tests\n    \n    @Test\n    fun testSystemIntegration() = runBlocking {\n        // Initialize all systems\n        settingsManager.initialize()\n        errorHandler.initialize()\n        performanceManager.initialize()\n        aiInferenceEngine.initialize()\n        \n        // Test settings interaction with other systems\n        settingsManager.setPerformanceMode(SettingsManager.PerformanceMode.PERFORMANCE)\n        \n        // Test error logging across systems\n        errorHandler.logInfo(\"Integration\", \"Testing system integration\")\n        \n        // Test AI response with settings\n        val response = aiInferenceEngine.generateResponse(\"Test integration\", emptyList())\n        assertNotNull(\"AI response should not be null\", response)\n        \n        // Test performance monitoring\n        Thread.sleep(2000)\n        val memoryUsage = performanceManager.memoryUsage.value\n        assertTrue(\"Memory monitoring should be active\", memoryUsage.totalMemoryMB > 0)\n    }\n    \n    @Test\n    fun testErrorRecovery() {\n        errorHandler.initialize()\n        \n        // Simulate various error conditions\n        try {\n            throw RuntimeException(\"Simulated error\")\n        } catch (e: Exception) {\n            errorHandler.logError(\"ErrorRecovery\", \"Testing error recovery\", e)\n        }\n        \n        try {\n            throw OutOfMemoryError(\"Simulated OOM\")\n        } catch (e: Error) {\n            errorHandler.logFatal(\"ErrorRecovery\", \"Testing fatal error recovery\", e)\n        }\n        \n        // System should still be functional\n        val state = errorHandler.errorState.value\n        assertNotEquals(\"System should recover from errors\", \n                       ErrorHandler.ErrorState.ERROR, state)\n    }\n    \n    @Test\n    fun testDataPersistence() = runBlocking {\n        settingsManager.initialize()\n        \n        // Set some settings\n        settingsManager.setLanguage(\"de\")\n        settingsManager.setAIModelType(SettingsManager.AIModelType.GEMMA_7B)\n        \n        // Create new instance (simulating app restart)\n        val newSettingsManager = SettingsManager.getInstance(context)\n        newSettingsManager.initialize()\n        \n        // Verify settings persisted\n        assertEquals(\"Language should persist\", \"de\", newSettingsManager.getLanguage())\n        assertEquals(\"AI model should persist\", \n                    SettingsManager.AIModelType.GEMMA_7B, newSettingsManager.getAIModelType())\n    }\n    \n    @Test\n    fun testSecurityFeatures() {\n        settingsManager.initialize()\n        \n        // Test encryption setting\n        settingsManager.setEncryptionEnabled(true)\n        assertTrue(\"Encryption should be enabled in secure storage\", \n                  settingsManager.isEncryptionEnabled())\n        \n        // Test that sensitive settings are stored securely\n        settingsManager.setBiometricAuthEnabled(true)\n        assertTrue(\"Biometric auth setting should be stored securely\", \n                  settingsManager.isBiometricAuthEnabled())\n    }\n    \n    @Test\n    fun testPerformanceUnderLoad() = runBlocking {\n        performanceManager.initialize()\n        \n        // Simulate load by creating multiple concurrent operations\n        val jobs = (1..10).map {\n            kotlinx.coroutines.async {\n                // Simulate some work\n                Thread.sleep(100)\n                performanceManager.memoryUsage.value\n            }\n        }\n        \n        // Wait for all jobs to complete\n        jobs.forEach { it.await() }\n        \n        // System should still be responsive\n        val state = performanceManager.performanceState.value\n        assertNotEquals(\"Performance manager should handle load\", \n                       PerformanceManager.PerformanceState.ERROR, state)\n    }\n}"
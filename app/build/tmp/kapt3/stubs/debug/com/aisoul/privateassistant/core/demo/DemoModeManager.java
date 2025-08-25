package com.aisoul.privateassistant.core.demo;

/**
 * Manages demo mode functionality for testing and development.
 * Provides simulated AI responses when real models are not available.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010$\n\u0000\n\u0002\u0010 \n\u0002\b\u0007\u0018\u0000 \u001f2\u00020\u0001:\u0001\u001fB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0014J\b\u0010\u0015\u001a\u00020\u0012H\u0002J\u0012\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\u0017J\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00120\u0019J\u0006\u0010\u001a\u001a\u00020\u0006J\u0016\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u001c\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u001d\u001a\u00020\u00122\u0006\u0010\u001e\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R$\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001b\u0010\u000b\u001a\u00020\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\u0010\u001a\u0004\b\r\u0010\u000e\u00a8\u0006 "}, d2 = {"Lcom/aisoul/privateassistant/core/demo/DemoModeManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "value", "", "isDemoModeEnabled", "()Z", "setDemoModeEnabled", "(Z)V", "prefs", "Landroid/content/SharedPreferences;", "getPrefs", "()Landroid/content/SharedPreferences;", "prefs$delegate", "Lkotlin/Lazy;", "generateDemoResponse", "", "userInput", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDemoContext", "getDemoStats", "", "getDemoSuggestions", "", "hasRealModelsAvailable", "simulateNotificationAnalysis", "notificationContent", "simulateSMSAnalysis", "smsContent", "Companion", "app_debug"})
public final class DemoModeManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy prefs$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_DEMO_MODE_ENABLED = "demo_mode_enabled";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.aisoul.privateassistant.core.demo.DemoModeManager INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.aisoul.privateassistant.core.demo.DemoModeManager.Companion Companion = null;
    
    private DemoModeManager(android.content.Context context) {
        super();
    }
    
    private final android.content.SharedPreferences getPrefs() {
        return null;
    }
    
    public final boolean isDemoModeEnabled() {
        return false;
    }
    
    public final void setDemoModeEnabled(boolean value) {
    }
    
    /**
     * Generate a demo response for the given user input
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateDemoResponse(@org.jetbrains.annotations.NotNull()
    java.lang.String userInput, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Get demo context information
     */
    private final java.lang.String getDemoContext() {
        return null;
    }
    
    /**
     * Simulate notification analysis
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object simulateNotificationAnalysis(@org.jetbrains.annotations.NotNull()
    java.lang.String notificationContent, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Simulate SMS analysis
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object simulateSMSAnalysis(@org.jetbrains.annotations.NotNull()
    java.lang.String smsContent, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Get demo statistics
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Object> getDemoStats() {
        return null;
    }
    
    /**
     * Check if real AI models are available
     */
    public final boolean hasRealModelsAvailable() {
        return false;
    }
    
    /**
     * Get suggested demo interactions
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getDemoSuggestions() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\tR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/aisoul/privateassistant/core/demo/DemoModeManager$Companion;", "", "()V", "INSTANCE", "Lcom/aisoul/privateassistant/core/demo/DemoModeManager;", "KEY_DEMO_MODE_ENABLED", "", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Get singleton instance of DemoModeManager
         */
        @org.jetbrains.annotations.NotNull()
        public final com.aisoul.privateassistant.core.demo.DemoModeManager getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}
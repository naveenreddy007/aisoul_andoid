package com.aisoul.privateassistant.services;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\u0018\u0000 \u00162\u00020\u0001:\u0001\u0016B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0007\u001a\u00020\bH\u0016J\b\u0010\t\u001a\u00020\bH\u0016J\b\u0010\n\u001a\u00020\bH\u0016J\u0012\u0010\u000b\u001a\u00020\b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0016J\u0012\u0010\u000e\u001a\u00020\b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0016J\u0010\u0010\u000f\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rH\u0002J \u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u0013H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/aisoul/privateassistant/services/NotificationListenerService;", "Landroid/service/notification/NotificationListenerService;", "()V", "serviceJob", "Lkotlinx/coroutines/CompletableJob;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "onDestroy", "", "onListenerConnected", "onListenerDisconnected", "onNotificationPosted", "sbn", "Landroid/service/notification/StatusBarNotification;", "onNotificationRemoved", "processNotification", "shouldProcessNotification", "", "packageName", "", "title", "text", "Companion", "app_debug"})
public final class NotificationListenerService extends android.service.notification.NotificationListenerService {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "AISoulNotificationListener";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CompletableJob serviceJob = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.aisoul.privateassistant.services.NotificationListenerService.Companion Companion = null;
    
    public NotificationListenerService() {
        super();
    }
    
    @java.lang.Override()
    public void onNotificationPosted(@org.jetbrains.annotations.Nullable()
    android.service.notification.StatusBarNotification sbn) {
    }
    
    @java.lang.Override()
    public void onNotificationRemoved(@org.jetbrains.annotations.Nullable()
    android.service.notification.StatusBarNotification sbn) {
    }
    
    @java.lang.Override()
    public void onListenerConnected() {
    }
    
    @java.lang.Override()
    public void onListenerDisconnected() {
    }
    
    private final void processNotification(android.service.notification.StatusBarNotification sbn) {
    }
    
    private final boolean shouldProcessNotification(java.lang.String packageName, java.lang.String title, java.lang.String text) {
        return false;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/aisoul/privateassistant/services/NotificationListenerService$Companion;", "", "()V", "TAG", "", "isNotificationAccessGranted", "", "context", "Landroid/content/Context;", "requestNotificationAccess", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean isNotificationAccessGranted(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return false;
        }
        
        public final void requestNotificationAccess(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
    }
}
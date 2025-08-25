package com.aisoul.privateassistant.services

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob

class NotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "AISoulNotificationListener"
        
        fun isNotificationAccessGranted(context: android.content.Context): Boolean {
            val enabledListeners = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            val packageName = context.packageName
            return enabledListeners?.contains(packageName) == true
        }
        
        fun requestNotificationAccess(context: android.content.Context) {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let { notification ->
            serviceScope.launch {
                processNotification(notification)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "Notification removed from ${sbn?.packageName}")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        
        // Optionally process existing notifications
        serviceScope.launch {
            try {
                val activeNotifications = activeNotifications
                Log.d(TAG, "Found ${activeNotifications.size} active notifications")
            } catch (e: Exception) {
                Log.e(TAG, "Error accessing active notifications", e)
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
    }

    private fun processNotification(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification
            
            // Extract notification content
            val title = notification.extras?.getString("android.title") ?: ""
            val text = notification.extras?.getString("android.text") ?: ""
            val bigText = notification.extras?.getString("android.bigText") ?: text
            
            Log.d(TAG, "Processing notification from $packageName: $title")
            
            // Filter out system notifications and our own app
            if (shouldProcessNotification(packageName, title, text)) {
                // TODO: In full implementation, this would:
                // 1. Save notification to encrypted database
                // 2. Analyze with AI for relevance
                // 3. Generate smart responses if needed
                // 4. Show AI suggestions to user
                
                Log.d(TAG, "Demo Mode: Processed notification - Title: $title, Text: $bigText")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    private fun shouldProcessNotification(packageName: String, title: String, text: String): Boolean {
        // Don't process our own notifications
        if (packageName == "com.aisoul.privateassistant") {
            return false
        }
        
        // Skip system notifications
        val systemPackages = setOf(
            "android",
            "com.android.systemui",
            "com.android.settings"
        )
        
        if (systemPackages.contains(packageName)) {
            return false
        }
        
        // Skip empty notifications
        if (title.isBlank() && text.isBlank()) {
            return false
        }
        
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }


}
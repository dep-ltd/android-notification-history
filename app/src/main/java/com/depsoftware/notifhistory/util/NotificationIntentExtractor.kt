package com.depsoftware.notifhistory.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent

object NotificationIntentExtractor {

    fun extractContentIntentUri(notification: Notification): String? {
        return serializeIntent(notification.contentIntent?.let { extractIntent(it) })
    }

    private fun extractIntent(pendingIntent: PendingIntent): Intent? {
        return try {
            val method = PendingIntent::class.java.getMethod("getIntent")
            method.invoke(pendingIntent) as? Intent
        } catch (_: Exception) {
            null
        }
    }

    private fun serializeIntent(intent: Intent?): String? {
        if (intent == null) return null
        return try {
            intent.toUri(Intent.URI_INTENT_SCHEME)
        } catch (_: Exception) {
            null
        }
    }
}

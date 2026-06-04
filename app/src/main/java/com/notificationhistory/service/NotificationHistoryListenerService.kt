package com.notificationhistory.service

import android.content.Context
import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notificationhistory.data.preferences.SettingsManager
import com.notificationhistory.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationHistoryListenerService : NotificationListenerService() {

    @Inject
    lateinit var repository: NotificationRepository

    @Inject
    lateinit var parser: NotificationParser

    @Inject
    lateinit var settingsManager: SettingsManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        syncActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { handleNotification(it) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            serviceScope.launch {
                repository.markAsRemoved(
                    NotificationParser.stableKey(notification.packageName, notification.key)
                )
            }
        }
    }

    private fun syncActiveNotifications() {
        val active = try {
            activeNotifications
        } catch (_: SecurityException) {
            null
        } ?: return
        active.forEach { handleNotification(it) }
    }

    private fun handleNotification(sbn: StatusBarNotification) {
        if (settingsManager.isBlacklisted(sbn.packageName)) return

        val event = parser.parse(
            packageName = sbn.packageName,
            notificationKey = sbn.key,
            postedAt = sbn.postTime,
            extras = sbn.notification.extras,
            appLabel = resolveAppLabel(sbn.packageName),
            channelId = sbn.notification.channelId,
            groupKey = sbn.groupKey,
            isGroupSummary = sbn.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY != 0,
            smallIconBitmap = extractSmallIconBitmap(sbn)
        )

        serviceScope.launch {
            repository.upsertNotification(event)
        }
    }

    private fun resolveAppLabel(packageName: String): String? {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun extractSmallIconBitmap(sbn: StatusBarNotification): Bitmap? {
        val icon = sbn.notification.smallIcon ?: return null
        val drawable = icon.loadDrawable(this) ?: return null
        return NotificationParser.bitmapFromDrawable(drawable)
    }
}

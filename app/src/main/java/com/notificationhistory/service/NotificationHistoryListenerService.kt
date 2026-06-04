package com.notificationhistory.service

import android.app.Notification
import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notificationhistory.data.entities.NotificationEvent
import com.notificationhistory.data.preferences.SettingsManager
import com.notificationhistory.data.repository.NotificationRepository
import com.notificationhistory.util.MediaStorage
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
    lateinit var mediaStorage: MediaStorage

    @Inject
    lateinit var settingsManager: SettingsManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            if (settingsManager.isBlacklisted(notification.packageName)) return

            val extras = notification.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE)
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            
            // Extract Large Icon if available
            val largeIcon = extras.getParcelable<Bitmap>(Notification.EXTRA_LARGE_ICON)
            val mediaPath = largeIcon?.let { mediaStorage.saveBitmap(it) }

            val event = NotificationEvent(
                stableKey = notification.key,
                packageName = notification.packageName,
                appLabel = getAppLabel(notification.packageName),
                postedAt = notification.postTime,
                title = title,
                text = text,
                channelId = notification.notification.channelId,
                groupKey = notification.groupKey,
                isGroupSummary = (notification.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0,
                mediaPath = mediaPath
            )

            serviceScope.launch {
                repository.upsertNotification(event)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            serviceScope.launch {
                repository.markAsRemoved(notification.key)
            }
        }
    }

    private fun getAppLabel(packageName: String): String? {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            null
        }
    }
}

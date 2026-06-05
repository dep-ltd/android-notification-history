package com.depsoftware.notifhistory.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.depsoftware.notifhistory.data.preferences.SettingsManager
import com.depsoftware.notifhistory.data.repository.NotificationRepository
import com.depsoftware.notifhistory.security.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationHistoryListenerService : NotificationListenerService() {

    @Inject lateinit var repository: NotificationRepository
    @Inject lateinit var parser: NotificationParser
    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var authManager: AuthManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        syncActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { handleNotification(it) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Dismissals from the status bar are not tracked in history.
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
        if (!authManager.isPinSet()) return
        if (settingsManager.isBlacklisted(sbn.packageName)) return
        // Group summary is a synthetic aggregation entry — skip it; we store individual pushes only
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val event = parser.parse(
            sbn = sbn,
            appLabel = resolveAppLabel(sbn.packageName)
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
}

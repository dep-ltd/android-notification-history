package com.notificationhistory.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.notificationhistory.service.NotificationHistoryListenerService

object NotificationAccess {
    fun isListenerEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        if (TextUtils.isEmpty(enabled)) return false

        val target = ComponentName(context, NotificationHistoryListenerService::class.java)
        return enabled.split(":")
            .mapNotNull { ComponentName.unflattenFromString(it) }
            .any { it == target }
    }
}

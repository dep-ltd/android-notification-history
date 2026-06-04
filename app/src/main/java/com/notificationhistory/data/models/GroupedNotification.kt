package com.notificationhistory.data.models

import com.notificationhistory.data.entities.NotificationEvent

data class GroupedNotification(
    val summary: NotificationEvent,
    val children: List<NotificationEvent> = emptyList(),
    val isExpanded: Boolean = false
)

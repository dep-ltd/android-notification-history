package com.notificationhistory.data.entities

fun NotificationEvent.hasDisplayableContent(): Boolean {
    return !title.isNullOrBlank() || !text.isNullOrBlank()
}

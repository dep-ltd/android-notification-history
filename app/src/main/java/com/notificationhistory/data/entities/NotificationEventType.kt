package com.notificationhistory.data.entities

enum class NotificationEventType {
    POSTED,
    UPDATED,
    REMOVED;

    companion object {
        fun fromStored(value: String?): NotificationEventType =
            entries.find { it.name == value } ?: POSTED
    }
}

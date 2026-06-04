package com.notificationhistory.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_events")
data class NotificationEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stableKey: String,
    val packageName: String,
    val appLabel: String?,
    val postedAt: Long,
    val updatedAt: Long? = null,
    val removedAt: Long? = null,
    val title: String?,
    val text: String?,
    val channelId: String?,
    val groupKey: String?,
    val isGroupSummary: Boolean = false,
    val mediaPath: String? = null
)

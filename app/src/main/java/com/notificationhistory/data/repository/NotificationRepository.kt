package com.notificationhistory.data.repository

import com.notificationhistory.data.dao.NotificationDao
import com.notificationhistory.data.entities.NotificationEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    val allNotifications: Flow<List<NotificationEvent>> = notificationDao.getAllNotifications()

    suspend fun upsertNotification(event: NotificationEvent) {
        val existing = notificationDao.getByStableKey(event.stableKey)
        if (existing != null) {
            notificationDao.update(event.copy(id = existing.id, updatedAt = System.currentTimeMillis()))
        } else {
            notificationDao.insert(event)
        }
    }

    suspend fun markAsRemoved(stableKey: String) {
        val existing = notificationDao.getByStableKey(stableKey)
        if (existing != null) {
            notificationDao.update(existing.copy(removedAt = System.currentTimeMillis()))
        }
    }
}

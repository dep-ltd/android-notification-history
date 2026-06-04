package com.notificationhistory.data.repository

import com.notificationhistory.data.dao.NotificationDao
import com.notificationhistory.data.entities.NotificationEvent
import com.notificationhistory.data.entities.NotificationEventType
import com.notificationhistory.data.preferences.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val settingsManager: SettingsManager
) {
    val allNotifications: Flow<List<NotificationEvent>> = settingsManager.blacklistFlow
        .flatMapLatest { blacklist ->
            notificationDao.getAllNotifications().map { events ->
                events.filter { it.packageName !in blacklist }
            }
        }

    fun observeNotification(id: Long): Flow<NotificationEvent?> = notificationDao.observeById(id)

    suspend fun upsertNotification(incoming: NotificationEvent) {
        val existing = notificationDao.getByStableKey(incoming.stableKey)
        val now = System.currentTimeMillis()

        val toSave = if (existing != null) {
            incoming.copy(
                id = existing.id,
                postedAt = existing.postedAt,
                updatedAt = now,
                removedAt = null,
                eventType = NotificationEventType.UPDATED.name,
                summaryId = existing.summaryId ?: incoming.summaryId,
                mediaPaths = mergeMediaPaths(existing.mediaPaths, incoming.mediaPaths)
            )
        } else {
            incoming.copy(eventType = NotificationEventType.POSTED.name)
        }

        val rowId = if (existing != null) {
            notificationDao.update(toSave)
            existing.id
        } else {
            notificationDao.insert(toSave)
        }

        if (toSave.groupKey != null && !toSave.isGroupSummary) {
            linkChildToSummary(rowId, toSave.packageName, toSave.groupKey)
        }
    }

    suspend fun markAsRemoved(stableKey: String) {
        val existing = notificationDao.getByStableKey(stableKey) ?: return
        notificationDao.update(
            existing.copy(
                removedAt = System.currentTimeMillis(),
                eventType = NotificationEventType.REMOVED.name
            )
        )
    }

    private suspend fun linkChildToSummary(childId: Long, packageName: String, groupKey: String) {
        val summary = notificationDao.getSummaryByGroup(packageName, groupKey) ?: return
        val child = notificationDao.getById(childId) ?: return
        if (child.summaryId == summary.id) return
        notificationDao.update(child.copy(summaryId = summary.id))
    }

    private fun mergeMediaPaths(existing: List<String>, incoming: List<String>): List<String> {
        return (existing + incoming).distinct()
    }
}

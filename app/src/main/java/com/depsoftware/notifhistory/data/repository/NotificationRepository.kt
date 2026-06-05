package com.depsoftware.notifhistory.data.repository

import com.depsoftware.notifhistory.data.DatabaseHolder
import com.depsoftware.notifhistory.data.dao.NotificationDao
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import com.depsoftware.notifhistory.data.entities.NotificationEventType
import com.depsoftware.notifhistory.data.preferences.SettingsManager
import com.depsoftware.notifhistory.security.AuthManager
import com.depsoftware.notifhistory.util.MediaStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class NotificationRepository @Inject constructor(
    private val databaseHolder: DatabaseHolder,
    private val settingsManager: SettingsManager,
    private val mediaStorage: MediaStorage,
    private val authManager: AuthManager
) {
    private fun isDatabaseReady(): Boolean = authManager.isPinSet()

    private fun dao(): NotificationDao = databaseHolder.get().notificationDao()

    val allNotifications: Flow<List<NotificationEvent>> = settingsManager.blacklistFlow
        .flatMapLatest { blacklist ->
            if (!isDatabaseReady()) {
                flowOf(emptyList())
            } else {
                dao().getAllNotifications().map { events ->
                    events.filter { it.packageName !in blacklist }
                }
            }
        }

    fun observeNotification(id: Long): Flow<NotificationEvent?> {
        if (!isDatabaseReady()) return flowOf(null)
        return dao().observeById(id)
    }

    fun observeDistinctPackages(): Flow<List<String>> {
        if (!isDatabaseReady()) return flowOf(emptyList())
        return dao().observeDistinctPackages()
    }

    suspend fun pruneExpiredNotifications() {
        if (!isDatabaseReady()) return
        val notificationDao = dao()
        val retentionDays = settingsManager.getRetentionDays()
        val cutoff = System.currentTimeMillis() - retentionDays * 24L * 60L * 60L * 1000L
        val expired = notificationDao.getOlderThan(cutoff)
        expired.forEach { event ->
            event.mediaPaths.forEach { path -> mediaStorage.deleteFile(path) }
        }
        notificationDao.deleteOlderThan(cutoff)
    }

    suspend fun clearAllHistory() {
        if (!isDatabaseReady()) return
        val notificationDao = dao()
        val all = notificationDao.getOlderThan(Long.MAX_VALUE)
        all.forEach { event ->
            event.mediaPaths.forEach { path -> mediaStorage.deleteFile(path) }
        }
        notificationDao.deleteAll()
    }

    suspend fun upsertNotification(incoming: NotificationEvent) {
        if (!isDatabaseReady()) return
        val notificationDao = dao()
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
                mediaPaths = mergeMediaPaths(existing.mediaPaths, incoming.mediaPaths),
                contentIntentUri = incoming.contentIntentUri ?: existing.contentIntentUri
            )
        } else {
            incoming.copy(eventType = NotificationEventType.POSTED.name)
        }

        if (existing != null) {
            notificationDao.update(toSave)
        } else {
            notificationDao.insert(toSave)
        }
    }

    suspend fun markAsRemoved(stableKey: String) {
        if (!isDatabaseReady()) return
        val notificationDao = dao()
        val existing = notificationDao.getByStableKey(stableKey) ?: return
        notificationDao.update(
            existing.copy(
                removedAt = System.currentTimeMillis(),
                eventType = NotificationEventType.REMOVED.name
            )
        )
    }

    private fun mergeMediaPaths(existing: List<String>, incoming: List<String>): List<String> {
        return (existing + incoming).distinct()
    }
}

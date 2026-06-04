package com.depsoftware.notifhistory.data.dao

import androidx.room.*
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_events ORDER BY postedAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: NotificationEvent): Long

    @Query("SELECT * FROM notification_events WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<NotificationEvent?>

    @Query("SELECT * FROM notification_events WHERE stableKey = :key LIMIT 1")
    suspend fun getByStableKey(key: String): NotificationEvent?

    @Query("SELECT * FROM notification_events WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NotificationEvent?

    @Query(
        """
        SELECT * FROM notification_events
        WHERE packageName = :packageName AND groupKey = :groupKey AND isGroupSummary = 1
        LIMIT 1
        """
    )
    suspend fun getSummaryByGroup(packageName: String, groupKey: String): NotificationEvent?

    @Update
    suspend fun update(event: NotificationEvent)

    @Query("DELETE FROM notification_events")
    suspend fun deleteAll()

    @Query("SELECT * FROM notification_events WHERE postedAt < :cutoffMs")
    suspend fun getOlderThan(cutoffMs: Long): List<NotificationEvent>

    @Query("DELETE FROM notification_events WHERE postedAt < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)

    @Query("SELECT DISTINCT packageName FROM notification_events ORDER BY packageName ASC")
    fun observeDistinctPackages(): Flow<List<String>>
}

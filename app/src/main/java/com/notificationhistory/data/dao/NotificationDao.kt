package com.notificationhistory.data.dao

import androidx.room.*
import com.notificationhistory.data.entities.NotificationEvent
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

    @Update
    suspend fun update(event: NotificationEvent)

    @Query("DELETE FROM notification_events")
    suspend fun deleteAll()
}

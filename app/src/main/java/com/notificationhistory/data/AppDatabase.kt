package com.notificationhistory.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notificationhistory.data.dao.NotificationDao
import com.notificationhistory.data.entities.NotificationEvent

@Database(entities = [NotificationEvent::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}

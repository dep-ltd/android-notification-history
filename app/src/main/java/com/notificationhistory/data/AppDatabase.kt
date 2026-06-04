package com.notificationhistory.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.notificationhistory.data.converters.StringListConverter
import com.notificationhistory.data.dao.NotificationDao
import com.notificationhistory.data.entities.NotificationEvent

@Database(
    entities = [NotificationEvent::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}

package com.depsoftware.notifhistory.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.depsoftware.notifhistory.data.converters.StringListConverter
import com.depsoftware.notifhistory.data.dao.NotificationDao
import com.depsoftware.notifhistory.data.entities.NotificationEvent

@Database(
    entities = [NotificationEvent::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}

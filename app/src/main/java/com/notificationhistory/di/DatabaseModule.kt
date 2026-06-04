package com.notificationhistory.di

import android.content.Context
import androidx.room.Room
import com.notificationhistory.data.AppDatabase
import com.notificationhistory.data.dao.NotificationDao
import com.notificationhistory.security.SecurityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        securityManager: SecurityManager
    ): AppDatabase {
        val passphrase = securityManager.getDatabasePassphrase().toByteArray()
        val factory = SupportOpenHelperFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notification_history.db"
        )
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }
}

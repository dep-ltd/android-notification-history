package com.notificationhistory.di

import android.content.Context
import com.notificationhistory.util.MediaStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun provideMediaStorage(@ApplicationContext context: Context): MediaStorage {
        return MediaStorage(context)
    }
}

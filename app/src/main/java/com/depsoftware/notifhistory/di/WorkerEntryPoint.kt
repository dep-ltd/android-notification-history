package com.depsoftware.notifhistory.di

import com.depsoftware.notifhistory.worker.RetentionScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun retentionScheduler(): RetentionScheduler
}

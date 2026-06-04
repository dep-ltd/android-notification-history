package com.depsoftware.notifhistory.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.depsoftware.notifhistory.data.repository.NotificationRepository
import com.depsoftware.notifhistory.security.AuthManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RetentionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: NotificationRepository,
    private val authManager: AuthManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!authManager.isPinSet()) {
            return Result.success()
        }
        return try {
            repository.pruneExpiredNotifications()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "notification_retention"
    }
}

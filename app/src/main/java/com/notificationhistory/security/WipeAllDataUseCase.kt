package com.notificationhistory.security

import android.content.Context
import com.notificationhistory.data.DatabaseHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WipeAllDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseHolder: DatabaseHolder,
    private val securityManager: SecurityManager
) {
    operator fun invoke() {
        databaseHolder.closeAndWipe()

        val mediaDir = File(context.filesDir, "media")
        if (mediaDir.exists()) {
            mediaDir.deleteRecursively()
        }

        File(context.filesDir, "datastore").takeIf { it.exists() }?.deleteRecursively()

        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        context.deleteSharedPreferences(SecurityManager.SECURE_PREFS_NAME)
        context.deleteSharedPreferences(AuthManager.AUTH_SECURE_PREFS_NAME)

        securityManager.clearSecureStorage()
    }
}

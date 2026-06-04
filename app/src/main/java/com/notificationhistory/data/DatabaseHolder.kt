package com.notificationhistory.data

import android.content.Context
import androidx.room.Room
import com.notificationhistory.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseHolder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityManager: SecurityManager
) {
    @Volatile
    private var database: AppDatabase? = null

    @Synchronized
    fun get(): AppDatabase {
        val existing = database
        if (existing != null && existing.isOpen) return existing
        return openDatabase().also { database = it }
    }

    @Synchronized
    fun recreateEncrypted() {
        close()
        context.deleteDatabase(DATABASE_NAME)
        securityManager.ensureDatabasePassphrase()
        database = openDatabase()
    }

    @Synchronized
    fun closeAndWipe() {
        close()
        context.deleteDatabase(DATABASE_NAME)
        database = null
    }

    @Synchronized
    private fun close() {
        database?.close()
        database = null
    }

    private fun openDatabase(): AppDatabase {
        SecurityManager.loadSqlCipher()
        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration()

        if (securityManager.hasDatabasePassphrase()) {
            builder.openHelperFactory(SupportFactory(securityManager.getDatabasePassphraseBytes()))
        }

        return builder.build()
    }

    companion object {
        const val DATABASE_NAME = "notification_history.db"
    }
}

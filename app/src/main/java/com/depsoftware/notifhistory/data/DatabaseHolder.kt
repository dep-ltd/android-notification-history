package com.depsoftware.notifhistory.data

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.Room
import com.depsoftware.notifhistory.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File
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
        wipeDatabaseFiles()
        securityManager.ensureDatabasePassphrase()
        database = openDatabase()
    }

    @Synchronized
    fun closeAndWipe() {
        close()
        wipeDatabaseFiles()
        database = null
    }

    @Synchronized
    private fun close() {
        database?.close()
        database = null
    }

    private fun openDatabase(): AppDatabase {
        if (!securityManager.hasDatabasePassphrase()) {
            throw IllegalStateException("Database is not available until a PIN is configured")
        }
        return try {
            buildDatabase()
        } catch (e: SQLiteException) {
            if (!isNotADatabaseError(e)) throw e
            wipeDatabaseFiles()
            buildDatabase()
        }
    }

    private fun buildDatabase(): AppDatabase {
        System.loadLibrary("sqlcipher")
        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration()

        builder.openHelperFactory(
            SupportOpenHelperFactory(securityManager.getDatabasePassphraseBytes())
        )

        val db = builder.build()
        // Force open so corrupt/plain files are detected before Room hands out DAOs.
        db.openHelper.writableDatabase
        return db
    }

    private fun wipeDatabaseFiles() {
        context.deleteDatabase(DATABASE_NAME)
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        val parent = dbFile.parentFile ?: return
        listOf(
            DATABASE_NAME,
            "$DATABASE_NAME-journal",
            "$DATABASE_NAME-wal",
            "$DATABASE_NAME-shm"
        ).forEach { name ->
            File(parent, name).delete()
        }
    }

    private fun isNotADatabaseError(e: SQLiteException): Boolean {
        return e.message?.contains("file is not a database", ignoreCase = true) == true
    }

    companion object {
        const val DATABASE_NAME = "notification_history.db"
    }
}

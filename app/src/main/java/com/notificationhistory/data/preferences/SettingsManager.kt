package com.notificationhistory.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore
    private val blacklistKey = stringSetPreferencesKey("blacklist")
    private val retentionDaysKey = intPreferencesKey("retention_days")
    private val lockTimeoutMinutesKey = intPreferencesKey("lock_timeout_minutes")

    val blacklistFlow: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[blacklistKey] ?: emptySet()
    }

    val retentionDaysFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[retentionDaysKey] ?: DEFAULT_RETENTION_DAYS
    }

    val lockTimeoutMinutesFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[lockTimeoutMinutesKey] ?: 0
    }

    fun isBlacklisted(packageName: String): Boolean {
        val blacklist = runBlocking { blacklistFlow.first() }
        return packageName in blacklist
    }

    suspend fun getRetentionDays(): Int = retentionDaysFlow.first()

    suspend fun setRetentionDays(days: Int) {
        dataStore.edit { prefs ->
            prefs[retentionDaysKey] = days.coerceIn(7, 365)
        }
    }

    suspend fun getLockTimeoutMinutes(): Int = lockTimeoutMinutesFlow.first()

    suspend fun setLockTimeoutMinutes(minutes: Int) {
        dataStore.edit { prefs ->
            prefs[lockTimeoutMinutesKey] = minutes.coerceIn(0, 60)
        }
    }

    suspend fun addToBlacklist(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[blacklistKey] ?: emptySet()
            prefs[blacklistKey] = current + packageName
        }
    }

    suspend fun removeFromBlacklist(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[blacklistKey] ?: emptySet()
            prefs[blacklistKey] = current - packageName
        }
    }

    suspend fun setBlacklisted(packageName: String, blocked: Boolean) {
        if (blocked) addToBlacklist(packageName) else removeFromBlacklist(packageName)
    }

    companion object {
        const val DEFAULT_RETENTION_DAYS = 90
    }
}

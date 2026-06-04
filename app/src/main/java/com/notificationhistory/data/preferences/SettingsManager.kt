package com.notificationhistory.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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

    val blacklistFlow: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[blacklistKey] ?: emptySet()
    }

    fun isBlacklisted(packageName: String): Boolean {
        val blacklist = runBlocking { blacklistFlow.first() }
        return packageName in blacklist
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
}

package com.notificationhistory.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun isBlacklisted(packageName: String): Boolean {
        return prefs.getStringSet("blacklist", emptySet())?.contains(packageName) ?: false
    }

    fun addToBlacklist(packageName: String) {
        val current = prefs.getStringSet("blacklist", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(packageName)
        prefs.edit().putStringSet("blacklist", current).apply()
    }

    fun removeFromBlacklist(packageName: String) {
        val current = prefs.getStringSet("blacklist", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(packageName)
        prefs.edit().putStringSet("blacklist", current).apply()
    }

    fun getBlacklist(): Set<String> {
        return prefs.getStringSet("blacklist", emptySet()) ?: emptySet()
    }
}

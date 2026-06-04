package com.notificationhistory.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notificationhistory.data.preferences.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledAppRow(
    val packageName: String,
    val label: String,
    val isBlacklisted: Boolean
)

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val installedApps: List<InstalledAppRow> = loadInstalledApps()

    val apps: StateFlow<List<InstalledAppRow>> = combine(
        settingsManager.blacklistFlow,
        searchQuery
    ) { blacklist, query ->
        installedApps
            .map { row -> row.copy(isBlacklisted = row.packageName in blacklist) }
            .filter { row ->
                query.isBlank() ||
                    row.label.contains(query, ignoreCase = true) ||
                    row.packageName.contains(query, ignoreCase = true)
            }
            .sortedBy { it.label.lowercase() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleBlacklist(packageName: String, blocked: Boolean) {
        viewModelScope.launch {
            settingsManager.setBlacklisted(packageName, blocked)
        }
    }

    private fun loadInstalledApps(): List<InstalledAppRow> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { resolve ->
                val packageName = resolve.activityInfo.packageName
                if (packageName == context.packageName) return@mapNotNull null
                val label = resolve.loadLabel(pm).toString()
                InstalledAppRow(packageName, label, isBlacklisted = false)
            }
            .distinctBy { it.packageName }
    }
}

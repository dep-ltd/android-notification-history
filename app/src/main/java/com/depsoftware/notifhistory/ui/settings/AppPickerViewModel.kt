package com.depsoftware.notifhistory.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depsoftware.notifhistory.data.preferences.SettingsManager
import com.depsoftware.notifhistory.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class InstalledAppRow(
    val packageName: String,
    val label: String,
    val isBlacklisted: Boolean
)

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val installedApps = MutableStateFlow<List<InstalledAppRow>>(emptyList())
    private val isLoading = MutableStateFlow(true)

    val isLoadingApps: StateFlow<Boolean> = isLoading.asStateFlow()

    val apps: StateFlow<List<InstalledAppRow>> = combine(
        installedApps,
        settingsManager.blacklistFlow,
        searchQuery
    ) { apps, blacklist, query ->
        apps
            .map { row -> row.copy(isBlacklisted = row.packageName in blacklist) }
            .filter { row ->
                query.isBlank() ||
                    row.label.contains(query, ignoreCase = true) ||
                    row.packageName.contains(query, ignoreCase = true)
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            val historyPackages = try {
                notificationRepository.observeDistinctPackages().first()
            } catch (_: Exception) {
                emptyList()
            }
            val loaded = withContext(Dispatchers.Default) {
                InstalledAppsLoader.load(context, historyPackages)
            }
            installedApps.value = loaded
            isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleBlacklist(packageName: String, blocked: Boolean) {
        viewModelScope.launch {
            settingsManager.setBlacklisted(packageName, blocked)
        }
    }
}

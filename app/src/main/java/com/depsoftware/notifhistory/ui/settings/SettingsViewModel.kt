package com.depsoftware.notifhistory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depsoftware.notifhistory.data.preferences.SettingsManager
import com.depsoftware.notifhistory.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val repository: NotificationRepository
) : ViewModel() {

    val retentionDays: StateFlow<Int> = settingsManager.retentionDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsManager.DEFAULT_RETENTION_DAYS)

    val pinReentryHours: StateFlow<Int> = settingsManager.pinReentryHoursFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SettingsManager.DEFAULT_PIN_REENTRY_HOURS
        )

    fun setRetentionDays(days: Int) {
        viewModelScope.launch { settingsManager.setRetentionDays(days) }
    }

    fun setPinReentryHours(hours: Int) {
        viewModelScope.launch { settingsManager.setPinReentryHours(hours) }
    }

    fun clearHistory(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllHistory()
            onDone()
        }
    }
}

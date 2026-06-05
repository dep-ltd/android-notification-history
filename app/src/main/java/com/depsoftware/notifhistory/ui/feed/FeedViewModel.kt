package com.depsoftware.notifhistory.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import com.depsoftware.notifhistory.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    repository: NotificationRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val packageFilter = MutableStateFlow<String?>(null)

    val availablePackages: StateFlow<List<String>> = repository.observeDistinctPackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQueryState: StateFlow<String> = searchQuery

    val packageFilterState: StateFlow<String?> = packageFilter

    val hasActiveFilters: StateFlow<Boolean> = combine(searchQuery, packageFilter) { query, pkg ->
        query.isNotBlank() || pkg != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val notifications: StateFlow<List<NotificationEvent>> = combine(
        repository.allNotifications,
        searchQuery.debounce(SEARCH_DEBOUNCE_MS),
        packageFilter
    ) { events, query, pkg ->
        if (query.isBlank() && pkg == null) {
            events
        } else {
            events.filter { event -> matchesFilters(event, query, pkg) }
        }
    }.distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setPackageFilter(packageName: String?) {
        packageFilter.value = packageName
    }

    private fun matchesFilters(
        event: NotificationEvent,
        query: String,
        packageName: String?
    ): Boolean {
        if (packageName != null && event.packageName != packageName) return false
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return listOfNotNull(
            event.title,
            event.text,
            event.appLabel,
            event.packageName
        ).any { it.lowercase().contains(q) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 250L
    }
}

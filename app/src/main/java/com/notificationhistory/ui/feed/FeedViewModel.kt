package com.notificationhistory.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notificationhistory.data.entities.NotificationEvent
import com.notificationhistory.data.models.FeedListItem
import com.notificationhistory.data.models.NotificationFeedGrouper
import com.notificationhistory.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    repository: NotificationRepository
) : ViewModel() {

    private val expandedGroupKeys = MutableStateFlow<Set<String>>(emptySet())
    private val searchQuery = MutableStateFlow("")
    private val packageFilter = MutableStateFlow<String?>(null)

    val availablePackages: StateFlow<List<String>> = repository.observeDistinctPackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQueryState: StateFlow<String> = searchQuery

    val packageFilterState: StateFlow<String?> = packageFilter

    val feedItems: StateFlow<List<FeedListItem>> = combine(
        repository.allNotifications,
        searchQuery,
        packageFilter,
        expandedGroupKeys
    ) { events, query, pkg, expanded ->
        val filtered = events.filter { event -> matchesFilters(event, query, pkg) }
        NotificationFeedGrouper.group(filtered, expanded)
    }.stateIn(
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

    fun toggleGroup(groupKey: String) {
        expandedGroupKeys.update { current ->
            if (groupKey in current) current - groupKey else current + groupKey
        }
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
}

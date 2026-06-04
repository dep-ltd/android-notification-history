package com.notificationhistory.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val feedItems: StateFlow<List<FeedListItem>> = combine(
        repository.allNotifications,
        expandedGroupKeys
    ) { events, expanded ->
        NotificationFeedGrouper.group(events, expanded)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleGroup(groupKey: String) {
        expandedGroupKeys.update { current ->
            if (groupKey in current) current - groupKey else current + groupKey
        }
    }
}

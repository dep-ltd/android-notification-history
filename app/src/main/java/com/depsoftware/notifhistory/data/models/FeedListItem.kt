package com.depsoftware.notifhistory.data.models

import com.depsoftware.notifhistory.data.entities.NotificationEvent

sealed class FeedListItem {
    abstract val sortKey: Long

    data class Single(val event: NotificationEvent) : FeedListItem() {
        override val sortKey: Long = event.postedAt
    }

    // events are sorted newest-first; shown as a collapsible group in the feed.
    data class Group(
        val groupKey: String,
        val events: List<NotificationEvent>,
        val isExpanded: Boolean
    ) : FeedListItem() {
        override val sortKey: Long = events.maxOfOrNull { it.postedAt } ?: 0L
    }
}

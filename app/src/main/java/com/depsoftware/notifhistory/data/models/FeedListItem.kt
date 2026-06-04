package com.depsoftware.notifhistory.data.models

import com.depsoftware.notifhistory.data.entities.NotificationEvent

sealed class FeedListItem {
    abstract val sortKey: Long

    data class Single(val event: NotificationEvent) : FeedListItem() {
        override val sortKey: Long = event.postedAt
    }

    data class Group(
        val groupKey: String,
        val summary: NotificationEvent?,
        val children: List<NotificationEvent>,
        val isExpanded: Boolean
    ) : FeedListItem() {
        override val sortKey: Long = (summary?.postedAt ?: children.maxOfOrNull { it.postedAt } ?: 0L)
    }
}

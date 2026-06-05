package com.depsoftware.notifhistory.data.models

import com.depsoftware.notifhistory.data.entities.NotificationEvent

object NotificationFeedGrouper {

    fun group(
        events: List<NotificationEvent>,
        expandedGroupKeys: Set<String>
    ): List<FeedListItem> {
        if (events.isEmpty()) return emptyList()

        val items = mutableListOf<FeedListItem>()
        val withGroupKey = events.filter { !it.groupKey.isNullOrBlank() }
        val noGroupKey = events.filter { it.groupKey.isNullOrBlank() }

        // Notifications with a groupKey: collapse into a Group only when 2+ exist
        withGroupKey
            .groupBy { it.groupKey!! }
            .forEach { (groupKey, grouped) ->
                val sorted = grouped.sortedByDescending { it.postedAt }
                if (sorted.size == 1) {
                    items.add(FeedListItem.Single(sorted.first()))
                } else {
                    items.add(
                        FeedListItem.Group(
                            groupKey = groupKey,
                            events = sorted,
                            isExpanded = groupKey in expandedGroupKeys
                        )
                    )
                }
            }

        // Notifications without a groupKey are always shown individually
        noGroupKey.forEach { items.add(FeedListItem.Single(it)) }

        return items.sortedByDescending { it.sortKey }
    }
}

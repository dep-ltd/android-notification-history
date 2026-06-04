package com.notificationhistory.data.models

import com.notificationhistory.data.entities.NotificationEvent

object NotificationFeedGrouper {

    fun group(
        events: List<NotificationEvent>,
        expandedGroupKeys: Set<String>
    ): List<FeedListItem> {
        if (events.isEmpty()) return emptyList()

        val consumed = mutableSetOf<Long>()
        val items = mutableListOf<FeedListItem>()

        val groupKeys = events.mapNotNull { it.groupKey }.distinct()
        for (groupKey in groupKeys) {
            val inGroup = events.filter { it.groupKey == groupKey }
            if (inGroup.isEmpty()) continue

            val summary = inGroup.find { it.isGroupSummary }
            val children = inGroup.filter { !it.isGroupSummary }
            if (summary == null && children.size <= 1) {
                val lone = children.singleOrNull() ?: summary
                lone?.let {
                    if (it.id !in consumed) {
                        items.add(FeedListItem.Single(it))
                        consumed.add(it.id)
                    }
                }
                continue
            }

            items.add(
                FeedListItem.Group(
                    groupKey = groupKey,
                    summary = summary,
                    children = children.sortedByDescending { it.postedAt },
                    isExpanded = groupKey in expandedGroupKeys
                )
            )
            consumed.addAll(inGroup.map { it.id })
        }

        events.filter { it.id !in consumed }.forEach { event ->
            items.add(FeedListItem.Single(event))
        }

        return items.sortedByDescending { it.sortKey }
    }
}

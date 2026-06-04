package com.notificationhistory.data.models

import com.notificationhistory.data.entities.NotificationEvent
import com.notificationhistory.data.entities.hasDisplayableContent

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

            val children = inGroup
                .filter { !it.isGroupSummary }
                .sortedByDescending { it.postedAt }
            val summary = inGroup
                .find { it.isGroupSummary && it.hasDisplayableContent() }

            if (children.isEmpty() && summary == null) {
                inGroup.filter { it.isGroupSummary }.forEach { summaryOnly ->
                    if (summaryOnly.id !in consumed) {
                        items.add(FeedListItem.Single(summaryOnly))
                        consumed.add(summaryOnly.id)
                    }
                }
                continue
            }

            if (children.isEmpty() && summary != null) {
                items.add(
                    FeedListItem.Group(
                        groupKey = groupKey,
                        summary = summary,
                        children = emptyList(),
                        isExpanded = groupKey in expandedGroupKeys
                    )
                )
                consumed.addAll(inGroup.map { it.id })
                continue
            }

            if (children.isEmpty()) continue

            items.add(
                FeedListItem.Group(
                    groupKey = groupKey,
                    summary = summary,
                    children = children,
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

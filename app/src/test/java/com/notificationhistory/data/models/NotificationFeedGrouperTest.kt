package com.notificationhistory.data.models

import com.notificationhistory.data.entities.NotificationEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationFeedGrouperTest {

    @Test
    fun group_ignoresEmptySummaryAndKeepsChildren() {
        val summary = event(
            id = 1L,
            groupKey = "g1",
            isGroupSummary = true,
            title = null,
            text = null
        )
        val child = event(
            id = 2L,
            groupKey = "g1",
            title = "Alice",
            text = "Hi"
        )
        val items = NotificationFeedGrouper.group(listOf(summary, child), emptySet())
        assertEquals(1, items.size)
        val group = items[0] as FeedListItem.Group
        assertEquals(null, group.summary)
        assertEquals(1, group.children.size)
        assertEquals("Hi", group.children[0].text)
    }

    @Test
    fun group_keepsSummaryWhenItHasContent() {
        val summary = event(
            id = 1L,
            groupKey = "g1",
            isGroupSummary = true,
            title = "3 new messages",
            text = "Group"
        )
        val child = event(
            id = 2L,
            groupKey = "g1",
            title = "Msg",
            text = "Body"
        )
        val items = NotificationFeedGrouper.group(listOf(summary, child), emptySet())
        val group = items[0] as FeedListItem.Group
        assertEquals("3 new messages", group.summary?.title)
        assertEquals(1, group.children.size)
    }

    private fun event(
        id: Long,
        groupKey: String? = null,
        isGroupSummary: Boolean = false,
        title: String? = "Title",
        text: String? = "Text"
    ) = NotificationEvent(
        id = id,
        stableKey = "pkg:$id",
        packageName = "com.example",
        appLabel = "Example",
        postedAt = id,
        title = title,
        text = text,
        channelId = null,
        groupKey = groupKey,
        isGroupSummary = isGroupSummary
    )
}

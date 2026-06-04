package com.notificationhistory.service

import android.app.Notification
import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationExtrasReaderTest {

    @Test
    fun parse_readsTitleAndText() {
        val extras = Bundle().apply {
            putString(Notification.EXTRA_TITLE, "Alice")
            putCharSequence(Notification.EXTRA_TEXT, "Hello")
        }
        val fields = NotificationExtrasReader.parse(extras, isGroupSummary = false, appLabel = "Chat")
        assertEquals("Alice", fields.title)
        assertEquals("Hello", fields.text)
    }

    @Test
    fun parse_usesMessagingStyleLatestMessage() {
        val message = Bundle().apply {
            putCharSequence("text", "Photo sent")
            putCharSequence("sender", "Bob")
            putLong("time", 2L)
        }
        val older = Bundle().apply {
            putCharSequence("text", "Hi")
            putCharSequence("sender", "Bob")
            putLong("time", 1L)
        }
        val extras = Bundle().apply {
            putParcelableArray("android.messages", arrayOf(older, message))
        }
        val fields = NotificationExtrasReader.parse(extras, isGroupSummary = false, appLabel = "Chat")
        assertEquals("Bob", fields.title)
        assertEquals("Photo sent", fields.text)
    }

    @Test
    fun parse_usesLastInboxLine() {
        val extras = Bundle().apply {
            putString(Notification.EXTRA_TITLE, "Mail")
            putCharSequenceArray(
                Notification.EXTRA_TEXT_LINES,
                arrayOf("First", "Second", "Third")
            )
        }
        val fields = NotificationExtrasReader.parse(extras, isGroupSummary = false, appLabel = "Mail")
        assertEquals("Mail", fields.title)
        assertEquals("Third", fields.text)
    }

    @Test
    fun parse_groupSummaryFallsBackToAppLabel() {
        val fields = NotificationExtrasReader.parse(
            Bundle.EMPTY,
            isGroupSummary = true,
            appLabel = "Telegram"
        )
        assertEquals("Telegram", fields.title)
        assertNull(fields.text)
    }
}

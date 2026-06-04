package com.notificationhistory.service

import android.app.Notification
import android.os.Build
import android.os.Bundle

/**
 * Extracts human-readable title/text from notification extras, including
 * MessagingStyle, InboxStyle, BigText, and group summaries.
 */
object NotificationExtrasReader {

    private const val EXTRA_CONVERSATION_TITLE = "android.conversationTitle"
    private const val EXTRA_MESSAGES = "android.messages"
    private const val MSG_KEY_TEXT = "text"
    private const val MSG_KEY_SENDER = "sender"
    private const val MSG_KEY_TIMESTAMP = "time"

    data class MessageLine(
        val sender: String?,
        val text: String?
    )

    fun parse(extras: Bundle, isGroupSummary: Boolean, appLabel: String?): ParsedNotificationFields {
        val messaging = latestMessagingLine(extras)
        var title = firstNonBlankCharSequence(
            extras,
            Notification.EXTRA_TITLE,
            EXTRA_CONVERSATION_TITLE
        ) ?: messaging?.sender?.takeIf { it.isNotBlank() }

        var text = firstNonBlankCharSequence(
            extras,
            Notification.EXTRA_TEXT,
            Notification.EXTRA_BIG_TEXT,
            Notification.EXTRA_SUMMARY_TEXT,
            Notification.EXTRA_INFO_TEXT,
            Notification.EXTRA_SUB_TEXT
        ) ?: messaging?.text?.takeIf { it.isNotBlank() }
            ?: lastTextLine(extras)

        if (isGroupSummary) {
            title = title ?: appLabel
            text = text ?: groupSummaryFallbackText(extras)
        } else if (title.isNullOrBlank() && !text.isNullOrBlank() && messaging != null) {
            // Chat child: keep sender in title when we promoted it from messaging
        } else if (title.isNullOrBlank() && messaging?.sender != null && !messaging.text.isNullOrBlank()) {
            title = messaging.sender
            text = messaging.text
        }

        return ParsedNotificationFields(
            title = title?.trim()?.takeIf { it.isNotEmpty() },
            text = text?.trim()?.takeIf { it.isNotEmpty() }
        )
    }

    private fun groupSummaryFallbackText(extras: Bundle): String? {
        return firstNonBlankCharSequence(
            extras,
            Notification.EXTRA_SUB_TEXT,
            Notification.EXTRA_INFO_TEXT
        ) ?: lastTextLine(extras)
    }

    private fun firstNonBlankCharSequence(extras: Bundle, vararg keys: String): String? {
        for (key in keys) {
            val value = extras.getCharSequence(key)?.toString()?.trim()
            if (!value.isNullOrEmpty()) return value
        }
        return null
    }

    private fun lastTextLine(extras: Bundle): String? {
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) ?: return null
        return lines.mapNotNull { it?.toString()?.trim() }
            .filter { it.isNotEmpty() }
            .lastOrNull()
    }

    private fun latestMessagingLine(extras: Bundle): MessageLine? {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelableArray(EXTRA_MESSAGES, Bundle::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelableArray(EXTRA_MESSAGES)
        } ?: return null

        val messages = raw.filterIsInstance<Bundle>()
        if (messages.isEmpty()) return null

        val latest = messages.maxByOrNull { it.getLong(MSG_KEY_TIMESTAMP, 0L) } ?: messages.last()
        val text = latest.getCharSequence(MSG_KEY_TEXT)?.toString()?.trim()
        val sender = latest.getCharSequence(MSG_KEY_SENDER)?.toString()?.trim()
        if (text.isNullOrEmpty() && sender.isNullOrEmpty()) return null
        return MessageLine(sender = sender, text = text)
    }
}

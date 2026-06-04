package com.notificationhistory.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.notificationhistory.data.entities.NotificationEvent
import com.notificationhistory.util.MediaStorage
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedNotificationFields(
    val title: String?,
    val text: String?
)

@Singleton
class NotificationParser @Inject constructor(
    private val mediaStorage: MediaStorage
) {

    fun parseExtras(extras: Bundle): ParsedNotificationFields {
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        return ParsedNotificationFields(title = title, text = text)
    }

    fun parse(
        packageName: String,
        notificationKey: String,
        postedAt: Long,
        extras: Bundle,
        appLabel: String?,
        channelId: String?,
        groupKey: String?,
        isGroupSummary: Boolean,
        smallIconBitmap: Bitmap?
    ): NotificationEvent {
        val fields = parseExtras(extras)
        val iconPath = smallIconBitmap?.let { mediaStorage.saveBitmap(it) }

        return NotificationEvent(
            stableKey = stableKey(packageName, notificationKey),
            packageName = packageName,
            appLabel = appLabel,
            postedAt = postedAt,
            title = fields.title,
            text = fields.text,
            channelId = channelId,
            groupKey = groupKey,
            isGroupSummary = isGroupSummary,
            mediaPath = iconPath
        )
    }

    fun cacheIcon(bitmap: Bitmap?): String? {
        return bitmap?.let { mediaStorage.saveBitmap(it) }
    }

    companion object {
        fun stableKey(packageName: String, notificationKey: String): String =
            "$packageName:$notificationKey"

        fun bitmapFromDrawable(drawable: Drawable, size: Int = drawable.intrinsicWidth.coerceAtLeast(1)): Bitmap {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else size
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else size
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}

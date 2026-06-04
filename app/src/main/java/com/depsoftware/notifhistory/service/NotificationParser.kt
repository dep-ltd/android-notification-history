package com.depsoftware.notifhistory.service

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import com.depsoftware.notifhistory.util.MediaStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedNotificationFields(
    val title: String?,
    val text: String?
)

@Singleton
class NotificationParser @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaStorage: MediaStorage
) {

    fun parseExtras(
        extras: Bundle,
        isGroupSummary: Boolean = false,
        appLabel: String? = null
    ): ParsedNotificationFields {
        return NotificationExtrasReader.parse(extras, isGroupSummary, appLabel)
    }

    fun parse(
        sbn: StatusBarNotification,
        appLabel: String?,
        smallIconBitmap: Bitmap?
    ): NotificationEvent {
        val notification = sbn.notification
        val extras = notification.extras
        val isGroupSummary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        val fields = parseExtras(extras, isGroupSummary, appLabel)
        val mediaPaths = buildMediaPaths(context, extras, notification, smallIconBitmap)

        return NotificationEvent(
            stableKey = stableKey(sbn.packageName, sbn.key),
            packageName = sbn.packageName,
            appLabel = appLabel,
            postedAt = sbn.postTime,
            title = fields.title,
            text = fields.text,
            channelId = notification.channelId,
            groupKey = resolveGroupKey(sbn, notification),
            isGroupSummary = isGroupSummary,
            clickUri = parseClickUri(extras),
            mediaPaths = mediaPaths
        )
    }

    fun cacheIcon(bitmap: Bitmap?): String? = bitmap?.let { mediaStorage.saveBitmap(it) }

    private fun buildMediaPaths(
        context: Context,
        extras: Bundle,
        notification: Notification,
        smallIconBitmap: Bitmap?
    ): List<String> {
        val paths = mutableListOf<String>()
        smallIconBitmap?.let { mediaStorage.saveBitmap(it) }?.let { paths.add(it) }

        @Suppress("DEPRECATION")
        val legacyLargeIcon = extras.getParcelable<Bitmap>(Notification.EXTRA_LARGE_ICON)
        legacyLargeIcon?.let { mediaStorage.saveBitmap(it) }?.let { paths.add(it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification.getLargeIcon()?.let { icon ->
                bitmapFromIcon(icon, context)?.let { mediaStorage.saveBitmap(it) }
            }?.let { paths.add(it) }
        }

        val bigPicture = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_PICTURE, Bitmap::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_PICTURE)
        }
        bigPicture?.let { mediaStorage.saveBitmap(it) }?.let { paths.add(it) }

        return paths.distinct()
    }

    fun parseClickUri(extras: Bundle): String? {
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        return extractHttpUri(text)
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.let { extractHttpUri(it) }
    }

    private fun resolveGroupKey(
        sbn: StatusBarNotification,
        notification: Notification
    ): String? {
        return sbn.groupKey?.takeIf { it.isNotEmpty() }
            ?: notification.group?.takeIf { it.isNotEmpty() }
    }

    private fun extractHttpUri(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val trimmed = value.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        return HTTP_URI_REGEX.find(trimmed)?.value
    }

    companion object {
        private val HTTP_URI_REGEX = Regex("https?://\\S+")

        fun stableKey(packageName: String, notificationKey: String): String =
            "$packageName:$notificationKey"

        fun bitmapFromDrawable(drawable: Drawable): Bitmap {
            val width = drawable.intrinsicWidth.coerceAtLeast(1)
            val height = drawable.intrinsicHeight.coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        fun bitmapFromIcon(icon: Icon, context: android.content.Context): Bitmap? {
            val drawable = icon.loadDrawable(context) ?: return null
            return bitmapFromDrawable(drawable)
        }
    }
}

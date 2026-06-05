package com.depsoftware.notifhistory.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.ConcurrentHashMap

object AppIconCache {
    private const val CACHE_PX = 96
    private val cache = ConcurrentHashMap<String, ImageBitmap>()

    fun get(packageName: String): ImageBitmap? = cache[packageName]

    fun load(context: Context, packageName: String): ImageBitmap? {
        cache[packageName]?.let { return it }
        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            val width = drawable.intrinsicWidth.coerceIn(1, CACHE_PX)
            val height = drawable.intrinsicHeight.coerceIn(1, CACHE_PX)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).also { canvas ->
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
            }
            val image = bitmap.asImageBitmap()
            cache[packageName] = image
            image
        } catch (_: Exception) {
            null
        }
    }

    fun clear() {
        cache.clear()
    }
}

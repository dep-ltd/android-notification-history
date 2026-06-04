package com.depsoftware.notifhistory.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MediaStorage(private val context: Context) {
    private val mediaDir = File(context.filesDir, "media")

    init {
        if (!mediaDir.exists()) mediaDir.mkdirs()
    }

    fun saveBitmap(bitmap: Bitmap, maxSide: Int = MAX_SIDE_PX): String? {
        val scaled = scaleDown(bitmap, maxSide) ?: return null
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(mediaDir, fileName)
        return try {
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            if (scaled !== bitmap) scaled.recycle()
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun loadBitmap(path: String): Bitmap? = BitmapFactory.decodeFile(path)

    fun deleteFile(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }

    private fun scaleDown(bitmap: Bitmap, maxSide: Int): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSide && height <= maxSide) return bitmap
        val ratio = minOf(maxSide.toFloat() / width, maxSide.toFloat() / height)
        val targetW = (width * ratio).toInt().coerceAtLeast(1)
        val targetH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    companion object {
        const val MAX_SIDE_PX = 512
        const val JPEG_QUALITY = 85
    }
}

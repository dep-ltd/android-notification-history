package com.notificationhistory.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MediaStorage(private val context: Context) {
    private val mediaDir = File(context.filesDir, "media")

    init {
        if (!mediaDir.exists()) mediaDir.mkdirs()
    }

    fun saveBitmap(bitmap: Bitmap): String? {
        val fileName = "${UUID.randomUUID()}.png"
        val file = File(mediaDir, fileName)
        return try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun loadBitmap(path: String): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }

    fun deleteFile(path: String) {
        val file = File(path)
        if (file.exists()) file.delete()
    }
}

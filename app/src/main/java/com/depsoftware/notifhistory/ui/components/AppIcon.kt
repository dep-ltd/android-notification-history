package com.depsoftware.notifhistory.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppIcon(packageName: String, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(initialValue = null, key1 = packageName) {
        value = withContext(Dispatchers.Default) {
            try {
                val drawable = context.packageManager.getApplicationIcon(packageName)
                val width = drawable.intrinsicWidth.coerceIn(1, 192)
                val height = drawable.intrinsicHeight.coerceIn(1, 192)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                Canvas(bitmap).also { canvas ->
                    drawable.setBounds(0, 0, width, height)
                    drawable.draw(canvas)
                }
                bitmap.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }
    val image = icon
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = modifier.size(size)
        )
    } else {
        Spacer(modifier.size(size))
    }
}

package com.depsoftware.notifhistory.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.depsoftware.notifhistory.util.AppIconCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppIcon(packageName: String, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val cached = AppIconCache.get(packageName)
    val icon by produceState<ImageBitmap?>(initialValue = cached, key1 = packageName) {
        if (value == null) {
            value = withContext(Dispatchers.Default) {
                AppIconCache.load(context, packageName)
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

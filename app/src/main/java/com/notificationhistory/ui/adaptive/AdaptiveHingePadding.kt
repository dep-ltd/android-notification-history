package com.notificationhistory.ui.adaptive

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

@Composable
fun Modifier.adaptiveHingePadding(): Modifier {
    val context = LocalContext.current
    val padding = produceState(initialValue = PaddingValues(0.dp), context) {
        WindowInfoTracker.getOrCreate(context)
            .windowLayoutInfo(context)
            .collect { info ->
                val fold = info.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
                value = if (fold == null || fold.state != FoldingFeature.State.HALF_OPENED) {
                    PaddingValues(0.dp)
                } else {
                    PaddingValues(12.dp)
                }
            }
    }
    return padding(padding.value)
}

package com.depsoftware.notifhistory.ui.adaptive

import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass {
    val activity = LocalContext.current as ComponentActivity
    return calculateWindowSizeClass(activity).widthSizeClass
}

@Composable
fun isCompactWidth(): Boolean =
    rememberWindowWidthSizeClass() == WindowWidthSizeClass.Compact

@Composable
fun isExpandedWidth(): Boolean =
    rememberWindowWidthSizeClass() == WindowWidthSizeClass.Expanded

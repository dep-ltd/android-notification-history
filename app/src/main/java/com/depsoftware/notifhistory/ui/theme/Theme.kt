package com.depsoftware.notifhistory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color.White,
    primaryContainer = Indigo90,
    onPrimaryContainer = Indigo10,
    secondary = Indigo20,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E0FF),
    onSecondaryContainer = Indigo10,
    tertiary = Color(0xFF6B5B95),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0E6FF),
    onTertiaryContainer = Color(0xFF251A47),
    error = Error40,
    onError = Color.White,
    errorContainer = Error90,
    onErrorContainer = Color(0xFF410002),
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Color(0xFFE6E0EC),
    onSurfaceVariant = Color(0xFF48454E),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF1ECF4),
    surfaceContainerHigh = Color(0xFFEBE6EE),
    surfaceContainerHighest = Color(0xFFE6E0EC),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4CF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo80,
    onPrimary = Indigo10,
    primaryContainer = Indigo20,
    onPrimaryContainer = Indigo90,
    secondary = Color(0xFFC8C5D8),
    onSecondary = Color(0xFF302E42),
    secondaryContainer = Color(0xFF464559),
    onSecondaryContainer = Color(0xFFE4E0F5),
    tertiary = Color(0xFFD4BBFF),
    onTertiary = Color(0xFF3B2556),
    tertiaryContainer = Color(0xFF523C6E),
    onTertiaryContainer = Color(0xFFF0E6FF),
    error = Error80,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Error90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Color(0xFFC8C5D0),
    surfaceContainerLowest = Color(0xFF0F0F14),
    surfaceContainerLow = Neutral20,
    surfaceContainer = Color(0xFF32323C),
    surfaceContainerHigh = Neutral30,
    surfaceContainerHighest = Color(0xFF484851),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF48454E)
)

@Composable
fun NotificationHistoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

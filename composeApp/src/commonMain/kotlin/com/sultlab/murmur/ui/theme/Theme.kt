package com.sultlab.murmur.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary             = Color.Black,
    onPrimary           = Color.White,
    primaryContainer    = Color.DarkGray,
    onPrimaryContainer  = Color.White,
    secondary           = AccentDim,
    onSecondary         = AccentLight,
    background          = Background,
    onBackground        = TextPrimary,
    surface             = Surface,
    onSurface           = TextPrimary,
    surfaceVariant      = Surface2,
    onSurfaceVariant    = TextMuted,
    outline             = Border,
    error               = Color(0xFFE24B4A),
    onError             = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary             = Color.Black,
    onPrimary           = Color.White,
    primaryContainer    = Color.LightGray,
    onPrimaryContainer  = Color.Black,
    secondary           = Color(0xFF625B71),
    onSecondary         = Color.White,
    background          = Color.White,
    onBackground        = Color.Black,
    surface             = Color.White,
    onSurface           = Color.Black,
    surfaceVariant      = Color(0xFFE7E0EC),
    onSurfaceVariant    = Color(0xFF49454F),
    outline             = Color(0xFF79747E),
    error               = Color(0xFFB3261E),
    onError             = Color.White,
)

@Composable
fun MurmurTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

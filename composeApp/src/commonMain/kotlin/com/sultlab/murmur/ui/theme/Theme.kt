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
    primary             = Dark,
    onPrimary           = White,
    primaryContainer    = Orange,
    onPrimaryContainer  = White,
    secondary           = Orange,
    onSecondary         = White,
    tertiary            = Blue,
    onTertiary          = White,
    background          = Cream,
    onBackground        = Dark,
    surface             = White,
    onSurface           = Dark,
    surfaceVariant      = White,
    onSurfaceVariant    = Muted,
    outline             = Muted.copy(alpha = 0.2f),
    error               = Coral,
    onError             = White,
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

package com.sultlab.murmur.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary             = BrandPurple,
    onPrimary           = Color.Black,
    primaryContainer    = BrandPurple.copy(alpha = 0.2f),
    onPrimaryContainer  = BrandPurple,
    secondary           = BrandPurple,
    onSecondary         = Color.Black,
    background          = BrandDark,
    onBackground        = TextPrimary,
    surface             = BrandSurface,
    onSurface           = TextPrimary,
    surfaceVariant      = Surface2,
    onSurfaceVariant    = TextMuted,
    outline             = BrandPurple.copy(alpha = 0.3f),
    error               = Color(0xFFE24B4A),
    onError             = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary             = BrandPurple,
    onPrimary           = White,
    primaryContainer    = BrandPurple.copy(alpha = 0.1f),
    onPrimaryContainer  = BrandPurple,
    secondary           = BrandPurple,
    onSecondary         = White,
    tertiary            = Blue,
    onTertiary          = White,
    background          = Cream,
    onBackground        = Dark,
    surface             = White,
    onSurface           = Dark,
    surfaceVariant      = White,
    onSurfaceVariant    = Muted,
    outline             = BrandPurple.copy(alpha = 0.2f),
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

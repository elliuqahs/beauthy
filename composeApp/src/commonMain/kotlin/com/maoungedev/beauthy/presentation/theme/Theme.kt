package com.maoungedev.beauthy.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary palette
private val DeepNavy = Color(0xFF0D3B66)
private val Teal = Color(0xFF028090)
private val MintGreen = Color(0xFF02C39A)

// Background & surface
private val DarkBackground = Color(0xFF0A1628)
private val CardDark = Color(0xFF1A2D4A)
val TimerTrack = Color(0xFF243B5C)

// Text
private val SlateGray = Color(0xFF64748B)

private val BeauthyColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = Color.White,
    primaryContainer = DeepNavy,
    onPrimaryContainer = Color.White,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = CardDark,
    onSecondaryContainer = MintGreen,
    tertiary = MintGreen,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkBackground,
    onSurface = Color.White,
    surfaceVariant = CardDark,
    onSurfaceVariant = SlateGray,
    surfaceContainerHighest = CardDark,
    error = Color(0xFFF87171),
    onError = Color.White,
    outline = SlateGray,
    outlineVariant = SlateGray.copy(alpha = 0.3f),
)

@Composable
fun BeauthyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BeauthyColorScheme,
        content = content
    )
}

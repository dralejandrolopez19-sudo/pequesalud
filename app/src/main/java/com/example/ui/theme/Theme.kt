package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val PequeSaludLightColorScheme = lightColorScheme(
    primary = PequeBlue,
    onPrimary = Color.White,
    primaryContainer = PequeLightBlue,
    onPrimaryContainer = PequeNavy,
    secondary = PequeGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F6EB),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = PequePurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE8FD),
    onTertiaryContainer = Color(0xFF4C1D95),
    background = PequeSkyBg,
    onBackground = PequeNavy,
    surface = Color.White,
    onSurface = PequeNavy,
    surfaceVariant = Color(0xFFEEF4FC),
    onSurfaceVariant = Color(0xFF4B6382),
    error = AlertRed,
    onError = Color.White
)

private val PequeSaludDarkColorScheme = darkColorScheme(
    primary = Color(0xFF5CA4FF),
    onPrimary = Color(0xFF062142),
    primaryContainer = Color(0xFF1B3A63),
    onPrimaryContainer = Color(0xFFDCEBFE),
    secondary = Color(0xFF2CE0A3),
    onSecondary = Color(0xFF043322),
    secondaryContainer = Color(0xFF104D38),
    onSecondaryContainer = Color(0xFFD8F6EB),
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF28115B),
    tertiaryContainer = Color(0xFF362270),
    onTertiaryContainer = Color(0xFFEDE8FD),
    background = PequeDarkBg,
    onBackground = PequeDarkOnSurface,
    surface = PequeDarkSurface,
    onSurface = PequeDarkOnSurface,
    surfaceVariant = PequeDarkSurfaceVariant,
    onSurfaceVariant = PequeDarkOnSurfaceVariant,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF3B0808)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PequeSaludDarkColorScheme else PequeSaludLightColorScheme
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

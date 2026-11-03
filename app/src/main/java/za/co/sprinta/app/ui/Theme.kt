package za.co.sprinta.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SprintaGold,
    onPrimary = SprintaBlack,
    secondary = SprintaGold,
    onSecondary = SprintaBlack,
    background = SprintaBlack,
    surface = SprintaDarkSurface,
    surfaceVariant = SprintaDarkCard,
    onBackground = SprintaWhite,
    onSurface = SprintaWhite,
    outline = BorderGray,
    error = SprintaError
)

private val LightColorScheme = lightColorScheme(
    primary = SprintaGold,
    onPrimary = SprintaWhite,
    secondary = SprintaGold,
    onSecondary = SprintaWhite,
    background = SprintaLightBg,
    surface = SprintaLightSurface,
    surfaceVariant = SprintaLightSurface,
    onBackground = SprintaLightText,
    onSurface = SprintaLightText,
    outline = BorderGrayLight,
    error = SprintaError
)

@Composable
fun SprintaTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkMode) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SprintaTypography,
        content = content
    )
}

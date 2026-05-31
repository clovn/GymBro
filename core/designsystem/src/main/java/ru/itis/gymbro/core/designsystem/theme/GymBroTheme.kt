package ru.itis.gymbro.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GymBroColors.Primary,
    onPrimary = GymBroColors.Background,
    primaryContainer = GymBroColors.PrimaryLight,
    background = GymBroColors.Background,
    onBackground = GymBroColors.TextPrimary,
    surface = GymBroColors.Surface,
    onSurface = GymBroColors.TextPrimary,
    surfaceVariant = GymBroColors.SurfaceVariant,
    onSurfaceVariant = GymBroColors.TextSecondary,
    outline = GymBroColors.Divider,
    error = GymBroColors.Error
)

// Dark theme repeats light theme partially (as post-MVP per Section 7.9)
private val DarkColorScheme = lightColorScheme(
    primary = GymBroColors.Primary,
    onPrimary = GymBroColors.Background,
    primaryContainer = GymBroColors.PrimaryLight,
    background = GymBroColors.Background,
    onBackground = GymBroColors.TextPrimary,
    surface = GymBroColors.Surface,
    onSurface = GymBroColors.TextPrimary,
    surfaceVariant = GymBroColors.SurfaceVariant,
    onSurfaceVariant = GymBroColors.TextSecondary,
    outline = GymBroColors.Divider,
    error = GymBroColors.Error
)

@Composable
fun GymBroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GymBroTypography,
        content = content
    )
}

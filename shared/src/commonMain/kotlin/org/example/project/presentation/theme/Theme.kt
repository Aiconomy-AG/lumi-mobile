package org.example.project.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AppDarkColorScheme = darkColorScheme(
    primary = AppColorPalette.Primary,
    onPrimary = AppColorPalette.OnPrimary,
    background = AppColorPalette.Background,
    onBackground = AppColorPalette.TextPrimary,
    surface = AppColorPalette.Surface,
    onSurface = AppColorPalette.TextPrimary,
    surfaceVariant = AppColorPalette.SurfaceVariant,
    onSurfaceVariant = AppColorPalette.TextSecondary,
    outline = AppColorPalette.Border,
    outlineVariant = AppColorPalette.Border,
    error = AppColorPalette.Error,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        shapes = AppShapes,
        content = content,
    )
}

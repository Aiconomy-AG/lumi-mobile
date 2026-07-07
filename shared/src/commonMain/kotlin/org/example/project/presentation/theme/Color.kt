package org.example.project.presentation.theme

import androidx.compose.ui.graphics.Color

data class StatusColor(
    val background: Color,
    val content: Color,
)

object AppColorPalette {
    val Background = Color(0xFF0B0B0B)
    val Surface = Color(0xFF121212)
    val SurfaceVariant = Color(0xFF1A1A1A)
    val Border = Color(0xFF2A2A2A)

    val Primary = Color(0xFFFFB31A)
    val OnPrimary = Color(0xFF000000)

    val TextPrimary = Color(0xFFF5F5F5)
    val TextSecondary = Color(0xFF9A9A9A)

    val Error = Color(0xFFFF5C5C)

    val StatusTodo = StatusColor(Color(0xFF3A3A3A), Color(0xFFE0E0E0))
    val StatusInProgress = StatusColor(Color(0xFF4A2E00), Color(0xFFFFA726))
    val StatusDone = StatusColor(Color(0xFF0F3D2E), Color(0xFF4CAF50))

    val AvatarPalette = listOf(
        Color(0xFFCB6E17),
        Color(0xFF2FA6A2),
        Color(0xFFE0A100),
        Color(0xFF6E7BCB),
        Color(0xFFB25CCB),
    )
}

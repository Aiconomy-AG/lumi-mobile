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
    val FieldBackground = Color(0xFF111111)
    val Border = Color(0xFF2A2A2A)
    val BorderStrong = Color(0xFF303030)
    val SubtleBorder = Color(0x1FE6E6E6)
    val OverlayBackground = Color(0xF20B0B0B)
    val OverlaySurface = Color(0xF20F0F0F)
    val SelectionOverlay = Color(0x1AE6E6E6)

    val Primary = Color(0xFFF5B11B)
    val OnPrimary = Background

    val TextPrimary = Color(0xFFFFFFFF)
    val TextPrimarySoft = Color(0xFFEAEAEA)
    val IconPrimary = Color(0xFFE0E0E0)
    val IconPrimaryTranslucent = Color(0xBFE0E0E0)
    val IconSecondaryTranslucent = Color(0x8FC6C6C6)
    val TextSecondary = Color(0xFF808080)

    val Error = Color(0xFFFF5C5C)
    val LogoutDanger = Color(0xFFFF1F3D)
    val Success = Color(0xFF00D084)

    val AdminAvatarBackground = Color(0xFF4A2408)
    val EmployeeAvatarBackground = Color(0xFF123B34)

    val StatusToDo = StatusColor(Color(0xFF3A3A3A), Color(0xFFE0E0E0))
    val StatusInProgress = StatusColor(Color(0xFF4A2E00), Color(0xFFFFA726))
    val StatusComplete = StatusColor(Color(0xFF0F3D2E), Color(0xFF4CAF50))
    val StatusBlocked = StatusColor(Color(0xFF3D0F0F), Color(0xFFFF5C5C))

    val AvatarPalette = listOf(
        Color(0xFFCB6E17),
        Color(0xFF2FA6A2),
        Color(0xFFE0A100),
        Color(0xFF6E7BCB),
        Color(0xFFB25CCB),
    )
}

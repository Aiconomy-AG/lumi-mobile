package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun CallCircleButton(
    background: Color,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp = 56.dp,
    iconSize: Dp = 26.dp,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .background(background, CircleShape),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColorPalette.TextPrimary,
            modifier = Modifier.size(iconSize),
        )
    }
}

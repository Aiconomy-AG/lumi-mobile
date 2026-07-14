package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.presentation.theme.AppColorPalette

@Composable
actual fun CallVideoRenderer(isLocal: Boolean, modifier: Modifier) {
    Box(modifier = modifier.background(AppColorPalette.SurfaceVariant))
}

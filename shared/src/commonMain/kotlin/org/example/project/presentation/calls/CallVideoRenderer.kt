package org.example.project.presentation.calls

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CallVideoRenderer(
    isLocal: Boolean,
    modifier: Modifier = Modifier,
    participantName: String = "",
    cameraEnabled: Boolean = true,
    participantIdentity: String? = null,
)

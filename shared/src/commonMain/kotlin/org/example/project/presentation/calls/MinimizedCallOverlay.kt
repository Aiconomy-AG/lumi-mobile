package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.example.project.presentation.theme.AppColorPalette
import kotlin.math.roundToInt

@Composable
fun MinimizedCallOverlay(
    state: CallUiState,
    currentUserId: Int,
    selfName: String,
    onExpand: () -> Unit,
) {
    val call = state.call ?: return
    val tiles = buildActiveCallTiles(
        call = call,
        mediaParticipants = state.mediaParticipants,
        currentUserId = currentUserId,
        selfName = selfName,
        localCameraEnabled = state.cameraEnabled,
    )
    val previewTile = tiles.firstOrNull { !it.isLocal && it.cameraEnabled }
        ?: tiles.firstOrNull { it.isLocal }
        ?: return

    val density = LocalDensity.current
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(width = 120.dp, height = 180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppColorPalette.Surface)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
        ) {
            CallParticipantTile(
                tile = previewTile,
                showVideo = call.isVideo,
                modifier = Modifier.fillMaxSize(),
            )
            IconButton(
                onClick = onExpand,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(AppColorPalette.Background.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInFull,
                    contentDescription = "Expand call",
                    tint = AppColorPalette.TextPrimary,
                )
            }
        }
    }
}

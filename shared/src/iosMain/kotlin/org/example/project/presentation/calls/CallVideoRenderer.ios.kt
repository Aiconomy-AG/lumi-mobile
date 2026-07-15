package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import org.example.project.domain.calls.IosLiveKitRoomHolder
import org.example.project.presentation.theme.AppColorPalette
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CallVideoRenderer(
    isLocal: Boolean,
    modifier: Modifier,
    participantName: String,
    cameraEnabled: Boolean,
) {
    val localCameraEnabled by IosLiveKitRoomHolder.localCameraEnabled.collectAsState()
    val remoteCameraEnabled by IosLiveKitRoomHolder.remoteCameraEnabled.collectAsState()
    val remoteName by IosLiveKitRoomHolder.remoteParticipantName.collectAsState()
    val localHasVideoTrack by IosLiveKitRoomHolder.localHasVideoTrack.collectAsState()
    val remoteHasVideoTrack by IosLiveKitRoomHolder.remoteHasVideoTrack.collectAsState()

    val holderCameraEnabled = if (isLocal) localCameraEnabled else remoteCameraEnabled
    val hasTrack = if (isLocal) localHasVideoTrack else remoteHasVideoTrack
    val displayName = if (isLocal) participantName else remoteName.ifBlank { participantName }
    val showVideo = cameraEnabled && holderCameraEnabled && hasTrack

    if (showVideo) {
        UIKitView(
            modifier = modifier,
            factory = {
                val container = UIView()
                NSNotificationCenter.defaultCenter.postNotificationName(
                    "LumiVideoViewCreated",
                    container,
                    mapOf("isLocal" to isLocal),
                )
                container
            },
            onRelease = { view ->
                NSNotificationCenter.defaultCenter.postNotificationName(
                    "LumiVideoViewReleased",
                    view,
                    mapOf("isLocal" to isLocal),
                )
            },
        )
    } else {
        CallVideoPlaceholder(name = displayName, modifier = modifier)
    }
}

@Composable
private fun CallVideoPlaceholder(name: String, modifier: Modifier) {
    Box(
        modifier = modifier.background(AppColorPalette.SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(AppColorPalette.Primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(2).uppercase().ifBlank { "?" },
                color = AppColorPalette.OnPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

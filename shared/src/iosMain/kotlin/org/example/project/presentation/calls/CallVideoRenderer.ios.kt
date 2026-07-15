package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
    participantIdentity: String?,
) {
    val mediaParticipants by IosLiveKitRoomHolder.mediaParticipants.collectAsState()
    val localCameraEnabled by IosLiveKitRoomHolder.localCameraEnabled.collectAsState()
    val remoteCameraEnabled by IosLiveKitRoomHolder.remoteCameraEnabled.collectAsState()
    val remoteName by IosLiveKitRoomHolder.remoteParticipantName.collectAsState()
    val localHasVideoTrack by IosLiveKitRoomHolder.localHasVideoTrack.collectAsState()
    val remoteHasVideoTrack by IosLiveKitRoomHolder.remoteHasVideoTrack.collectAsState()

    val mediaParticipant = participantIdentity?.let { identity ->
        mediaParticipants.firstOrNull { it.identity == identity }
    } ?: mediaParticipants.firstOrNull { it.isLocal == isLocal }

    val holderCameraEnabled = mediaParticipant?.cameraEnabled
        ?: if (isLocal) localCameraEnabled else remoteCameraEnabled
    val hasTrack = mediaParticipant?.hasVideoTrack
        ?: if (isLocal) localHasVideoTrack else remoteHasVideoTrack
    val displayName = mediaParticipant?.name?.ifBlank { participantName } ?: participantName
        .ifBlank { if (isLocal) participantName else remoteName.ifBlank { participantName } }
    val identity = mediaParticipant?.identity
        ?: participantIdentity
        ?: if (isLocal) "local" else remoteName.ifBlank { "remote" }
    val showVideo = cameraEnabled && holderCameraEnabled && hasTrack

    if (showVideo) {
        UIKitView(
            modifier = modifier,
            factory = {
                val container = UIView()
                NSNotificationCenter.defaultCenter.postNotificationName(
                    "LumiVideoViewCreated",
                    container,
                    mapOf("identity" to identity),
                )
                container
            },
            onRelease = { view ->
                NSNotificationCenter.defaultCenter.postNotificationName(
                    "LumiVideoViewReleased",
                    view,
                    mapOf("identity" to identity),
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

package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.VideoTrack
import org.example.project.domain.calls.AndroidLiveKitRoomHolder
import org.example.project.presentation.theme.AppColorPalette

@Composable
actual fun CallVideoRenderer(
    isLocal: Boolean,
    modifier: Modifier,
    participantName: String,
    cameraEnabled: Boolean,
) {
    val room by AndroidLiveKitRoomHolder.room.collectAsState()
    val localCameraEnabled by AndroidLiveKitRoomHolder.localCameraEnabled.collectAsState()
    val remoteCameraEnabled by AndroidLiveKitRoomHolder.remoteCameraEnabled.collectAsState()
    val remoteName by AndroidLiveKitRoomHolder.remoteParticipantName.collectAsState()
    val localTrack by AndroidLiveKitRoomHolder.localVideoTrack.collectAsState()
    val remoteTrack by AndroidLiveKitRoomHolder.remoteVideoTrack.collectAsState()

    val holderCameraEnabled = if (isLocal) localCameraEnabled else remoteCameraEnabled
    val track = if (isLocal) localTrack else remoteTrack
    val displayName = if (isLocal) participantName else remoteName.ifBlank { participantName }
    val showVideo = cameraEnabled && holderCameraEnabled && room != null && track != null

    if (showVideo) {
        VideoTrackView(room = room!!, track = track!!, modifier = modifier, mirror = isLocal)
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

@Composable
private fun VideoTrackView(room: Room, track: VideoTrack, modifier: Modifier, mirror: Boolean) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                room.initVideoRenderer(this)
                setMirror(mirror)
                track.addRenderer(this)
            }
        },
        onRelease = { view ->
            val surface = view as SurfaceViewRenderer
            track.removeRenderer(surface)
            surface.release()
        },
    )
    DisposableEffect(track) {
        onDispose { }
    }
}

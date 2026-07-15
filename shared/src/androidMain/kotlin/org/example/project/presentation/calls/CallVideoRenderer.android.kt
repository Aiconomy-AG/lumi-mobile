package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
    participantIdentity: String?,
) {
    val room by AndroidLiveKitRoomHolder.room.collectAsState()
    val mediaParticipants by AndroidLiveKitRoomHolder.mediaParticipants.collectAsState()
    val localCameraEnabled by AndroidLiveKitRoomHolder.localCameraEnabled.collectAsState()
    val remoteCameraEnabled by AndroidLiveKitRoomHolder.remoteCameraEnabled.collectAsState()
    val remoteName by AndroidLiveKitRoomHolder.remoteParticipantName.collectAsState()
    val localTrack by AndroidLiveKitRoomHolder.localVideoTrack.collectAsState()
    val remoteTrack by AndroidLiveKitRoomHolder.remoteVideoTrack.collectAsState()

    val mediaParticipant = participantIdentity?.let { identity ->
        mediaParticipants.firstOrNull { it.identity == identity }
    } ?: mediaParticipants.firstOrNull { it.isLocal == isLocal }

    val holderCameraEnabled = mediaParticipant?.cameraEnabled
        ?: if (isLocal) localCameraEnabled else remoteCameraEnabled
    val displayName = mediaParticipant?.name?.ifBlank { participantName } ?: participantName
        .ifBlank { if (isLocal) participantName else remoteName.ifBlank { participantName } }
    val track = resolveVideoTrack(
        room = room,
        mediaParticipant = mediaParticipant,
        isLocal = isLocal,
        localTrack = localTrack,
        remoteTrack = remoteTrack,
    )
    val showVideo = cameraEnabled && holderCameraEnabled && room != null && track != null

    if (showVideo) {
        VideoTrackView(room = room!!, track = track!!, modifier = modifier, mirror = isLocal)
    } else {
        CallVideoPlaceholder(name = displayName, modifier = modifier)
    }
}

private fun resolveVideoTrack(
    room: Room?,
    mediaParticipant: org.example.project.domain.calls.CallMediaParticipant?,
    isLocal: Boolean,
    localTrack: VideoTrack?,
    remoteTrack: VideoTrack?,
): VideoTrack? {
    if (room == null) return null
    val identity = mediaParticipant?.identity
    if (!identity.isNullOrBlank()) {
        val participant = if (mediaParticipant?.isLocal == true) {
            room.localParticipant
        } else {
            room.remoteParticipants.values.firstOrNull {
                it.identity?.value == identity || it.name == identity
            }
        } ?: return if (isLocal) localTrack else remoteTrack
        val publication = participant.getTrackPublication(io.livekit.android.room.track.Track.Source.CAMERA)
        return publication?.track as? VideoTrack
    }
    return if (isLocal) localTrack else remoteTrack
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
    key(track) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                SurfaceViewRenderer(context).apply {
                    room.initVideoRenderer(this)
                    setMirror(mirror)
                    track.addRenderer(this)
                }
            },
            onRelease = { surface ->
                track.removeRenderer(surface)
                surface.release()
            },
        )
    }
}

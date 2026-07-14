package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.VideoTrack
import org.example.project.domain.calls.AndroidLiveKitRoomHolder
import org.example.project.presentation.theme.AppColorPalette

@Composable
actual fun CallVideoRenderer(isLocal: Boolean, modifier: Modifier) {
    val room by AndroidLiveKitRoomHolder.room.collectAsState()
    val localTrack by AndroidLiveKitRoomHolder.localVideoTrack.collectAsState()
    val remoteTracks by AndroidLiveKitRoomHolder.remoteVideoTracks.collectAsState()
    val track = if (isLocal) localTrack else remoteTracks.firstOrNull()

    if (room != null && track != null) {
        VideoTrackView(room = room!!, track = track, modifier = modifier, mirror = isLocal)
    } else {
        Box(modifier = modifier.background(AppColorPalette.SurfaceVariant))
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

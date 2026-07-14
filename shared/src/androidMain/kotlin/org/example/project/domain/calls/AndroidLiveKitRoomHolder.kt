package org.example.project.domain.calls

import io.livekit.android.room.Room
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object AndroidLiveKitRoomHolder {
    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()
    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()
    private val _remoteVideoTracks = MutableStateFlow<List<VideoTrack>>(emptyList())
    val remoteVideoTracks: StateFlow<List<VideoTrack>> = _remoteVideoTracks.asStateFlow()
    private val _remoteParticipantCount = MutableStateFlow(0)
    val remoteParticipantCount: StateFlow<Int> = _remoteParticipantCount.asStateFlow()

    private var eventsJob: Job? = null

    fun attach(room: Room, scope: CoroutineScope) {
        detachEvents()
        _room.value = room
        refresh(room)
        eventsJob = scope.launch {
            while (isActive && _room.value === room) {
                refresh(room)
                delay(500)
            }
        }
    }

    fun refresh(room: Room) {
        _room.value = room
        _remoteParticipantCount.value = room.remoteParticipants.size
        _localVideoTrack.value = room.localParticipant
            .getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack
        _remoteVideoTracks.value = room.remoteParticipants.values.flatMap { participant ->
            participant.trackPublications.values.mapNotNull { publication ->
                publication.track as? VideoTrack
            }
        }
    }

    fun clear() {
        detachEvents()
        _room.value = null
        _localVideoTrack.value = null
        _remoteVideoTracks.value = emptyList()
        _remoteParticipantCount.value = 0
    }

    private fun detachEvents() {
        eventsJob?.cancel()
        eventsJob = null
    }
}

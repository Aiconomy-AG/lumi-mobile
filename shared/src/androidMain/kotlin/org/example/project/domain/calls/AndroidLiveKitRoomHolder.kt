package org.example.project.domain.calls

import io.livekit.android.room.Room
import io.livekit.android.room.track.LocalTrackPublication
import io.livekit.android.room.track.RemoteTrackPublication
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
    private val _localCameraEnabled = MutableStateFlow(false)
    val localCameraEnabled: StateFlow<Boolean> = _localCameraEnabled.asStateFlow()
    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()
    private val _remoteCameraEnabled = MutableStateFlow(false)
    val remoteCameraEnabled: StateFlow<Boolean> = _remoteCameraEnabled.asStateFlow()
    private val _remoteParticipantName = MutableStateFlow("")
    val remoteParticipantName: StateFlow<String> = _remoteParticipantName.asStateFlow()
    private val _remoteParticipantCount = MutableStateFlow(0)
    val remoteParticipantCount: StateFlow<Int> = _remoteParticipantCount.asStateFlow()
    private val _mediaParticipants = MutableStateFlow<List<CallMediaParticipant>>(emptyList())
    val mediaParticipants: StateFlow<List<CallMediaParticipant>> = _mediaParticipants.asStateFlow()

    private var eventsJob: Job? = null

    fun attach(room: Room, scope: CoroutineScope) {
        detachEvents()
        _room.value = room
        refresh(room)
        eventsJob = scope.launch {
            while (isActive && _room.value === room) {
                refresh(room)
                delay(400)
            }
        }
    }

    fun refresh(room: Room) {
        _room.value = room
        _remoteParticipantCount.value = room.remoteParticipants.size

        val localPublication = room.localParticipant.getTrackPublication(Track.Source.CAMERA)
        val localMicPublication = room.localParticipant.getTrackPublication(Track.Source.MICROPHONE)
        val localActive = isLocalCameraActive(localPublication)
        _localCameraEnabled.value = localActive
        _localVideoTrack.value = if (localActive) localPublication?.track as? VideoTrack else null

        val localIdentity = room.localParticipant.identity?.value.orEmpty()
        val localName = room.localParticipant.name.orEmpty().ifBlank { localIdentity }
        val participants = mutableListOf<CallMediaParticipant>()
        participants += CallMediaParticipant(
            identity = localIdentity.ifBlank { "local" },
            name = localName.ifBlank { "You" },
            isLocal = true,
            cameraEnabled = localActive,
            isMuted = localMicPublication?.muted == true,
            hasVideoTrack = localPublication?.track != null,
        )

        room.remoteParticipants.values.forEach { remoteParticipant ->
            val identity = remoteParticipant.identity?.value.orEmpty()
            val name = remoteParticipant.name.orEmpty().ifBlank { identity }
            val remotePublication = remoteParticipant.getTrackPublication(Track.Source.CAMERA) as? RemoteTrackPublication
            val micPublication = remoteParticipant.getTrackPublication(Track.Source.MICROPHONE) as? RemoteTrackPublication
            val remoteActive = isRemoteCameraActive(remotePublication)
            participants += CallMediaParticipant(
                identity = identity.ifBlank { name },
                name = name,
                isLocal = false,
                cameraEnabled = remoteActive,
                isMuted = micPublication?.muted == true,
                hasVideoTrack = remotePublication?.track != null,
            )
        }
        _mediaParticipants.value = participants

        val remoteParticipant = room.remoteParticipants.values.firstOrNull()
        _remoteParticipantName.value = remoteParticipant?.name.orEmpty()
            .ifBlank { remoteParticipant?.identity?.value.orEmpty() }
        val remotePublication = remoteParticipant?.getTrackPublication(Track.Source.CAMERA) as? RemoteTrackPublication
        val remoteActive = isRemoteCameraActive(remotePublication)
        _remoteCameraEnabled.value = remoteActive
        _remoteVideoTrack.value = if (remoteActive) remotePublication?.track as? VideoTrack else null
    }

    fun clear() {
        detachEvents()
        _room.value = null
        _localVideoTrack.value = null
        _localCameraEnabled.value = false
        _remoteVideoTrack.value = null
        _remoteCameraEnabled.value = false
        _remoteParticipantName.value = ""
        _remoteParticipantCount.value = 0
        _mediaParticipants.value = emptyList()
    }

    private fun detachEvents() {
        eventsJob?.cancel()
        eventsJob = null
    }

    private fun isLocalCameraActive(publication: LocalTrackPublication?): Boolean {
        if (publication == null) return false
        return publication.track != null && !publication.muted
    }

    private fun isRemoteCameraActive(publication: RemoteTrackPublication?): Boolean {
        if (publication == null) return false
        return publication.subscribed && publication.track != null && !publication.muted
    }
}

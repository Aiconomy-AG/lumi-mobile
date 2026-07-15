package org.example.project.domain.calls

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object IosLiveKitRoomHolder {
    private val _localCameraEnabled = MutableStateFlow(false)
    val localCameraEnabled: StateFlow<Boolean> = _localCameraEnabled.asStateFlow()

    private val _remoteCameraEnabled = MutableStateFlow(false)
    val remoteCameraEnabled: StateFlow<Boolean> = _remoteCameraEnabled.asStateFlow()

    private val _remoteParticipantCount = MutableStateFlow(0)
    val remoteParticipantCount: StateFlow<Int> = _remoteParticipantCount.asStateFlow()

    private val _remoteParticipantName = MutableStateFlow("")
    val remoteParticipantName: StateFlow<String> = _remoteParticipantName.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _localHasVideoTrack = MutableStateFlow(false)
    val localHasVideoTrack: StateFlow<Boolean> = _localHasVideoTrack.asStateFlow()

    private val _remoteHasVideoTrack = MutableStateFlow(false)
    val remoteHasVideoTrack: StateFlow<Boolean> = _remoteHasVideoTrack.asStateFlow()

    private val _mediaParticipants = MutableStateFlow<List<CallMediaParticipant>>(emptyList())
    val mediaParticipants: StateFlow<List<CallMediaParticipant>> = _mediaParticipants.asStateFlow()

    fun apply(
        remoteCount: Int,
        remoteCameraEnabled: Boolean,
        localCameraEnabled: Boolean,
        isMuted: Boolean,
        localHasVideoTrack: Boolean,
        remoteHasVideoTrack: Boolean,
        remoteParticipantName: String,
        mediaParticipants: List<CallMediaParticipant> = emptyList(),
    ) {
        _remoteParticipantCount.value = remoteCount
        _remoteCameraEnabled.value = remoteCameraEnabled
        _localCameraEnabled.value = localCameraEnabled
        _isMuted.value = isMuted
        _localHasVideoTrack.value = localHasVideoTrack
        _remoteHasVideoTrack.value = remoteHasVideoTrack
        _remoteParticipantName.value = remoteParticipantName
        _mediaParticipants.value = mediaParticipants
    }

    fun clear() {
        apply(
            remoteCount = 0,
            remoteCameraEnabled = false,
            localCameraEnabled = false,
            isMuted = false,
            localHasVideoTrack = false,
            remoteHasVideoTrack = false,
            remoteParticipantName = "",
            mediaParticipants = emptyList(),
        )
    }
}

fun updateIosRoomState(
    remoteCount: Int,
    remoteCameraEnabled: Boolean,
    localCameraEnabled: Boolean,
    isMuted: Boolean,
    localHasVideoTrack: Boolean,
    remoteHasVideoTrack: Boolean,
    remoteParticipantName: String,
    identities: List<String> = emptyList(),
    names: List<String> = emptyList(),
    isLocalFlags: List<Boolean> = emptyList(),
    cameraEnabledFlags: List<Boolean> = emptyList(),
    mutedFlags: List<Boolean> = emptyList(),
    hasVideoTrackFlags: List<Boolean> = emptyList(),
) {
    val mediaParticipants = identities.mapIndexed { index, identity ->
        CallMediaParticipant(
            identity = identity,
            name = names.getOrNull(index).orEmpty().ifBlank { identity },
            isLocal = isLocalFlags.getOrNull(index) == true,
            cameraEnabled = cameraEnabledFlags.getOrNull(index) == true,
            isMuted = mutedFlags.getOrNull(index) == true,
            hasVideoTrack = hasVideoTrackFlags.getOrNull(index) == true,
        )
    }
    IosLiveKitRoomHolder.apply(
        remoteCount = remoteCount,
        remoteCameraEnabled = remoteCameraEnabled,
        localCameraEnabled = localCameraEnabled,
        isMuted = isMuted,
        localHasVideoTrack = localHasVideoTrack,
        remoteHasVideoTrack = remoteHasVideoTrack,
        remoteParticipantName = remoteParticipantName,
        mediaParticipants = mediaParticipants,
    )
}

fun clearIosRoomState() {
    IosLiveKitRoomHolder.clear()
}

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

    fun apply(
        remoteCount: Int,
        remoteCameraEnabled: Boolean,
        localCameraEnabled: Boolean,
        isMuted: Boolean,
        localHasVideoTrack: Boolean,
        remoteHasVideoTrack: Boolean,
        remoteParticipantName: String,
    ) {
        _remoteParticipantCount.value = remoteCount
        _remoteCameraEnabled.value = remoteCameraEnabled
        _localCameraEnabled.value = localCameraEnabled
        _isMuted.value = isMuted
        _localHasVideoTrack.value = localHasVideoTrack
        _remoteHasVideoTrack.value = remoteHasVideoTrack
        _remoteParticipantName.value = remoteParticipantName
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
) {
    IosLiveKitRoomHolder.apply(
        remoteCount = remoteCount,
        remoteCameraEnabled = remoteCameraEnabled,
        localCameraEnabled = localCameraEnabled,
        isMuted = isMuted,
        localHasVideoTrack = localHasVideoTrack,
        remoteHasVideoTrack = remoteHasVideoTrack,
        remoteParticipantName = remoteParticipantName,
    )
}

fun clearIosRoomState() {
    IosLiveKitRoomHolder.clear()
}

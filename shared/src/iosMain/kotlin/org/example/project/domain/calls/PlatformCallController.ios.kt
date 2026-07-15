package org.example.project.domain.calls

import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSNotificationCenter

private class IosLiveKitCallController : PlatformCallController {
    override val remoteParticipantCount: StateFlow<Int> =
        IosLiveKitRoomHolder.remoteParticipantCount
    override val remoteCameraEnabled: StateFlow<Boolean> =
        IosLiveKitRoomHolder.remoteCameraEnabled

    override suspend fun connect(call: WorkspaceCall) {
        val connection = call.connection ?: return
        NSNotificationCenter.defaultCenter.postNotificationName(
            "LumiCallConnect",
            null,
            mapOf(
                "url" to connection.url,
                "token" to connection.token,
                "video" to call.isVideo,
            ),
        )
    }

    override suspend fun disconnect() {
        NSNotificationCenter.defaultCenter.postNotificationName("LumiCallDisconnect", null)
        IosLiveKitRoomHolder.clear()
    }

    override suspend fun setMuted(muted: Boolean) {
        NSNotificationCenter.defaultCenter.postNotificationName("LumiCallMute", null, mapOf("muted" to muted))
    }

    override suspend fun setCameraEnabled(enabled: Boolean) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            "LumiCallCamera",
            null,
            mapOf("enabled" to enabled),
        )
    }

    override fun isMuted(): Boolean = IosLiveKitRoomHolder.isMuted.value

    override fun isCameraEnabled(): Boolean = IosLiveKitRoomHolder.localCameraEnabled.value

    override fun showIncoming(call: WorkspaceCall) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            "LumiCallIncoming",
            null,
            mapOf(
                "callId" to call.id,
                "callerName" to call.caller.name,
                "callerUserId" to call.caller.id.toString(),
                "video" to call.isVideo,
            ),
        )
    }

    override fun dismissIncoming(callId: String) {
        NSNotificationCenter.defaultCenter.postNotificationName("LumiCallDismiss", null, mapOf("callId" to callId))
    }
}

actual fun createPlatformCallController(): PlatformCallController = IosLiveKitCallController()

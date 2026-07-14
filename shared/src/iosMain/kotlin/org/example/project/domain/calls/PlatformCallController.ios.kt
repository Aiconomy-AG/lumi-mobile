package org.example.project.domain.calls

import platform.Foundation.NSNotificationCenter

private class IosLiveKitCallController : PlatformCallController {
    override suspend fun connect(call: WorkspaceCall) {
        val connection = call.connection ?: return
        NSNotificationCenter.defaultCenter.postNotificationName(
            "LumiCallConnect",
            null,
            mapOf("url" to connection.url, "token" to connection.token),
        )
    }

    override suspend fun disconnect() {
        NSNotificationCenter.defaultCenter.postNotificationName("LumiCallDisconnect", null)
    }

    override suspend fun setMuted(muted: Boolean) {
        NSNotificationCenter.defaultCenter.postNotificationName("LumiCallMute", null, mapOf("muted" to muted))
    }

    override fun showIncoming(call: WorkspaceCall) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            "LumiCallIncoming",
            null,
            mapOf("callId" to call.id, "callerName" to call.caller.name, "callerUserId" to call.caller.id.toString()),
        )
    }

    override fun dismissIncoming(callId: String) {
        NSNotificationCenter.defaultCenter.postNotificationName("LumiCallDismiss", null, mapOf("callId" to callId))
    }
}

actual fun createPlatformCallController(): PlatformCallController = IosLiveKitCallController()

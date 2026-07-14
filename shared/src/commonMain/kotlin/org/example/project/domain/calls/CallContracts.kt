package org.example.project.domain.calls

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CallApi {
    suspend fun start(
        calleeIds: List<Int>,
        clientInstanceId: String,
        type: String = "audio",
        mode: String? = null,
    ): WorkspaceCall

    suspend fun startFromConversation(
        conversationId: Int,
        clientInstanceId: String,
        type: String = "audio",
    ): WorkspaceCall
    suspend fun get(callId: String): WorkspaceCall
    suspend fun active(clientInstanceId: String): WorkspaceCall?
    suspend fun history(page: Int = 1, perPage: Int = 20): CallHistoryPage
    suspend fun accept(callId: String, clientInstanceId: String): WorkspaceCall
    suspend fun decline(callId: String): WorkspaceCall
    suspend fun cancel(callId: String): WorkspaceCall
    suspend fun leave(callId: String): WorkspaceCall
    suspend fun invite(callId: String, userIds: List<Int>): WorkspaceCall
    suspend fun end(callId: String): WorkspaceCall
}

interface CallRealtimeApi {
    fun events(userId: Int): Flow<WorkspaceCall>
}

interface CallPresenceRealtimeApi {
    fun presenceEvents(callId: String): Flow<CallPresenceEvent>
}

interface PlatformCallController {
    suspend fun connect(call: WorkspaceCall)
    suspend fun disconnect()
    suspend fun setMuted(muted: Boolean)
    suspend fun setCameraEnabled(enabled: Boolean)
    fun isMuted(): Boolean
    fun isCameraEnabled(): Boolean
    val remoteCameraEnabled: StateFlow<Boolean>
    val remoteParticipantCount: StateFlow<Int>
    fun showIncoming(call: WorkspaceCall)
    fun dismissIncoming(callId: String)
}

expect fun createPlatformCallController(): PlatformCallController

class CallApiException(message: String, val code: String? = null) : Exception(message)

package org.example.project.domain.calls

import kotlinx.coroutines.flow.Flow

interface CallApi {
    suspend fun start(conversationId: Int, clientInstanceId: String): WorkspaceCall
    suspend fun active(clientInstanceId: String): WorkspaceCall?
    suspend fun get(callId: String): WorkspaceCall
    suspend fun accept(callId: String, clientInstanceId: String): WorkspaceCall
    suspend fun decline(callId: String): WorkspaceCall
    suspend fun cancel(callId: String): WorkspaceCall
    suspend fun end(callId: String): WorkspaceCall
}

interface CallRealtimeApi {
    fun events(userId: Int): Flow<WorkspaceCall>
}

interface PlatformCallController {
    suspend fun connect(call: WorkspaceCall)
    suspend fun disconnect()
    suspend fun setMuted(muted: Boolean)
    fun showIncoming(call: WorkspaceCall)
    fun dismissIncoming(callId: String)
}

expect fun createPlatformCallController(): PlatformCallController

class CallApiException(message: String, val code: String? = null) : Exception(message)


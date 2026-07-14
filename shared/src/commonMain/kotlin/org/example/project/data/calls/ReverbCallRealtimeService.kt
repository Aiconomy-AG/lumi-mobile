package org.example.project.data.calls

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.example.project.data.realtime.ReverbPrivateChannelClient
import org.example.project.data.realtime.decodeRealtime
import org.example.project.domain.calls.CallPresenceEvent
import org.example.project.domain.calls.CallPresenceRealtimeApi
import org.example.project.domain.calls.CallRealtimeApi
import org.example.project.domain.calls.WorkspaceCall

private val CALL_EVENTS = setOf(
    "call.incoming",
    "call.ringing",
    "call.accepted",
    "call.declined",
    "call.cancelled",
    "call.ended",
    "call.updated",
)

class ReverbCallRealtimeService(
    private val realtime: ReverbPrivateChannelClient,
) : CallRealtimeApi {
    override fun events(userId: Int): Flow<WorkspaceCall> = realtime.events("users.$userId")
        .filter { it.name in CALL_EVENTS }
        .map { it.data.decodeRealtime<WorkspaceCall>() }
}

class ReverbCallPresenceRealtimeService(
    private val realtime: ReverbPrivateChannelClient,
) : CallPresenceRealtimeApi {
    override fun presenceEvents(callId: String): Flow<CallPresenceEvent> =
        realtime.events("presence-call.$callId")
            .filter { it.name == "participant.joined" || it.name == "participant.left" }
            .map { event ->
                val payload = event.data.decodeRealtime<PresencePayload>()
                CallPresenceEvent(
                    call = payload.call,
                    participantUserId = payload.participantUserId,
                )
            }
}

@kotlinx.serialization.Serializable
private data class PresencePayload(
    val call: WorkspaceCall,
    @kotlinx.serialization.SerialName("participant_user_id")
    val participantUserId: Int,
)

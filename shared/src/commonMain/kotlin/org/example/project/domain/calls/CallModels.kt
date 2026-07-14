package org.example.project.domain.calls

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CallStatus {
    @SerialName("ringing") RINGING,
    @SerialName("active") ACTIVE,
    @SerialName("declined") DECLINED,
    @SerialName("cancelled") CANCELLED,
    @SerialName("missed") MISSED,
    @SerialName("ended") ENDED,
    @SerialName("failed") FAILED;

    val isTerminal: Boolean
        get() = this != RINGING && this != ACTIVE
}

@Serializable
data class CallIdentity(
    val id: Int,
    val name: String,
)

@Serializable
data class CallParticipant(
    @SerialName("user_id") val userId: Int,
    val name: String = "",
    val role: String,
    val status: String,
)

@Serializable
data class CallConnection(val url: String, val token: String)

@Serializable
data class WorkspaceCall(
    val id: String,
    @SerialName("conversation_id") val conversationId: Int? = null,
    @SerialName("initiated_by_user_id") val initiatedByUserId: Int,
    @SerialName("destination_type") val destinationType: String = "workspace_user",
    @SerialName("room_name") val roomName: String = "",
    val caller: CallIdentity,
    val participants: List<CallParticipant>,
    val mode: String = "1v1",
    val type: String = "audio",
    @SerialName("media_type") val mediaType: String = "audio",
    val status: CallStatus,
    @SerialName("answered_client_instance_id") val answeredClientInstanceId: String? = null,
    @SerialName("end_reason") val endReason: String? = null,
    @SerialName("answered_at") val answeredAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String? = null,
    val connection: CallConnection? = null,
) {
    val isGroup: Boolean get() = mode == "group"
    val isVideo: Boolean get() = type == "video" || mediaType == "video"
}

@Serializable
data class CallHistoryPage(
    val data: List<WorkspaceCall> = emptyList(),
    val meta: CallHistoryMeta = CallHistoryMeta(),
)

@Serializable
data class CallHistoryMeta(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    val total: Int = 0,
)

@Serializable
data class CallPresenceEvent(
    val call: WorkspaceCall,
    @SerialName("participant_user_id") val participantUserId: Int,
)

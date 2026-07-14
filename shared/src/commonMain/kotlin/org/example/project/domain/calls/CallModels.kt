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
    @SerialName("conversation_id") val conversationId: Int,
    @SerialName("initiated_by_user_id") val initiatedByUserId: Int,
    @SerialName("destination_type") val destinationType: String = "workspace_user",
    val caller: CallIdentity,
    val participants: List<CallParticipant>,
    @SerialName("media_type") val mediaType: String = "audio",
    val status: CallStatus,
    @SerialName("answered_client_instance_id") val answeredClientInstanceId: String? = null,
    @SerialName("end_reason") val endReason: String? = null,
    @SerialName("answered_at") val answeredAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    val connection: CallConnection? = null,
)

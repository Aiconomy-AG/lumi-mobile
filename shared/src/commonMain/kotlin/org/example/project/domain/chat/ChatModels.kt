package org.example.project.domain.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
enum class ConversationType {
    @SerialName("direct")
    DIRECT,

    @SerialName("group")
    GROUP,
}

@Serializable
enum class ChatMessageType {
    @SerialName("text")
    TEXT,

    @SerialName("call")
    CALL,

    @SerialName("ai_action")
    AI_ACTION
}

@Serializable
data class Conversation (
    val id: Int,
    val type: ConversationType,
    val name: String? = null,
    val createdBy: Int
)

@Serializable
data class AiActionMeta(
    @SerialName("action_id") val actionId: Int,
    @SerialName("tool_name") val toolName: String,
    val summary: String,
    val status: String,
    @SerialName("requested_by_user_id") val requestedByUserId: Int? = null,
    @SerialName("requested_by_name") val requestedByName: String? = null,
    val arguments: Map<String, JsonElement>? = null,
    val result: JsonElement? = null,
    val error: String? = null,
    @SerialName("expires_at") val expiresAt: String,
)
@Serializable
data class ConversationParticipant(
    val conversationId: Int,
    val employeeId: Int,
    val joinedAt: String,
)

@Serializable
data class ChatCallMetadata(
    val id: String,
    val status: String,
    val type: String,
    val mode: String = "1v1",
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("initiated_by_user_id") val initiatedByUserId: Int,
    @SerialName("caller_name") val callerName: String = "",
    @SerialName("answered_at") val answeredAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null,
)

@Serializable
data class ChatMessage(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val messageText: String,
    val sentAt: String,
    val messageType: ChatMessageType = ChatMessageType.TEXT,
    val call: ChatCallMetadata? = null,
    val meta: AiActionMeta? = null,
)

data class ChatParticipant(
    val id: Int,
    val name: String,
    val email: String,
    val role: String = "",
    val status: String = "",
    val isBot: Boolean = false,
)

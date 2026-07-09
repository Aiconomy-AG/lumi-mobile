package org.example.project.domain.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ConversationType {
    @SerialName("direct")
    DIRECT,

    @SerialName("group")
    GROUP,
}

@Serializable
data class Conversation (
    val id: Int,
    val type: ConversationType,
    val name: String? = null,
    val createdBy: Int
)

@Serializable
data class ConversationParticipant(
    val conversationId: Int,
    val employeeId: Int,
    val joinedAt: String,
)

@Serializable
data class ChatMessage(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val messageText: String,
    val sentAt: String,
)

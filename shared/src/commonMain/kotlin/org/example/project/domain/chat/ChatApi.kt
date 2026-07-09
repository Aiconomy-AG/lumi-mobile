package org.example.project.domain.chat

import kotlinx.coroutines.flow.Flow


interface ChatApi {
    suspend fun getConversations(employeeId: Int): List<Conversation>
    suspend fun getParticipants(conversationId: Int): List<ConversationParticipant>
    suspend fun getMessages(conversationId: Int): List<ChatMessage>
    suspend fun createDirectConversation(participantEmployeeId: Int): Conversation
    suspend fun sendMessage(conversationId: Int, senderId: Int, messageText: String): ChatMessage
}

interface ChatRealtimeApi {
    fun notificationEvents(userId: Int): Flow<ChatNotificationEvent>
}

data class ChatNotificationEvent(
    val id: Int,
    val type: String,
    val conversationId: Int?,
    val messageId: Int?,
    val actorUserId: Int?,
)

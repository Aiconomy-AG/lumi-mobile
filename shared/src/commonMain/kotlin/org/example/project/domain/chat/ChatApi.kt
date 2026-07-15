package org.example.project.domain.chat

import kotlinx.coroutines.flow.Flow


data class ChatMessagePreview(
    val message: String,
    val senderId: Int,
    val sentAt: String,
)

data class ConversationSummary(
    val conversation: Conversation,
    val participants: List<ChatParticipant>,
    val lastMessage: ChatMessagePreview? = null,
    val lastMessageAt: String? = null,
)

data class ConversationDetail(
    val conversation: Conversation,
    val participants: List<ChatParticipant>,
)

interface ChatApi {
    suspend fun getConversations(employeeId: Int): List<ConversationSummary>
    suspend fun getConversation(conversationId: Int): ConversationDetail
    suspend fun getParticipants(conversationId: Int): List<ConversationParticipant>
    suspend fun getMessages(conversationId: Int): List<ChatMessage>
    suspend fun createDirectConversation(participantEmployeeId: Int): Conversation
    suspend fun createGroupConversation(name: String, participantEmployeeIds: List<Int>): Conversation
    suspend fun updateGroupConversation(
        conversationId: Int,
        name: String? = null,
        addParticipantEmployeeIds: List<Int> = emptyList(),
        removeParticipantEmployeeIds: List<Int> = emptyList(),
    ): Conversation
    suspend fun sendMessage(conversationId: Int, senderId: Int, messageText: String): ChatMessage
    suspend fun approveAiAction(conversationId: Int, actionId: Int)
    suspend fun rejectAiAction(conversationId: Int, actionId: Int)
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

package org.example.project.domain.chat


interface ChatApi {
    suspend fun getConversations(employeeId: Int): List<Conversation>
    suspend fun getParticipants(conversationId: Int): List<ConversationParticipant>
    suspend fun getMessages(conversationId: Int): List<ChatMessage>
    suspend fun sendMessage(conversationId: Int, senderId: Int, messageText: String): ChatMessage
}
// ChatMockApiService.kt
package org.example.project.data.chat

import kotlinx.coroutines.delay
import org.example.project.domain.chat.ChatMessage
import org.example.project.domain.chat.Conversation
import org.example.project.domain.chat.ConversationParticipant
import org.example.project.domain.chat.ConversationType

class ChatMockApiService {

    private val conversations = listOf(
        Conversation(id = 1, type = ConversationType.DIRECT, createdBy = 1),
        Conversation(id = 2, type = ConversationType.DIRECT, createdBy = 3),
        Conversation(id = 3, type = ConversationType.GROUP, name = "Echipa depozit", createdBy = 1),
    )

    private val participants = listOf(
        ConversationParticipant(conversationId = 1, employeeId = 1, joinedAt = "2026-07-01 09:00"),
        ConversationParticipant(conversationId = 1, employeeId = 3, joinedAt = "2026-07-01 09:00"),

        ConversationParticipant(conversationId = 2, employeeId = 1, joinedAt = "2026-07-02 10:15"),
        ConversationParticipant(conversationId = 2, employeeId = 4, joinedAt = "2026-07-02 10:15"),

        ConversationParticipant(conversationId = 3, employeeId = 1, joinedAt = "2026-07-03 08:30"),
        ConversationParticipant(conversationId = 3, employeeId = 3, joinedAt = "2026-07-03 08:30"),
        ConversationParticipant(conversationId = 3, employeeId = 4, joinedAt = "2026-07-03 08:30"),
        ConversationParticipant(conversationId = 3, employeeId = 5, joinedAt = "2026-07-03 08:30"),
    )

    private val messages = mutableListOf(
        ChatMessage(1, 1, 3, "Buna! Ai verificat task-ul de azi?", "09:12"),
        ChatMessage(2, 1, 1, "Da, ma uit acum peste el.", "09:14"),
        ChatMessage(3, 2, 4, "Avem nevoie de confirmare pentru stoc.", "10:05"),
        ChatMessage(4, 3, 5, "Am terminat inventarul pe primul raft.", "11:20"),
        ChatMessage(5, 3, 1, "Perfect, multumesc. Continuam cu raftul doi.", "11:24"),
    )

    suspend fun getConversations(employeeId: Int): List<Conversation> {
        delay(250)
        val conversationIds = participants
            .filter { it.employeeId == employeeId }
            .map { it.conversationId }
            .toSet()

        return conversations.filter { it.id in conversationIds }
    }

    suspend fun getParticipants(conversationId: Int): List<ConversationParticipant> {
        delay(150)
        return participants.filter { it.conversationId == conversationId }
    }

    suspend fun getMessages(conversationId: Int): List<ChatMessage> {
        delay(200)
        return messages.filter { it.conversationId == conversationId }
    }

    suspend fun sendMessage(
        conversationId: Int,
        senderId: Int,
        messageText: String,
    ): ChatMessage {
        delay(150)
        val message = ChatMessage(
            id = (messages.maxOfOrNull { it.id } ?: 0) + 1,
            conversationId = conversationId,
            senderId = senderId,
            messageText = messageText,
            sentAt = currentTimeLabel(),
        )
        messages.add(message)
        return message
    }
}

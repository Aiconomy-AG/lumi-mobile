package org.example.project.data.chat

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.chat.ChatApi
import org.example.project.domain.chat.ChatMessage
import org.example.project.domain.chat.Conversation
import org.example.project.domain.chat.ConversationParticipant
import org.example.project.domain.chat.ConversationType

class ChatApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) : ChatApi {


    override suspend fun getConversations(employeeId: Int): List<Conversation> {
        val response = client.get("$baseUrl/workspace/conversations") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return chatJson.decodeFromString<ConversationListResponse>(text).data.map { it.toConversation() }
    }

    override suspend fun getParticipants(conversationId: Int): List<ConversationParticipant> {
        val response = client.get("$baseUrl/workspace/conversations/$conversationId") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return chatJson.decodeFromString<ConversationResponse>(text).data.participants.map {
            ConversationParticipant(
                conversationId = conversationId,
                employeeId = it.id,
                joinedAt = "",
            )
        }
    }

    override suspend fun getMessages(conversationId: Int): List<ChatMessage> {
        val response = client.get("$baseUrl/workspace/conversations/$conversationId/messages") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return chatJson.decodeFromString<MessageListResponse>(text).data.map { it.toChatMessage() }
    }

    override suspend fun sendMessage(conversationId: Int, senderId: Int, messageText: String): ChatMessage {
        val response = client.post("$baseUrl/workspace/conversations/$conversationId/messages") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequestBody(message = messageText))
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return chatJson.decodeFromString<MessageResponse>(text).data.toChatMessage()
    }

    private fun HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            chatJson.decodeFromString<ApiErrorResponse>(responseText).message
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class ConversationListResponse(
    val data: List<ConversationDto>,
)

@Serializable
private data class ConversationResponse(
    val data: ConversationDto,
)

@Serializable
private data class ConversationDto(
    val id: Int,
    val type: ConversationType,
    val name: String? = null,
    @SerialName("created_by")
    val createdBy: Int? = null,
    val participants: List<ParticipantDto> = emptyList(),
) {
    fun toConversation(): Conversation = Conversation(
        id = id,
        type = type,
        name = name,
        createdBy = createdBy ?: 0,
    )
}

@Serializable
private data class ParticipantDto(
    val id: Int,
    val name: String = "",
    val email: String = "",
)

@Serializable
private data class MessageListResponse(
    val data: List<MessageDto>,
)

@Serializable
private data class MessageResponse(
    val data: MessageDto,
)

@Serializable
private data class MessageDto(
    val id: Int,
    @SerialName("conversation_id")
    val conversationId: Int,
    @SerialName("sender_id")
    val senderId: Int,
    val message: String,
    @SerialName("sent_at")
    val sentAt: String? = null,
) {
    fun toChatMessage(): ChatMessage = ChatMessage(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        messageText = message,
        sentAt = sentAt ?: "",
    )
}

@Serializable
private data class SendMessageRequestBody(
    val message: String,
)

@Serializable
private data class ApiErrorResponse(
    val message: String = "Request failed.",
)

private val chatJson = Json {
    ignoreUnknownKeys = true
}

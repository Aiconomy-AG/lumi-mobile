package org.example.project.data.chat

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
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
import org.example.project.domain.chat.ChatMessagePreview
import org.example.project.domain.chat.ChatParticipant
import org.example.project.domain.chat.Conversation
import org.example.project.domain.chat.ConversationDetail
import org.example.project.domain.chat.ConversationParticipant
import org.example.project.domain.chat.ConversationSummary
import org.example.project.domain.chat.ConversationType

class ChatApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) : ChatApi {


    override suspend fun getConversations(employeeId: Int): List<ConversationSummary> {
        val response = client.get("$baseUrl/workspace/conversations") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return chatJson.decodeFromString<ConversationListResponse>(text).data.map { it.toConversationSummary() }
    }

    override suspend fun getConversation(conversationId: Int): ConversationDetail {
        val response = client.get("$baseUrl/workspace/conversations/$conversationId") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        val dto = chatJson.decodeFromString<ConversationResponse>(text).data
        return ConversationDetail(
            conversation = dto.toConversation(),
            participants = dto.participants.map { it.toChatParticipant() },
        )
    }

    override suspend fun getParticipants(conversationId: Int): List<ConversationParticipant> {
        return getConversation(conversationId).participants.map {
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

    override suspend fun createDirectConversation(participantEmployeeId: Int): Conversation {
        val response = client.post("$baseUrl/workspace/conversations") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(
                StoreConversationRequestBody(
                    type = ConversationType.DIRECT,
                    participantEmployeeIds = listOf(participantEmployeeId),
                )
            )
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return chatJson.decodeFromString<ConversationResponse>(text).data.toConversation()
    }

    override suspend fun createGroupConversation(
        name: String,
        participantEmployeeIds: List<Int>,
    ): Conversation {
        val response = client.post("$baseUrl/workspace/conversations") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(
                StoreGroupConversationRequestBody(
                    type = ConversationType.GROUP,
                    name = name,
                    participantEmployeeIds = participantEmployeeIds,
                )
            )
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return chatJson.decodeFromString<ConversationResponse>(text).data.toConversation()
    }

    override suspend fun updateGroupConversation(
        conversationId: Int,
        name: String?,
        addParticipantEmployeeIds: List<Int>,
        removeParticipantEmployeeIds: List<Int>,
    ): Conversation {
        val response = client.put("$baseUrl/workspace/conversations/$conversationId") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(
                UpdateGroupRequestBody(
                    name = name,
                    addParticipantEmployeeIds = addParticipantEmployeeIds.takeIf { it.isNotEmpty() },
                    removeParticipantEmployeeIds = removeParticipantEmployeeIds.takeIf { it.isNotEmpty() },
                )
            )
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return chatJson.decodeFromString<ConversationResponse>(text).data.toConversation()
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
    @SerialName("last_message_at")
    val lastMessageAt: String? = null,
    @SerialName("last_message")
    val lastMessage: LastMessageDto? = null,
) {
    fun toConversation(): Conversation = Conversation(
        id = id,
        type = type,
        name = name,
        createdBy = createdBy ?: 0,
    )

    fun toConversationSummary(): ConversationSummary = ConversationSummary(
        conversation = toConversation(),
        participants = participants.map { it.toChatParticipant() },
        lastMessage = lastMessage?.toChatMessagePreview(),
        lastMessageAt = lastMessageAt,
    )
}

@Serializable
private data class LastMessageDto(
    val message: String = "",
    @SerialName("sender_id")
    val senderId: Int = 0,
    @SerialName("sent_at")
    val sentAt: String = "",
) {
    fun toChatMessagePreview(): ChatMessagePreview = ChatMessagePreview(
        message = message,
        senderId = senderId,
        sentAt = sentAt,
    )
}

@Serializable
private data class ParticipantDto(
    val id: Int,
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val status: String = "",
    @SerialName("is_bot")
    val isBot: Boolean = false,
) {
    fun toChatParticipant(): ChatParticipant = ChatParticipant(
        id = id,
        name = name,
        email = email,
        role = role,
        status = status,
        isBot = isBot,
    )
}

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
private data class StoreConversationRequestBody(
    val type: ConversationType,
    @SerialName("participants_employee_ids")
    val participantEmployeeIds: List<Int>,
)

@Serializable
private data class StoreGroupConversationRequestBody(
    val type: ConversationType,
    val name: String,
    @SerialName("participants_employee_ids")
    val participantEmployeeIds: List<Int>,
)

@Serializable
private data class UpdateGroupRequestBody(
    val name: String? = null,
    @SerialName("add_participants_employee_ids")
    val addParticipantEmployeeIds: List<Int>? = null,
    @SerialName("remove_participants_employee_ids")
    val removeParticipantEmployeeIds: List<Int>? = null,
)

@Serializable
private data class ApiErrorResponse(
    val message: String = "Request failed.",
)

private val chatJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

package org.example.project.data.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.data.realtime.ReverbPrivateChannelClient
import org.example.project.data.realtime.decodeRealtime
import org.example.project.domain.chat.ChatNotificationEvent
import org.example.project.domain.chat.ChatRealtimeApi
import org.example.project.domain.chat.ChatRealtimeEvent

private val CHAT_CONVERSATION_EVENTS = setOf("message.reaction.updated")

class ReverbChatRealtimeService(
    private val realtime: ReverbPrivateChannelClient,
) : ChatRealtimeApi {
    override fun notificationEvents(userId: Int): Flow<ChatNotificationEvent> = realtime
        .events("users.$userId")
        .filter { it.name == "notification.delivered" }
        .map { event ->
            val delivery = event.data.decodeRealtime<NotificationDeliveryData>()
            ChatNotificationEvent(
                id = delivery.id,
                type = delivery.event.type,
                conversationId = delivery.event.conversationId,
                messageId = delivery.event.messageId,
                actorUserId = delivery.event.actorUserId,
            )
        }
        .flowOn(Dispatchers.Default)

    override fun conversationEvents(conversationId: Int): Flow<ChatRealtimeEvent> = realtime
        .events("conversations.$conversationId")
        .filter { it.name in CHAT_CONVERSATION_EVENTS }
        .map { event ->
            val message = event.data.decodeRealtime<MessageDto>().toChatMessage()
            ChatRealtimeEvent.ReactionUpdated(message)
        }
        .flowOn(Dispatchers.Default)
}

@Serializable
private data class NotificationDeliveryData(val id: Int, val event: NotificationEventData)

@Serializable
private data class NotificationEventData(
    val type: String,
    @SerialName("actor_user_id") val actorUserId: Int? = null,
    @SerialName("conversation_id") val conversationId: Int? = null,
    @SerialName("message_id") val messageId: Int? = null,
)

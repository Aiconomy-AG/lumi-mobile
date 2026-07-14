package org.example.project.data.chat

import io.ktor.client.HttpClient
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

class ReverbChatRealtimeService(
    client: HttpClient,
    baseUrl: String,
    appKey: String,
    host: String,
    port: Int,
    scheme: String,
    token: String,
) : ChatRealtimeApi {
    private val realtime = ReverbPrivateChannelClient(client, baseUrl, appKey, host, port, scheme, token)

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

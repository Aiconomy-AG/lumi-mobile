package org.example.project.data.chat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json
import org.example.project.domain.chat.ChatMessageType

class MessageDtoParsingTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun callMessageWithNonAiMetaDoesNotCrash() {
        val payload = """
            {
              "data": [
                {
                  "id": 1,
                  "conversation_id": 10,
                  "sender_id": 2,
                  "message": "Call",
                  "message_type": "call",
                  "sent_at": "2026-07-15T12:00:00Z",
                  "call": {
                    "id": "call-uuid",
                    "status": "active",
                    "type": "audio",
                    "initiated_by_user_id": 2
                  },
                  "meta": {
                    "preview": "Call"
                  }
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<MessageListResponse>(payload)
        val message = response.data.single().toChatMessage()

        assertEquals(ChatMessageType.CALL, message.messageType)
        assertNull(message.meta)
    }
}

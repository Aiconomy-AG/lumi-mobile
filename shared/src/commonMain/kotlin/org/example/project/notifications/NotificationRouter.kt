package org.example.project.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationDeepLink(
    val type: String,
    val taskId: Int? = null,
    val conversationId: Int? = null,
    val messageId: Int? = null,
)

object NotificationRouter {
    private val _pending = MutableStateFlow<NotificationDeepLink?>(null)
    val pending: StateFlow<NotificationDeepLink?> = _pending.asStateFlow()

    fun parse(data: Map<String, String>): NotificationDeepLink? {
        val type = data["type"] ?: return null

        return when (type) {
            "task_assigned",
            "task_unassigned",
            "task_status_changed" -> {
                val taskId = data["task_id"]?.toIntOrNull() ?: return null
                NotificationDeepLink(type = type, taskId = taskId)
            }

            "chat_message_received" -> {
                val conversationId = data["conversation_id"]?.toIntOrNull() ?: return null
                NotificationDeepLink(
                    type = type,
                    conversationId = conversationId,
                    messageId = data["message_id"]?.toIntOrNull(),
                )
            }

            else -> null
        }
    }

    fun emit(link: NotificationDeepLink) {
        _pending.value = link
    }

    fun consume() {
        _pending.value = null
    }
}

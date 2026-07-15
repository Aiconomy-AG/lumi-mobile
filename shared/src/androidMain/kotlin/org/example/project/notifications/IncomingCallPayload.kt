package org.example.project.notifications

import android.content.Intent

internal data class IncomingCallPayload(
    val callId: String,
    val callerName: String,
    val callerUserId: String,
    val callType: String,
    val callMode: String,
    val conversationId: String,
    val title: String,
    val body: String,
) {
    val isVideo: Boolean get() = callType == "video"

    fun putInto(intent: Intent) {
        intent.putExtra(KEY_TYPE, TYPE_INCOMING_CALL)
        intent.putExtra(KEY_CALL_ID, callId)
        intent.putExtra(KEY_CALLER_NAME, callerName)
        intent.putExtra(KEY_CALLER_USER_ID, callerUserId)
        intent.putExtra(KEY_CALL_TYPE, callType)
        intent.putExtra(KEY_CALL_MODE, callMode)
        intent.putExtra(KEY_CONVERSATION_ID, conversationId)
        intent.putExtra(KEY_TITLE, title)
        intent.putExtra(KEY_BODY, body)
    }

    companion object {
        const val TYPE_INCOMING_CALL = "workspace_call_incoming"
        const val KEY_TYPE = "type"
        const val KEY_CALL_ID = "call_id"
        const val KEY_CALLER_NAME = "caller_name"
        const val KEY_CALLER_USER_ID = "caller_user_id"
        const val KEY_CALL_TYPE = "call_type"
        const val KEY_CALL_MODE = "call_mode"
        const val KEY_CONVERSATION_ID = "conversation_id"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"

        fun fromData(data: Map<String, String>): IncomingCallPayload? {
            val callId = data[KEY_CALL_ID]?.takeIf { it.isNotBlank() } ?: return null
            val callerName = data[KEY_CALLER_NAME].orEmpty().ifBlank { "Incoming call" }
            val callType = data[KEY_CALL_TYPE].orEmpty().ifBlank { "audio" }
            return IncomingCallPayload(
                callId = callId,
                callerName = callerName,
                callerUserId = data[KEY_CALLER_USER_ID].orEmpty(),
                callType = callType,
                callMode = data[KEY_CALL_MODE].orEmpty().ifBlank { "1v1" },
                conversationId = data[KEY_CONVERSATION_ID].orEmpty(),
                title = data[KEY_TITLE].orEmpty().ifBlank { callerName },
                body = data[KEY_BODY].orEmpty().ifBlank {
                    if (callType == "video") "Incoming video call" else "Incoming audio call"
                },
            )
        }

        fun fromIntent(intent: Intent?): IncomingCallPayload? {
            intent ?: return null
            val callId = intent.getStringExtra(KEY_CALL_ID)?.takeIf { it.isNotBlank() } ?: return null
            val callerName = intent.getStringExtra(KEY_CALLER_NAME).orEmpty().ifBlank { "Incoming call" }
            val callType = intent.getStringExtra(KEY_CALL_TYPE).orEmpty().ifBlank { "audio" }
            return IncomingCallPayload(
                callId = callId,
                callerName = callerName,
                callerUserId = intent.getStringExtra(KEY_CALLER_USER_ID).orEmpty(),
                callType = callType,
                callMode = intent.getStringExtra(KEY_CALL_MODE).orEmpty().ifBlank { "1v1" },
                conversationId = intent.getStringExtra(KEY_CONVERSATION_ID).orEmpty(),
                title = intent.getStringExtra(KEY_TITLE).orEmpty().ifBlank { callerName },
                body = intent.getStringExtra(KEY_BODY).orEmpty().ifBlank {
                    if (callType == "video") "Incoming video call" else "Incoming audio call"
                },
            )
        }
    }
}

package org.example.project.data.chat

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual object ChatReadStateStorage {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    actual fun initialize(platformContext: Any?) {
    }

    actual fun load(userId: Int): Map<Int, Int> {
        val raw = NSUserDefaults.standardUserDefaults.stringForKey(keyFor(userId)) ?: return emptyMap()
        return try {
            json.decodeFromString<Map<Int, Int>>(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    actual fun save(userId: Int, readStateByConversationId: Map<Int, Int>) {
        NSUserDefaults.standardUserDefaults.setObject(
            json.encodeToString(readStateByConversationId),
            forKey = keyFor(userId),
        )
    }

    actual fun clear(userId: Int) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(keyFor(userId))
    }

    private fun keyFor(userId: Int): String = "lumi_chat_read_state_$userId"
}

package org.example.project.data.chat

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual object ChatReadStateStorage {
    private const val PREFS_NAME = "lumi_chat_read_state"

    private var prefs: android.content.SharedPreferences? = null

    private val json = Json {
        ignoreUnknownKeys = true
    }

    actual fun initialize(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun load(userId: Int): Map<Int, Int> {
        val raw = prefs?.getString(keyFor(userId), null) ?: return emptyMap()
        return try {
            json.decodeFromString<Map<Int, Int>>(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    actual fun save(userId: Int, readStateByConversationId: Map<Int, Int>) {
        prefs?.edit()
            ?.putString(keyFor(userId), json.encodeToString(readStateByConversationId))
            ?.apply()
    }

    actual fun clear(userId: Int) {
        prefs?.edit()?.remove(keyFor(userId))?.apply()
    }

    private fun keyFor(userId: Int): String = "read_state_$userId"
}

package org.example.project.data.auth

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual object SessionStorage {
    private const val PREFS_NAME = "lumi_auth"
    private const val KEY_SESSION = "user_session"

    private var prefs: android.content.SharedPreferences? = null

    private val json = Json {
        ignoreUnknownKeys = true
    }

    actual fun initialize(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun saveSession(session: UserSession) {
        prefs?.edit()
            ?.putString(KEY_SESSION, json.encodeToString(session))
            ?.apply()
    }

    actual fun loadSession(): UserSession? {
        val raw = prefs?.getString(KEY_SESSION, null) ?: return null
        return try {
            json.decodeFromString<UserSession>(raw)
        } catch (_: Exception) {
            null
        }
    }

    actual fun clearSession() {
        prefs?.edit()?.remove(KEY_SESSION)?.apply()
    }
}

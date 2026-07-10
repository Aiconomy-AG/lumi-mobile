package org.example.project.data.auth

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual object SessionStorage {
    private const val KEY_SESSION = "lumi_user_session"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    actual fun initialize(platformContext: Any?) {
    }

    actual fun saveSession(session: UserSession) {
        NSUserDefaults.standardUserDefaults.setObject(
            json.encodeToString(session),
            forKey = KEY_SESSION,
        )
    }

    actual fun loadSession(): UserSession? {
        val raw = NSUserDefaults.standardUserDefaults.stringForKey(KEY_SESSION) ?: return null
        return try {
            json.decodeFromString<UserSession>(raw)
        } catch (_: Exception) {
            null
        }
    }

    actual fun clearSession() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_SESSION)
    }
}

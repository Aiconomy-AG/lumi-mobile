package org.example.project.data.calls

import android.content.Context
import java.util.UUID

actual object ClientInstanceIdStorage {
    private const val PREFS_NAME = "lumi_client_instance"
    private const val KEY_ID = "installation_id"

    private var prefs: android.content.SharedPreferences? = null

    actual fun initialize(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun getOrCreate(): String {
        val storage = prefs ?: return "android-${UUID.randomUUID()}"
        val existing = storage.getString(KEY_ID, null)
        if (existing != null) return "android-$existing"
        val id = UUID.randomUUID().toString()
        storage.edit().putString(KEY_ID, id).apply()
        return "android-$id"
    }
}

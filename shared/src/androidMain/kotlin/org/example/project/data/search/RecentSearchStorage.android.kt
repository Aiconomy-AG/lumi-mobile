package org.example.project.data.search

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.domain.search.RECENT_SEARCH_LIMIT
import org.example.project.domain.search.RecentSearchEntry
import org.example.project.domain.search.RecentSearchStore

actual object RecentSearchStorage : RecentSearchStore {
    private const val PREFS_NAME = "lumi_global_search"

    private var prefs: android.content.SharedPreferences? = null

    private val json = Json {
        ignoreUnknownKeys = true
    }

    actual fun initialize(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual override fun load(userId: Int): List<RecentSearchEntry> {
        val raw = prefs?.getString(keyFor(userId), null) ?: return emptyList()
        return try {
            json.decodeFromString<List<RecentSearchEntry>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    actual override fun save(userId: Int, entry: RecentSearchEntry) {
        val merged = (listOf(entry) + load(userId).filter { it.id != entry.id })
            .take(RECENT_SEARCH_LIMIT)
        prefs?.edit()
            ?.putString(keyFor(userId), json.encodeToString(merged))
            ?.apply()
    }

    actual override fun clear(userId: Int) {
        prefs?.edit()?.remove(keyFor(userId))?.apply()
    }

    private fun keyFor(userId: Int): String = "recent_search_$userId"
}

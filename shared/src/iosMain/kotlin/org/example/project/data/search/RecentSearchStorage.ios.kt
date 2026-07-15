package org.example.project.data.search

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.domain.search.RECENT_SEARCH_LIMIT
import org.example.project.domain.search.RecentSearchEntry
import org.example.project.domain.search.RecentSearchStore
import platform.Foundation.NSUserDefaults

actual object RecentSearchStorage : RecentSearchStore {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    actual fun initialize(platformContext: Any?) {
    }

    actual override fun load(userId: Int): List<RecentSearchEntry> {
        val raw = NSUserDefaults.standardUserDefaults.stringForKey(keyFor(userId)) ?: return emptyList()
        return try {
            json.decodeFromString<List<RecentSearchEntry>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    actual override fun save(userId: Int, entry: RecentSearchEntry) {
        val merged = (listOf(entry) + load(userId).filter { it.id != entry.id })
            .take(RECENT_SEARCH_LIMIT)
        NSUserDefaults.standardUserDefaults.setObject(
            json.encodeToString(merged),
            forKey = keyFor(userId),
        )
    }

    actual override fun clear(userId: Int) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(keyFor(userId))
    }

    private fun keyFor(userId: Int): String = "lumi_recent_search_$userId"
}

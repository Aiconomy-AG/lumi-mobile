package org.example.project.domain.search

import kotlinx.serialization.Serializable

@Serializable
enum class RecentSearchKind {
    QUERY,
    RESULT,
    PAGE,
    ACTION,
}

@Serializable
data class RecentSearchEntry(
    val id: String,
    val kind: RecentSearchKind,
    val label: String,
    val resultType: SearchResultType? = null,
    val resultId: Int? = null,
    val pageId: String? = null,
    val actionId: String? = null,
    val timestamp: Long,
)

interface RecentSearchStore {
    fun load(userId: Int): List<RecentSearchEntry>
    fun save(userId: Int, entry: RecentSearchEntry)
    fun clear(userId: Int)
}

const val RECENT_SEARCH_LIMIT = 5

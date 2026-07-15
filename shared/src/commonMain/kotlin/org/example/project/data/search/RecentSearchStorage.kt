package org.example.project.data.search

import org.example.project.domain.search.RecentSearchEntry
import org.example.project.domain.search.RecentSearchStore

expect object RecentSearchStorage : RecentSearchStore {
    fun initialize(platformContext: Any? = null)
    override fun load(userId: Int): List<RecentSearchEntry>
    override fun save(userId: Int, entry: RecentSearchEntry)
    override fun clear(userId: Int)
}

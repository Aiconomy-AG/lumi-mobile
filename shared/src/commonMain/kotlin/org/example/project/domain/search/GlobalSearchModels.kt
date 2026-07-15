package org.example.project.domain.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SearchResultType(val apiValue: String) {
    @SerialName("task")
    TASK("task"),

    @SerialName("project")
    PROJECT("project"),

    @SerialName("product")
    PRODUCT("product"),

    @SerialName("order")
    ORDER("order"),

    @SerialName("return")
    RETURN("return"),

    @SerialName("user")
    USER("user");

    companion object {
        val ordered = listOf(PRODUCT, TASK, PROJECT, ORDER, RETURN, USER)
    }
}

@Serializable
data class SearchResult(
    val type: SearchResultType,
    val module: String,
    val id: Int,
    val title: String,
    val subtitle: String? = null,
    val url: String,
)

@Serializable
data class GlobalSearchResponse(
    val query: String,
    val results: List<SearchResult>,
    val meta: SearchMeta = SearchMeta(),
)

@Serializable
data class SearchMeta(
    val total: Int = 0,
    @SerialName("per_type")
    val perType: Map<String, Int> = emptyMap(),
)

interface GlobalSearchApi {
    suspend fun search(
        query: String,
        types: List<SearchResultType> = emptyList(),
        includeCompleted: Boolean = false,
        limit: Int = 5,
    ): GlobalSearchResponse
}

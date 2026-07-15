package org.example.project.presentation.search

import org.example.project.domain.search.SearchResultType

data class ParsedSearchQuery(
    val query: String,
    val types: List<SearchResultType> = emptyList(),
    val pagesOnly: Boolean = false,
)

fun parseSearchQuery(input: String): ParsedSearchQuery {
    val trimmed = input.trim()
    val parts = trimmed.split(":", limit = 2)
    if (parts.size != 2 || parts.first().isBlank()) {
        return ParsedSearchQuery(query = trimmed)
    }

    val mapped = when (parts.first().lowercase()) {
        "task" -> SearchResultType.TASK
        "proj", "project" -> SearchResultType.PROJECT
        "prod", "product" -> SearchResultType.PRODUCT
        "ord", "order" -> SearchResultType.ORDER
        "ret", "return" -> SearchResultType.RETURN
        "user" -> SearchResultType.USER
        "page" -> null
        else -> return ParsedSearchQuery(query = trimmed)
    }

    val query = parts[1].trim()
    return if (parts.first().equals("page", ignoreCase = true)) {
        ParsedSearchQuery(query = query, pagesOnly = true)
    } else {
        ParsedSearchQuery(query = query, types = listOfNotNull(mapped))
    }
}

val searchPrefixHints = listOf("task:", "proj:", "prod:", "ord:", "ret:", "user:", "page:")

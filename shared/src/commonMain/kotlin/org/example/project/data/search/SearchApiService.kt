package org.example.project.data.search

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.search.GlobalSearchApi
import org.example.project.domain.search.GlobalSearchResponse
import org.example.project.domain.search.SearchResultType

class SearchApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) : GlobalSearchApi {

    override suspend fun search(
        query: String,
        types: List<SearchResultType>,
        includeCompleted: Boolean,
        limit: Int,
    ): GlobalSearchResponse {
        val response = client.get("$baseUrl/search") {
            bearerAuth()
            parameter("q", query)
            types.forEach { type -> parameter("types[]", type.apiValue) }
            if (includeCompleted) parameter("include_completed", 1)
            parameter("limit", limit)
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return searchJson.decodeFromString<SearchResponseEnvelope>(text).data
    }

    private fun HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            searchJson.decodeFromString<ApiErrorResponse>(responseText).message
        } catch (_: Exception) {
            "Search failed."
        }
    }
}

@Serializable
private data class SearchResponseEnvelope(
    val data: GlobalSearchResponse,
)

@Serializable
private data class ApiErrorResponse(
    val message: String = "Search failed.",
)

private val searchJson = Json {
    ignoreUnknownKeys = true
}

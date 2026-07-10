package org.example.project.data.auditlogs

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.example.project.domain.auditlogs.AuditLog
import org.example.project.domain.auditlogs.AuditLogApi
import org.example.project.domain.auditlogs.AuditLogChanges
import org.example.project.domain.auditlogs.AuditLogPage

class AuditLogApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String
) : AuditLogApi {

    override suspend fun getAuditLogs(
        page: Int,
        perPage: Int,
        module: String?,
        action: String?,
        entityType: String?,
        entityId: Int?,
        actorUserId: Int?,
        from: String?,
        to: String?
    ): Result<AuditLogPage> {
        return try {
            val response = client.get("$baseUrl/admin/audit-logs") {
                bearerAuth()
                parameter("page", page)
                parameter("per_page", perPage)
                if (!module.isNullOrBlank()) parameter("module", module)
                if (!action.isNullOrBlank()) parameter("action", action)
                if (!entityType.isNullOrBlank()) parameter("entity_type", entityType)
                if (entityId != null) parameter("entity_id", entityId)
                if (actorUserId != null) parameter("actor_user_id", actorUserId)
                if (!from.isNullOrBlank()) parameter("from", from)
                if (!to.isNullOrBlank()) parameter("to", to)
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = auditLogJson.decodeFromString<AuditLogListResponse>(responseText)

            val result = AuditLogPage(
                logs = body.data.map { it.toAuditLog() },
                currentPage = body.meta.currentPage,
                lastPage = body.meta.lastPage,
                total = body.meta.total,
                perPage = body.meta.perPage
            )

            Result.success(result)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not load audit logs."))
        }
    }

    private fun HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            auditLogJson.decodeFromString<ApiErrorResponse>(responseText).message ?: "Request failed."
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class AuditLogListResponse(
    val data: List<AuditLogDto>,
    val meta: PaginationMeta
)

@Serializable
private data class PaginationMeta(
    @SerialName("current_page")
    val currentPage: Int,

    @SerialName("last_page")
    val lastPage: Int,

    val total: Int? = null,

    @SerialName("per_page")
    val perPage: Int? = null
)

@Serializable
private data class AuditLogDto(
    val id: Int,
    val module: String,
    val action: String,

    @SerialName("entity_type")
    val entityType: String,

    @SerialName("entity_id")
    val entityId: Int,

    @SerialName("entity_label")
    val entityLabel: String? = null,

    @SerialName("actor_user_id")
    val actorUserId: Int? = null,

    @SerialName("actor_name")
    val actorName: String,

    val description: String? = null,
    val changes: AuditLogChangesDto? = null,

    @SerialName("occurred_at")
    val occurredAt: String
)

@Serializable
private data class AuditLogChangesDto(
    val old: Map<String, JsonElement>? = null,
    val new: Map<String, JsonElement>? = null
)

@Serializable
private data class ApiErrorResponse(
    val message: String? = null
)

private fun AuditLogDto.toAuditLog(): AuditLog {
    return AuditLog(
        id = id,
        module = module,
        action = action,
        entityType = entityType,
        entityId = entityId,
        entityLabel = entityLabel,
        actorUserId = actorUserId,
        actorName = actorName,
        description = description,
        changes = changes?.let { AuditLogChanges(old = it.old, new = it.new) },
        occurredAt = occurredAt
    )
}

private val auditLogJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

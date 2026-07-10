package org.example.project.data.tasktimeentry

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryApi
import kotlin.time.Instant

class TaskTimeEntryApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) : TaskTimeEntryApi {

    override suspend fun getTimeEntries(taskId: Int): List<TaskTimeEntry> {
        val response = client.get("$baseUrl/workspace/tasks/$taskId/time-entries") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return timeEntryJson.decodeFromString<TimeEntryListResponse>(text).data.map { it.toTimeEntry() }
    }

    override suspend fun getActiveTimer(): TaskTimeEntry? {
        val response = client.get("$baseUrl/workspace/me/active-time-entry") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return timeEntryJson.decodeFromString<NullableTimeEntryResponse>(text).data?.toTimeEntry()
    }

    override suspend fun startTimer(taskId: Int): TaskTimeEntry {
        val response = client.post("$baseUrl/workspace/tasks/$taskId/time-entries/start") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return timeEntryJson.decodeFromString<TimeEntryResponse>(text).data.toTimeEntry()
    }

    override suspend fun stopTimer(taskId: Int, entryId: Int): TaskTimeEntry {
        val response = client.post("$baseUrl/workspace/tasks/$taskId/time-entries/$entryId/stop") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return timeEntryJson.decodeFromString<TimeEntryResponse>(text).data.toTimeEntry()
    }

    private fun HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            timeEntryJson.decodeFromString<ApiErrorResponse>(responseText).message
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class TimeEntryListResponse(
    val data: List<TimeEntryDto>,
)

@Serializable
private data class TimeEntryResponse(
    val data: TimeEntryDto,
)

@Serializable
private data class NullableTimeEntryResponse(
    val data: TimeEntryDto? = null,
)

@Serializable
private data class TimeEntryDto(
    val id: Int,
    @SerialName("task_id")
    val taskId: Int,
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("stopped_at")
    val stoppedAt: String? = null,
    @SerialName("duration_seconds")
    val durationSeconds: Int? = null,
) {
    fun toTimeEntry(): TaskTimeEntry = TaskTimeEntry(
        id = id,
        taskId = taskId,
        employeeId = employeeId,
        startedAt = Instant.parse(startedAt),
        stoppedAt = stoppedAt?.let { Instant.parse(it) },
        durationSeconds = durationSeconds,
    )
}

@Serializable
private data class ApiErrorResponse(
    val message: String = "Request failed.",
)

private val timeEntryJson = Json {
    ignoreUnknownKeys = true
}

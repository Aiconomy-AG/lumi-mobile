package org.example.project.data.tasktimeentry

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryApi

class TaskTimeEntryApiService(
    private val client: HttpClient,
    private val baseUrl: String,
) : TaskTimeEntryApi {

    override suspend fun getTimeEntries(taskId: Int): List<TaskTimeEntry> =
        client.get("$baseUrl/tasks/$taskId/time-entries").body()

    override suspend fun startTimer(taskId: Int): TaskTimeEntry =
        client.post("$baseUrl/tasks/$taskId/time-entries/start").body()

    override suspend fun stopTimer(taskId: Int, entryId: Int): TaskTimeEntry =
        client.post("$baseUrl/tasks/$taskId/time-entries/$entryId/stop").body()
}

package org.example.project.data.task

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi
import org.example.project.domain.task.TaskStatus

class TaskApiService(
    private val client: HttpClient,
    private val baseUrl: String,
) : TaskApi {

    override suspend fun getTasks(): List<Task> =
        client.get("$baseUrl/tasks").body()

    override suspend fun createTask(title: String, description: String, dueDate: String, status: TaskStatus, projectId: Int, assigneeIds: List<Int>): Task =
        client.post("$baseUrl/tasks") {
            contentType(ContentType.Application.Json)
            setBody(TaskRequestBody(title = title, description = description, status = status, dueDate = dueDate, projectId = projectId, assigneeIds = assigneeIds))
        }.body()

    override suspend fun updateTask(id: Int, title: String, description: String, dueDate: String, status: TaskStatus, projectId: Int): Task =
        client.put("$baseUrl/tasks/$id") {
            contentType(ContentType.Application.Json)
            setBody(TaskRequestBody(title = title, description = description, status = status, dueDate = dueDate))
        }.body()

    override suspend fun assignUser(taskId: Int, userId: Int): Task =
        client.post("$baseUrl/tasks/$taskId/assignees") {
            contentType(ContentType.Application.Json)
            setBody(AssigneeRequestBody(userId = userId))
        }.body()

    override suspend fun unassignUser(taskId: Int, userId: Int): Task =
        client.delete("$baseUrl/tasks/$taskId/assignees/$userId").body()
}

@Serializable
private data class TaskRequestBody(
    val title: String,
    val description: String,
    val status: TaskStatus,
    val dueDate: String,
    @SerialName("project_id")
    val projectId: Int? = null,
    @SerialName("assignee_ids")
    val assigneeIds: List<Int> = emptyList(),
)

@Serializable
private data class AssigneeRequestBody(
    @SerialName("user_id")
    val userId: Int,
)

package org.example.project.data.task

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi
import org.example.project.domain.task.TaskStatus

class TaskApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) : TaskApi {

    override suspend fun getTasks(): List<Task> {
        val response = client.get("$baseUrl/workspace/tasks") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return tasksJson.decodeFromString<TaskListResponse>(text).data.map { it.toTask() }
    }

    override suspend fun createTask(
        title: String,
        description: String,
        dueDate: String,
        status: TaskStatus,
        projectId: Int,
        assigneeIds: List<Int>
    ): Task {
        val response = client.post("$baseUrl/workspace/tasks") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(TaskRequestBody(title = title, description = description, status = status, dueDate = dueDate, projectId = projectId))
        }

        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))

        val createdTask = tasksJson.decodeFromString<TaskResponse>(text).data.toTask()

        if (assigneeIds.isNotEmpty()) {
            val assignResponse = client.post("$baseUrl/workspace/tasks/${createdTask.id}/assignees") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(AssigneeRequestBody(employeeIds = assigneeIds))
            }

            val assignText = assignResponse.bodyAsText()
            if (!assignResponse.status.isSuccess()) throw Exception(parseErrorMessage(assignText))

            return tasksJson.decodeFromString<TaskResponse>(assignText).data.toTask()
        }

        return createdTask
    }

    override suspend fun updateTask(id: Int, title: String, description: String, dueDate: String, status: TaskStatus, projectId: Int): Task {
        val response = client.put("$baseUrl/workspace/tasks/$id") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(TaskRequestBody(title = title, description = description, status = status, dueDate = dueDate, projectId = projectId))
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return tasksJson.decodeFromString<TaskResponse>(text).data.toTask()
    }

    override suspend fun deleteTask(id: Int) {
        val response = client.delete("$baseUrl/workspace/tasks/$id") { bearerAuth() }
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(response.bodyAsText()))
    }

    override suspend fun assignUser(taskId: Int, userId: Int): Task {
        val response = client.post("$baseUrl/workspace/tasks/$taskId/assignees") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(AssigneeRequestBody(employeeIds = listOf(userId)))
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return tasksJson.decodeFromString<TaskResponse>(text).data.toTask()
    }

    override suspend fun unassignUser(taskId: Int, userId: Int): Task {
        val response = client.delete("$baseUrl/workspace/tasks/$taskId/assignees/$userId") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return tasksJson.decodeFromString<TaskResponse>(text).data.toTask()
    }

    private fun HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            tasksJson.decodeFromString<ApiErrorResponse>(responseText).message
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class TaskListResponse(
    val data: List<TaskDto>,
)

@Serializable
private data class TaskResponse(
    val data: TaskDto,
)

@Serializable
private data class TaskDto(
    val id: Int,
    val title: String,
    val description: String? = null,
    val status: TaskStatus,
    @SerialName("created_by")
    val createdBy: Int? = null,
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("project_id")
    val projectId: Int? = null,
    val assignees: List<AssigneeDto> = emptyList(),
) {
    fun toTask(): Task = Task(
        id = id,
        title = title,
        description = description ?: "",
        status = status,
        createdBy = createdBy ?: 0,
        dueDate = dueDate ?: "",
        projectId = projectId ?: 0,
        assigneeIds = assignees.map { it.id },
    )
}

@Serializable
private data class AssigneeDto(
    val id: Int,
    val name: String = "",
    val email: String = "",
)

@Serializable
private data class TaskRequestBody(
    val title: String,
    val description: String,
    val status: TaskStatus,
    @SerialName("due_date")
    val dueDate: String,
    @SerialName("project_id")
    val projectId: Int? = null,
)

@Serializable
private data class AssigneeRequestBody(
    @SerialName("employee_ids")
    val employeeIds: List<Int>,
)

@Serializable
private data class ApiErrorResponse(
    val message: String = "Request failed.",
)

private val tasksJson = Json {
    ignoreUnknownKeys = true
}

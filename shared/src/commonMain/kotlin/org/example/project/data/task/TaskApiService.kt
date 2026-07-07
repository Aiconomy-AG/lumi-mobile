package org.example.project.data.task

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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

    override suspend fun createTask(title: String, description: String, dueDate: String, status: TaskStatus): Task =
        client.post("$baseUrl/tasks") {
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest(title = title, description = description, status = status, dueDate = dueDate))
        }.body()
}

@Serializable
private data class CreateTaskRequest(
    val title: String,
    val description: String,
    val status: TaskStatus,
    val dueDate: String,
)

package org.example.project.data.task

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi

class TaskApiService(
    private val client: HttpClient,
    private val baseUrl: String,
) : TaskApi {

    override suspend fun getTasks(): List<Task> =
        client.get("$baseUrl/tasks").body()
}

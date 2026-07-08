package org.example.project.data.project

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.example.project.domain.project.Project
import org.example.project.domain.project.ProjectApi
import org.example.project.domain.project.ProjectStatus

class ProjectApiService(
    private val client: HttpClient,
    private val baseUrl: String,
) : ProjectApi {

    override suspend fun getProjects(): List<Project> =
        client.get("$baseUrl/workspace/projects").body()

    override suspend fun getProject(id: Int): Project =
        client.get("$baseUrl/workspace/projects/$id").body()

    override suspend fun createProject(name: String, description: String, deadline: String, status: ProjectStatus): Project =
        client.post("$baseUrl/workspace/projects") {
            contentType(ContentType.Application.Json)
            setBody(ProjectRequestBody(name = name, description = description, deadline = deadline, status = status))
        }.body()

    override suspend fun updateProject(id: Int, name: String, description: String, deadline: String, status: ProjectStatus): Project =
        client.put("$baseUrl/workspace/projects/$id") {
            contentType(ContentType.Application.Json)
            setBody(ProjectRequestBody(name = name, description = description, deadline = deadline, status = status))
        }.body()

    override suspend fun deleteProject(id: Int) {
        client.delete("$baseUrl/workspace/projects/$id")
    }
}

@Serializable
private data class ProjectRequestBody(
    val name: String,
    val description: String,
    val deadline: String,
    val status: ProjectStatus,
)

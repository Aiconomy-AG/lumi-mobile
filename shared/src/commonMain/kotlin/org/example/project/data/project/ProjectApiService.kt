package org.example.project.data.project

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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.project.Project
import org.example.project.domain.project.ProjectApi
import org.example.project.domain.project.ProjectStatus

class ProjectApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) : ProjectApi {

    override suspend fun getProjects(): List<Project> {
        val response = client.get("$baseUrl/v1/workspace/projects") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return projectsJson.decodeFromString<ProjectListResponse>(text).data.map { it.toProject() }
    }

    override suspend fun getProject(id: Int): Project {
        val response = client.get("$baseUrl/v1/workspace/projects/$id") { bearerAuth() }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return projectsJson.decodeFromString<ProjectResponse>(text).data.toProject()
    }

    override suspend fun createProject(name: String, description: String, deadline: String, status: ProjectStatus): Project {
        val response = client.post("$baseUrl/v1/workspace/projects") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(ProjectRequestBody(name = name, description = description, deadline = deadline, status = status))
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return projectsJson.decodeFromString<ProjectResponse>(text).data.toProject()
    }

    override suspend fun updateProject(id: Int, name: String, description: String, deadline: String, status: ProjectStatus): Project {
        val response = client.put("$baseUrl/v1/workspace/projects/$id") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(ProjectRequestBody(name = name, description = description, deadline = deadline, status = status))
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(text))
        return projectsJson.decodeFromString<ProjectResponse>(text).data.toProject()
    }

    override suspend fun deleteProject(id: Int) {
        val response = client.delete("$baseUrl/v1/workspace/projects/$id") { bearerAuth() }
        if (!response.status.isSuccess()) throw Exception(parseErrorMessage(response.bodyAsText()))
    }

    private fun HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            projectsJson.decodeFromString<ApiErrorResponse>(responseText).message
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class ProjectListResponse(
    val data: List<ProjectDto>,
)

@Serializable
private data class ProjectResponse(
    val data: ProjectDto,
)

@Serializable
private data class ProjectDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val deadline: String? = null,
    val status: ProjectStatus,
) {
    fun toProject(): Project = Project(
        id = id,
        name = name,
        description = description ?: "",
        deadline = deadline ?: "",
        status = status,
    )
}

@Serializable
private data class ProjectRequestBody(
    val name: String,
    val description: String,
    val deadline: String,
    val status: ProjectStatus,
)

@Serializable
private data class ApiErrorResponse(
    val message: String = "Request failed.",
)

private val projectsJson = Json {
    ignoreUnknownKeys = true
}

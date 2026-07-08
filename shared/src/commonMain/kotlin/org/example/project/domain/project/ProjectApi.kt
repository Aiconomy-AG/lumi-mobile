package org.example.project.domain.project

interface ProjectApi {
    suspend fun getProjects(): List<Project>
    suspend fun getProject(id: Int): Project
    suspend fun createProject(name: String, description: String, deadline: String, status: ProjectStatus): Project
    suspend fun updateProject(id: Int, name: String, description: String, deadline: String, status: ProjectStatus): Project
    suspend fun deleteProject(id: Int)
}

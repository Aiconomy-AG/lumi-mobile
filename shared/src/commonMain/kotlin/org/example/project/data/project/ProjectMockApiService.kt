package org.example.project.data.project

import kotlinx.coroutines.delay
import org.example.project.domain.project.Project
import org.example.project.domain.project.ProjectApi
import org.example.project.domain.project.ProjectStatus

class ProjectMockApiService : ProjectApi {

    private val projects = mutableListOf(
        Project(id = 1, name = "Aplicație mobilă Lumi", description = "KMP + Compose Multiplatform pentru iOS și Android", deadline = "2026-09-01", status = ProjectStatus.IN_PROGRESS),
        Project(id = 2, name = "Refactorizare backend", description = "Migrare API Laravel la structura nouă", deadline = "2026-07-30", status = ProjectStatus.IN_PROGRESS),
        Project(id = 3, name = "Portal clienți", description = "Dashboard web pentru clienți", deadline = "2026-10-15", status = ProjectStatus.TO_DO),
        Project(id = 4, name = "Integrare plăți", description = "Integrare procesator de plăți end-to-end", deadline = "2026-08-20", status = ProjectStatus.BLOCKED),
        Project(id = 5, name = "Website de prezentare", description = "Landing page și blog", deadline = "2026-06-10", status = ProjectStatus.COMPLETE),
    )

    override suspend fun getProjects(): List<Project> {
        delay(500)
        return projects.toList()
    }

    override suspend fun getProject(id: Int): Project {
        delay(300)
        return projects.first { it.id == id }
    }

    override suspend fun createProject(name: String, description: String, deadline: String, status: ProjectStatus): Project {
        delay(300)
        val newProject = Project(
            id = (projects.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            description = description,
            deadline = deadline,
            status = status,
        )
        projects.add(newProject)
        return newProject
    }

    override suspend fun updateProject(id: Int, name: String, description: String, deadline: String, status: ProjectStatus): Project {
        delay(300)
        val index = projects.indexOfFirst { it.id == id }
        require(index >= 0) { "Project $id not found" }
        val updated = projects[index].copy(
            name = name,
            description = description,
            deadline = deadline,
            status = status,
        )
        projects[index] = updated
        return updated
    }

    override suspend fun deleteProject(id: Int) {
        delay(300)
        projects.removeAll { it.id == id }
    }
}

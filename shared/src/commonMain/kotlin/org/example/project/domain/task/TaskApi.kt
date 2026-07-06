package org.example.project.domain.task

interface TaskApi {
    suspend fun getTasks(): List<Task>
}

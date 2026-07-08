package org.example.project.domain.task

interface TaskApi {
    suspend fun getTasks(): List<Task>
    suspend fun createTask(title: String, description: String, dueDate: String, status: TaskStatus): Task
    suspend fun updateTask(id: Int, title: String, description: String, dueDate: String, status: TaskStatus): Task
}

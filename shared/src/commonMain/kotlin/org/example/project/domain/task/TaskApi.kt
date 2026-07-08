package org.example.project.domain.task

interface TaskApi {
    suspend fun getTasks(): List<Task>
    suspend fun createTask(title: String, description: String, dueDate: String, status: TaskStatus, projectId: Int = 0, assigneeIds: List<Int> = emptyList()): Task
    suspend fun updateTask(id: Int, title: String, description: String, dueDate: String, status: TaskStatus, projectId: Int): Task

    suspend fun assignUser(taskId: Int, userId: Int): Task

    suspend fun unassignUser(taskId: Int, userId: Int): Task
}

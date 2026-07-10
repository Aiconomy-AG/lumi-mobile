package org.example.project.data.task

import kotlinx.coroutines.delay
import kotlin.time.Clock
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi
import org.example.project.domain.task.TaskStatus

class TaskMockApiService : TaskApi {

    private val today = Clock.System.now().toString().take(10)

    private val tasks = mutableListOf(
        Task(id = 1, title = "Configurare Ktor", description = "Adaugă clientul HTTP în proiect", status = TaskStatus.COMPLETE, createdBy = 1, dueDate = "2026-07-01", projectId = 1, assigneeIds = listOf(1, 3)),
        Task(id = 2, title = "Ecran listă produse", description = "Construiește UI-ul pentru catalogul de produse", status = TaskStatus.IN_PROGRESS, createdBy = 2, dueDate = today, projectId = 1, assigneeIds = listOf(2, 4)),
        Task(id = 3, title = "Autentificare", description = "Implementează login și înregistrare", status = TaskStatus.TO_DO, createdBy = 1, dueDate = today, projectId = 2, assigneeIds = listOf(1)),
        Task(id = 4, title = "Coș de cumpărături", description = "Adaugă/elimină produse din coș", status = TaskStatus.BLOCKED, createdBy = 2, dueDate = today, projectId = 1, assigneeIds = emptyList()),
        Task(id = 5, title = "Notificări push", description = "Integrare notificări pentru comenzi", status = TaskStatus.TO_DO, createdBy = 5, dueDate = "2026-07-22", projectId = 3, assigneeIds = listOf(5, 6)),
        Task(id = 6, title = "Testare plăți", description = "Testează fluxul de plată end-to-end", status = TaskStatus.IN_PROGRESS, createdBy = 1, dueDate = "2026-07-25", projectId = 2, assigneeIds = listOf(3)),
        Task(id = 7, title = "Scrie release notes", description = "Subtask pentru notificări", status = TaskStatus.TO_DO, createdBy = 5, dueDate = today, projectId = 3, parentId = 5, assigneeIds = listOf(5)),
    )

    override suspend fun getTasks(): List<Task> {
        delay(500)
        return tasks.toList()
    }

    override suspend fun getTask(taskId: Int): Task {
        delay(200)
        val task = tasks.firstOrNull { it.id == taskId } ?: throw Exception("Task $taskId not found")
        val subtasks = tasks.filter { it.parentId == taskId }
        return task.copy(subtasks = subtasks)
    }

    override suspend fun createTask(
        title: String,
        description: String,
        dueDate: String,
        status: TaskStatus,
        projectId: Int,
        assigneeIds: List<Int>,
        parentId: Int?,
    ): Task {
        delay(300)
        val newTask = Task(
            id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            description = description,
            status = status,
            createdBy = 1,
            dueDate = dueDate,
            projectId = projectId,
            parentId = parentId,
            assigneeIds = assigneeIds.distinct(),
        )
        tasks.add(newTask)
        return newTask
    }

    override suspend fun updateTask(id: Int, title: String, description: String, dueDate: String, status: TaskStatus, projectId: Int): Task {
        delay(300)
        val index = tasks.indexOfFirst { it.id == id }
        require(index >= 0) { "Task $id not found" }
        val updated = tasks[index].copy(
            title = title,
            description = description,
            dueDate = dueDate,
            status = status,
            projectId = projectId,
        )
        tasks[index] = updated
        return updated
    }

    override suspend fun deleteTask(id: Int) {
        delay(300)
        tasks.removeAll { it.id == id }
    }

    override suspend fun assignUser(taskId: Int, userId: Int): Task {
        delay(200)
        val index = tasks.indexOfFirst { it.id == taskId }
        require(index >= 0) { "Task $taskId not found" }
        val current = tasks[index]
        val updated = current.copy(assigneeIds = (current.assigneeIds + userId).distinct())
        tasks[index] = updated
        return updated
    }

    override suspend fun unassignUser(taskId: Int, userId: Int): Task {
        delay(200)
        val index = tasks.indexOfFirst { it.id == taskId }
        require(index >= 0) { "Task $taskId not found" }
        val current = tasks[index]
        val updated = current.copy(assigneeIds = current.assigneeIds.filterNot { it == userId })
        tasks[index] = updated
        return updated
    }
}

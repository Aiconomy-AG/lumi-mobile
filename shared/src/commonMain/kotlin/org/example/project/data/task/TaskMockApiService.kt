package org.example.project.data.task

import kotlinx.coroutines.delay
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi
import org.example.project.domain.task.TaskStatus

class TaskMockApiService : TaskApi {

    override suspend fun getTasks(): List<Task> {
        delay(500)
        return listOf(
            Task(id = 1, title = "Configurare Ktor", description = "Adaugă clientul HTTP în proiect", status = TaskStatus.DONE, createdBy = 1, dueDate = "2026-07-01"),
            Task(id = 2, title = "Ecran listă produse", description = "Construiește UI-ul pentru catalogul de produse", status = TaskStatus.IN_PROGRESS, createdBy = 2, dueDate = "2026-07-10"),
            Task(id = 3, title = "Autentificare", description = "Implementează login și înregistrare", status = TaskStatus.TODO, createdBy = 1, dueDate = "2026-07-15"),
            Task(id = 4, title = "Coș de cumpărături", description = "Adaugă/elimină produse din coș", status = TaskStatus.TODO, createdBy = 2, dueDate = "2026-07-18"),
            Task(id = 5, title = "Notificări push", description = "Integrare notificări pentru comenzi", status = TaskStatus.TODO, createdBy = 5, dueDate = "2026-07-22"),
            Task(id = 6, title = "Testare plăți", description = "Testează fluxul de plată end-to-end", status = TaskStatus.IN_PROGRESS, createdBy = 1, dueDate = "2026-07-25"),
        )
    }
}

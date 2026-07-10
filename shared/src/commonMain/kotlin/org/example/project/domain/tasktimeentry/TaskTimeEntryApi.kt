package org.example.project.domain.tasktimeentry

interface TaskTimeEntryApi {
    suspend fun getTimeEntries(taskId: Int): List<TaskTimeEntry>
    suspend fun getActiveTimer(): TaskTimeEntry?
    suspend fun startTimer(taskId: Int): TaskTimeEntry
    suspend fun stopTimer(taskId: Int, entryId: Int): TaskTimeEntry
}

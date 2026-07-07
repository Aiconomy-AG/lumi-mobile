package org.example.project.data.tasktimeentry

import kotlinx.coroutines.delay
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryApi
import kotlin.time.Clock

class TaskTimeEntryMockApiService(
    private val employeeId: Int = 1
) : TaskTimeEntryApi {

    private val entriesByTask = mutableMapOf<Int, MutableList<TaskTimeEntry>>()
    private var nextId = 1

    override suspend fun getTimeEntries(taskId: Int): List<TaskTimeEntry> {
        delay(400)
        return entriesByTask[taskId].orEmpty()
    }

    override suspend fun startTimer(taskId: Int): TaskTimeEntry {
        delay(300)
        val entry = TaskTimeEntry(
            id = nextId++,
            taskId = taskId,
            employeeId = employeeId,
            startedAt = Clock.System.now(),
        )
        entriesByTask.getOrPut(taskId) { mutableListOf() }.add(entry)
        return entry
    }

    override suspend fun stopTimer(taskId: Int, entryId: Int): TaskTimeEntry {
        delay(300)
        val list = entriesByTask[taskId] ?: error("No time entries for task $taskId")
        val index = list.indexOfFirst { it.id == entryId }
        require(index >= 0) { "Time entry $entryId not found for task $taskId" }

        val entry = list[index]
        val stoppedAt = Clock.System.now()
        val durationSeconds = (stoppedAt - entry.startedAt).inWholeSeconds.toInt()
        val updated = entry.copy(stoppedAt = stoppedAt, durationSeconds = durationSeconds)
        list[index] = updated
        return updated
    }
}

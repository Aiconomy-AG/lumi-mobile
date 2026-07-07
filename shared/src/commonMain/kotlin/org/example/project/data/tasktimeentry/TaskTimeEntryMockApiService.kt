package org.example.project.data.tasktimeentry

import kotlinx.coroutines.delay
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryApi
import kotlin.time.TimeSource

class TaskTimeEntryMockApiService : TaskTimeEntryApi {

    private val entriesByTask = mutableMapOf<Int, MutableList<TaskTimeEntry>>()
    private val startMarks = mutableMapOf<Int, TimeSource.Monotonic.ValueTimeMark>()
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
            employeeId = 1,
            startedAt = "mock-start",
        )
        entriesByTask.getOrPut(taskId) { mutableListOf() }.add(entry)
        startMarks[entry.id] = TimeSource.Monotonic.markNow()
        return entry
    }

    override suspend fun stopTimer(taskId: Int, entryId: Int): TaskTimeEntry {
        delay(300)
        val list = entriesByTask[taskId] ?: error("No time entries for task $taskId")
        val index = list.indexOfFirst { it.id == entryId }
        require(index >= 0) { "Time entry $entryId not found for task $taskId" }

        val elapsedSeconds = startMarks[entryId]?.elapsedNow()?.inWholeSeconds?.toInt() ?: 0
        val updated = list[index].copy(stoppedAt = "mock-stop", durationSeconds = elapsedSeconds)
        list[index] = updated
        return updated
    }
}

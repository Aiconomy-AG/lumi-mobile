package org.example.project.domain.tasktimeentry

import kotlinx.coroutines.flow.Flow

sealed interface TimeEntryRealtimeEvent {
    data class Started(val entry: TaskTimeEntry) : TimeEntryRealtimeEvent
    data class Stopped(val entryId: Int, val taskId: Int) : TimeEntryRealtimeEvent
}

interface TaskTimeEntryRealtimeApi {
    fun timeEntryEvents(userId: Int): Flow<TimeEntryRealtimeEvent>
}

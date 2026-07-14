package org.example.project.data.tasktimeentry

import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.data.realtime.ReverbPrivateChannelClient
import org.example.project.data.realtime.decodeRealtime
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryRealtimeApi
import org.example.project.domain.tasktimeentry.TimeEntryRealtimeEvent
import kotlin.time.Instant

class ReverbTaskTimeEntryRealtimeService(
    client: HttpClient,
    baseUrl: String,
    appKey: String,
    host: String,
    port: Int,
    scheme: String,
    token: String,
) : TaskTimeEntryRealtimeApi {
    private val realtime = ReverbPrivateChannelClient(client, baseUrl, appKey, host, port, scheme, token)

    override fun timeEntryEvents(userId: Int): Flow<TimeEntryRealtimeEvent> = realtime
        .events("users.$userId")
        .filter { it.name == "time-entry.started" || it.name == "time-entry.stopped" }
        .map { event ->
            val payload = event.data.decodeRealtime<TimeEntryPayload>()
            if (event.name == "time-entry.started") {
                TimeEntryRealtimeEvent.Started(payload.toTimeEntry())
            } else {
                TimeEntryRealtimeEvent.Stopped(entryId = payload.id, taskId = payload.taskId)
            }
        }
        .flowOn(Dispatchers.Default)
}

@Serializable
private data class TimeEntryPayload(
    val id: Int,
    @SerialName("task_id") val taskId: Int,
    @SerialName("employee_id") val employeeId: Int,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("stopped_at") val stoppedAt: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
) {
    fun toTimeEntry(): TaskTimeEntry = TaskTimeEntry(
        id = id,
        taskId = taskId,
        employeeId = employeeId,
        startedAt = Instant.parse(requireNotNull(startedAt)),
        stoppedAt = stoppedAt?.let(Instant::parse),
        durationSeconds = durationSeconds,
    )
}

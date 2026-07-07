package org.example.project.domain.tasktimeentry

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TaskTimeEntry(
    val id: Int,
    val taskId: Int,
    val employeeId: Int,
    val startedAt: Instant,
    val stoppedAt: Instant? = null,
    val durationSeconds: Int? = null,
)

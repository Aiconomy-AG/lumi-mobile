package org.example.project.domain.tasktimeentry

import kotlinx.serialization.Serializable

@Serializable
data class TaskTimeEntry(
    val id: Int,
    val taskId: Int,
    val employeeId: Int,
    val startedAt: String,
    val stoppedAt: String? = null,
    val durationSeconds: Int? = null,
)

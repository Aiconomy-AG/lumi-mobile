package org.example.project.domain.task

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    TODO, IN_PROGRESS, DONE
}

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val createdBy: Int,
    val dueDate: String,
)

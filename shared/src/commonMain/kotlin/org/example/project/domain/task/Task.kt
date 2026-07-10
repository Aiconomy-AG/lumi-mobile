package org.example.project.domain.task

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    @SerialName("to_do")
    TO_DO,

    @SerialName("in_progress")
    IN_PROGRESS,

    @SerialName("complete")
    COMPLETE,

    @SerialName("blocked")
    BLOCKED,
}

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val createdBy: Int,
    val dueDate: String,
    @SerialName("project_id")
    val projectId: Int = 0,
    @SerialName("parent_id")
    val parentId: Int? = null,
    @SerialName("assignee_ids")
    val assigneeIds: List<Int> = emptyList(),
    val subtasks: List<Task> = emptyList(),
) {
    val isRootTask: Boolean get() = parentId == null
    val isSubtask: Boolean get() = parentId != null
}

fun Task.canCreateSubtask(): Boolean = isRootTask

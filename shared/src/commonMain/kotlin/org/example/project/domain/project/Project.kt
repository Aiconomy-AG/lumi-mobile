package org.example.project.domain.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectStatus {
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
data class Project(
    val id: Int,
    val name: String,
    val description: String,
    val deadline: String,
    val status: ProjectStatus,
)

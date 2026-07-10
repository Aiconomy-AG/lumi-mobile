package features.main

import org.example.project.domain.project.Project
import org.example.project.domain.task.Task

sealed class MainSubRoute {
    data object AddTask : MainSubRoute()

    data class TaskDetail(val task: Task) : MainSubRoute()

    data object EditTask : MainSubRoute()

    data object AddProject : MainSubRoute()

    data class ProjectDetail(val project: Project) : MainSubRoute()

    data object AddProduct : MainSubRoute()

    data object AddUser : MainSubRoute()
}

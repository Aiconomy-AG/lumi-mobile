package org.example.project.presentation.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.project.Project
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi

data class ProjectDetailUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val error: String? = null,
)

class ProjectDetailViewModel(
    private val project: Project,
    private val taskApi: TaskApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectDetailUiState(isLoading = true))
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val projectTasks = taskApi.getTasks().filter { it.projectId == project.id }
                _uiState.value = _uiState.value.copy(isLoading = false, tasks = projectTasks)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}

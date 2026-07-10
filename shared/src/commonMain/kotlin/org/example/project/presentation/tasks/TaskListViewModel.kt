package org.example.project.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.accounts.User
import org.example.project.data.accounts.UserApiService
import org.example.project.domain.project.Project
import org.example.project.domain.project.ProjectApi
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi
import org.example.project.domain.task.TaskStatus

data class TaskListUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val users: List<User> = emptyList(),
    val projects: List<Project> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: TaskStatus? = null,
    val onlyMine: Boolean = false,
    val currentUserId: Int = 0,
    val error: String? = null,
) {
    val filteredTasks: List<Task>
        get() = tasks.filter { task ->
            task.parentId == null &&
                (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true)) &&
                (statusFilter == null || task.status == statusFilter) &&
                (!onlyMine || currentUserId in task.assigneeIds)
        }
}

class TaskListViewModel(
    private val userApi: UserApiService,
    private val api: TaskApi,
    private val projectApi: ProjectApi,
    private val currentUserId: Int = 0,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState(currentUserId = currentUserId))
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
        loadUsers()
        loadProjects()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val tasks = api.getTasks()
                _uiState.value = _uiState.value.copy(isLoading = false, tasks = tasks)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val users = userApi.getUsers().getOrThrow()
                _uiState.value = _uiState.value.copy(users = users)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            try {
                val projects = projectApi.getProjects()
                _uiState.value = _uiState.value.copy(projects = projects)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onStatusFilterChanged(status: TaskStatus?) {
        _uiState.value = _uiState.value.copy(statusFilter = status)
    }

    fun onToggleOnlyMine() {
        _uiState.value = _uiState.value.copy(onlyMine = !_uiState.value.onlyMine)
    }

    fun addTask(title: String, description: String, dueDate: String, status: TaskStatus, assigneeIds: List<Int>, projectId: Int = 0) {
        viewModelScope.launch {
            try {
                api.createTask(title = title, description = description, dueDate = dueDate, status = status, projectId = projectId, assigneeIds = assigneeIds)
                loadTasks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

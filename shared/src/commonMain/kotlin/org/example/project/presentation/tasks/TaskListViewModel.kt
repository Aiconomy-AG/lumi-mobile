package org.example.project.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.employee.EmployeeMockApiService
import org.example.project.data.task.TaskMockApiService
import org.example.project.domain.employee.Employee
import org.example.project.domain.employee.EmployeeApi
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi
import org.example.project.domain.task.TaskStatus

data class TaskListUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val employees: List<Employee> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: TaskStatus? = null,
    val onlyMine: Boolean = false,
    val currentUserId: Int = 0,
    val error: String? = null,
) {
    val filteredTasks: List<Task>
        get() = tasks.filter { task ->
            (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true)) &&
                (statusFilter == null || task.status == statusFilter) &&
                (!onlyMine || currentUserId in task.assigneeIds)
        }
}

class TaskListViewModel(
    private val api: TaskApi = TaskMockApiService(),
    private val employeeApi: EmployeeApi = EmployeeMockApiService(),
    private val currentUserId: Int = 0,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState(currentUserId = currentUserId))
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
        loadEmployees()
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

    private fun loadEmployees() {
        viewModelScope.launch {
            try {
                val employees = employeeApi.getEmployees()
                _uiState.value = _uiState.value.copy(employees = employees)
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

    fun addTask(title: String, description: String, dueDate: String, status: TaskStatus, assigneeIds: List<Int>) {
        viewModelScope.launch {
            try {
                api.createTask(title = title, description = description, dueDate = dueDate, status = status, assigneeIds = assigneeIds)
                loadTasks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

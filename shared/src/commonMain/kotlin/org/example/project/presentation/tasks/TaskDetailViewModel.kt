package org.example.project.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.project.data.task.TaskMockApiService
import org.example.project.data.tasktimeentry.TaskTimeEntryMockApiService
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi
import org.example.project.domain.task.TaskStatus
import org.example.project.domain.tasktimeentry.TaskTimeEntryApi

data class TaskDetailUiState(
    val task: Task,
    val isLoading: Boolean = false,
    val isTimerRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val taskTotalSeconds: Int = 0,
    val isSaving: Boolean = false,
    val error: String? = null,
)

private data class HistoricalTotals(
    val isLoading: Boolean = true,
    val myPastSeconds: Int = 0,
    val taskTotalPastSeconds: Int = 0,
    val error: String? = null,
)

class TaskDetailViewModel(
    private val task: Task,
    private val employeeId: Int,
    private val activeTimerViewModel: ActiveTimerViewModel,
    private val taskApi: TaskApi = TaskMockApiService(),
    private val timeEntryApi: TaskTimeEntryApi = TaskTimeEntryMockApiService(),
) : ViewModel() {

    private val historicalState = MutableStateFlow(HistoricalTotals())
    private val currentTaskState = MutableStateFlow(task)
    private val savingState = MutableStateFlow(false to null as String?)

    val uiState: StateFlow<TaskDetailUiState> =
        combine(historicalState, activeTimerViewModel.uiState, currentTaskState, savingState) { historical, active, currentTask, saving ->
            val isMine = active.activeTask?.id == task.id
            TaskDetailUiState(
                task = currentTask,
                isLoading = historical.isLoading,
                isTimerRunning = isMine,
                elapsedSeconds = historical.myPastSeconds + if (isMine) active.elapsedSeconds else 0,
                taskTotalSeconds = historical.taskTotalPastSeconds + if (isMine) active.elapsedSeconds else 0,
                isSaving = saving.first,
                error = historical.error ?: active.error ?: saving.second,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskDetailUiState(task = task, isLoading = true))

    init {
        loadEntries()

        viewModelScope.launch {
            activeTimerViewModel.uiState
                .map { it.activeTask?.id == task.id }
                .distinctUntilChanged()
                .drop(1)
                .collect { isActiveNow -> if (!isActiveNow) loadEntries() }
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            historicalState.value = historicalState.value.copy(isLoading = true, error = null)
            try {
                val allEntries = timeEntryApi.getTimeEntries(task.id)
                val myPast = allEntries
                    .filter { it.employeeId == employeeId && it.stoppedAt != null }
                    .sumOf { it.durationSeconds ?: 0 }
                val allPast = allEntries
                    .filter { it.stoppedAt != null }
                    .sumOf { it.durationSeconds ?: 0 }

                historicalState.value = HistoricalTotals(
                    isLoading = false,
                    myPastSeconds = myPast,
                    taskTotalPastSeconds = allPast,
                )
            } catch (e: Exception) {
                historicalState.value = historicalState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleTimer() {
        if (activeTimerViewModel.isActiveFor(task.id)) {
            activeTimerViewModel.stop()
        } else {
            activeTimerViewModel.start(task)
        }
    }

    fun updateTask(title: String, description: String, dueDate: String, status: TaskStatus, onSuccess: () -> Unit) {
        viewModelScope.launch {
            savingState.value = true to null
            try {
                val updated = taskApi.updateTask(
                    id = task.id,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    status = status,
                )
                currentTaskState.value = updated
                savingState.value = false to null
                onSuccess()
            } catch (e: Exception) {
                savingState.value = false to e.message
            }
        }
    }
}

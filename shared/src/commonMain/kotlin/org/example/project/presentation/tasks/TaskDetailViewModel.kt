package org.example.project.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.tasktimeentry.TaskTimeEntryMockApiService
import org.example.project.domain.task.Task
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryApi
import kotlin.time.Clock

data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val isTimerRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val error: String? = null,
)

class TaskDetailViewModel(
    private val task: Task,
    private val employeeId: Int,
    private val timeEntryApi: TaskTimeEntryApi = TaskTimeEntryMockApiService(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private var activeEntry: TaskTimeEntry? = null
    private var tickingJob: Job? = null

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val entries = timeEntryApi.getTimeEntries(task.id).filter { it.employeeId == employeeId }
                val completed = entries.filter { it.stoppedAt != null }.sumOf { it.durationSeconds ?: 0 }
                val running = entries.firstOrNull { it.stoppedAt == null }
                activeEntry = running

                val elapsed = completed + (running?.let {
                    (Clock.System.now() - it.startedAt).inWholeSeconds.toInt()
                } ?: 0)

                tickingJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isTimerRunning = running != null,
                    elapsedSeconds = elapsed,
                )
                if (running != null) startTicking()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleTimer() {
        if (_uiState.value.isTimerRunning) stopTimer() else startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            try {
                activeEntry = timeEntryApi.startTimer(task.id)
                _uiState.value = _uiState.value.copy(isTimerRunning = true, error = null)
                startTicking()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun stopTimer() {
        val entry = activeEntry ?: return
        viewModelScope.launch {
            try {
                timeEntryApi.stopTimer(task.id, entry.id)
                tickingJob?.cancel()
                activeEntry = null
                _uiState.value = _uiState.value.copy(isTimerRunning = false)
                loadEntries()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun startTicking() {
        tickingJob?.cancel()
        tickingJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(elapsedSeconds = _uiState.value.elapsedSeconds + 1)
            }
        }
    }

    override fun onCleared() {
        tickingJob?.cancel()
    }
}

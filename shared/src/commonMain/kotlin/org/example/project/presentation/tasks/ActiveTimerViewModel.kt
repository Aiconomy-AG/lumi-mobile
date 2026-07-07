package org.example.project.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.task.Task
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryApi

data class ActiveTimerUiState(
    val activeTask: Task? = null,
    val elapsedSeconds: Int = 0,
    val error: String? = null,
)

class ActiveTimerViewModel(
    private val timeEntryApi: TaskTimeEntryApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveTimerUiState())
    val uiState: StateFlow<ActiveTimerUiState> = _uiState.asStateFlow()

    private var activeEntry: TaskTimeEntry? = null
    private var tickingJob: Job? = null

    fun isActiveFor(taskId: Int): Boolean = _uiState.value.activeTask?.id == taskId

    fun start(task: Task) {
        if (_uiState.value.activeTask != null) return
        viewModelScope.launch {
            try {
                val entry = timeEntryApi.startTimer(task.id)
                activeEntry = entry
                _uiState.value = ActiveTimerUiState(activeTask = task)
                startTicking()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun stop() {
        val entry = activeEntry ?: return
        val task = _uiState.value.activeTask ?: return
        viewModelScope.launch {
            try {
                timeEntryApi.stopTimer(task.id, entry.id)
                tickingJob?.cancel()
                activeEntry = null
                _uiState.value = ActiveTimerUiState()
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

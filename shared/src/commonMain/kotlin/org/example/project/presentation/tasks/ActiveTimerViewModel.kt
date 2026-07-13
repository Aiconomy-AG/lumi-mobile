package org.example.project.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskApi
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryApi
import org.example.project.domain.tasktimeentry.TaskTimeEntryRealtimeApi
import org.example.project.domain.tasktimeentry.TimeEntryRealtimeEvent
import kotlin.time.Clock

data class ActiveTimerUiState(
    val activeTask: Task? = null,
    val elapsedSeconds: Int = 0,
    val error: String? = null,
)

class ActiveTimerViewModel(
    private val timeEntryApi: TaskTimeEntryApi,
    private val taskApi: TaskApi,
    private val realtimeApi: TaskTimeEntryRealtimeApi,
    private val currentUserId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveTimerUiState())
    val uiState: StateFlow<ActiveTimerUiState> = _uiState.asStateFlow()

    private var activeEntry: TaskTimeEntry? = null
    private var tickingJob: Job? = null

    init {
        hydrate()
        observeRealtime()
    }

    fun isActiveFor(taskId: Int): Boolean = _uiState.value.activeTask?.id == taskId

    fun start(task: Task) {
        if (_uiState.value.activeTask != null) return
        viewModelScope.launch {
            try {
                val entry = timeEntryApi.startTimer(task.id)
                applyRunning(entry, task)
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
                clear()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun hydrate() {
        viewModelScope.launch {
            try {
                val entry = timeEntryApi.getActiveTimer() ?: return@launch
                if (entry.stoppedAt == null) {
                    applyRemoteEntry(entry)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun observeRealtime() {
        viewModelScope.launch {
            realtimeApi.timeEntryEvents(currentUserId)
                .catch { }
                .collect { event ->
                    when (event) {
                        is TimeEntryRealtimeEvent.Started -> applyRemoteEntry(event.entry)
                        is TimeEntryRealtimeEvent.Stopped -> {
                            if (activeEntry?.id == event.entryId) clear()
                        }
                    }
                }
        }
    }

    private suspend fun applyRemoteEntry(entry: TaskTimeEntry) {
        if (activeEntry?.id == entry.id) return
        val task = try {
            taskApi.getTask(entry.taskId)
        } catch (_: Exception) {
            null
        } ?: return
        applyRunning(entry, task)
    }

    private fun applyRunning(entry: TaskTimeEntry, task: Task) {
        activeEntry = entry
        _uiState.value = ActiveTimerUiState(activeTask = task, elapsedSeconds = currentElapsed(entry))
        startTicking()
    }

    private fun clear() {
        tickingJob?.cancel()
        activeEntry = null
        _uiState.value = ActiveTimerUiState()
    }

    private fun currentElapsed(entry: TaskTimeEntry): Int =
        (Clock.System.now() - entry.startedAt).inWholeSeconds.toInt().coerceAtLeast(0)

    private fun startTicking() {
        tickingJob?.cancel()
        tickingJob = viewModelScope.launch {
            while (true) {
                val entry = activeEntry ?: break
                _uiState.value = _uiState.value.copy(elapsedSeconds = currentElapsed(entry))
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        tickingJob?.cancel()
    }
}

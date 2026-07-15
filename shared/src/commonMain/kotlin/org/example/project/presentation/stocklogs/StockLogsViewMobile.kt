package org.example.project.presentation.stocklogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.auditlogs.AuditLogApi

class StockLogsViewModel(
    private val repository: AuditLogApi
) : ViewModel() {

    private val _state = MutableStateFlow(StockLogsState())
    val state: StateFlow<StockLogsState> = _state.asStateFlow()

    private var loadLogsJob: Job? = null

    init {
        loadAvailableActions()
        loadLogs()
    }

    fun loadLogs() {
        loadLogsJob?.cancel()

        loadLogsJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val current = _state.value

            repository.getAuditLogs(
                page = current.currentPage,
                perPage = current.perPage,
                module = STOCK_MODULE,
                action = current.actionFilter.ifBlank { null },
                from = current.fromFilter.ifBlank { null },
                to = current.toFilter.ifBlank { null }
            ).onSuccess { page ->
                _state.update {
                    it.copy(
                        logs = page.logs,
                        currentPage = page.currentPage,
                        lastPage = page.lastPage,
                        total = page.total,
                        isLoading = false
                    )
                }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        logs = emptyList(),
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Could not load stock logs."
                    )
                }
            }
        }
    }

    private fun loadAvailableActions() {
        viewModelScope.launch {
            repository.getAuditLogs(
                page = 1,
                perPage = 100,
                module = STOCK_MODULE
            ).onSuccess { page ->
                val actions = page.logs
                    .map { it.action }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                _state.update {
                    it.copy(availableActions = actions)
                }
            }
        }
    }

    fun onActionFilterChanged(action: String) {
        _state.update {
            it.copy(
                actionFilter = action,
                currentPage = 1,
                expandedLogId = null
            )
        }

        loadLogs()
    }

    fun onFromFilterChanged(from: String) {
        _state.update {
            it.copy(
                fromFilter = from,
                currentPage = 1,
                expandedLogId = null
            )
        }

        loadLogs()
    }

    fun onToFilterChanged(to: String) {
        _state.update {
            it.copy(
                toFilter = to,
                currentPage = 1,
                expandedLogId = null
            )
        }

        loadLogs()
    }

    fun onPerPageChanged(perPage: Int) {
        _state.update {
            it.copy(
                perPage = perPage,
                currentPage = 1,
                expandedLogId = null
            )
        }

        loadLogs()
    }

    fun onPageChanged(page: Int) {
        if (page !in 1.._state.value.lastPage) {
            return
        }

        _state.update {
            it.copy(
                currentPage = page,
                expandedLogId = null
            )
        }

        loadLogs()
    }

    fun onLogClicked(logId: Int) {
        _state.update {
            it.copy(
                expandedLogId = if (it.expandedLogId == logId) {
                    null
                } else {
                    logId
                }
            )
        }
    }

    companion object {
        private const val STOCK_MODULE = "sales"
    }
}
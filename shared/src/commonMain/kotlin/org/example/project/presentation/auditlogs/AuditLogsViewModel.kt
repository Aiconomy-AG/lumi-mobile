package org.example.project.presentation.auditlogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.auditlogs.AuditLogApi

class AuditLogsViewModel(private val repository: AuditLogApi) : ViewModel() {
    private val _state = MutableStateFlow(AuditLogsState())
    val state: StateFlow<AuditLogsState> = _state.asStateFlow()

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val current = _state.value

            val result = repository.getAuditLogs(
                page = current.currentPage,
                perPage = current.perPage,
                module = current.moduleFilter.ifBlank { null },
                from = current.fromFilter.ifBlank { null },
                to = current.toFilter.ifBlank { null }
            )

            result
                .onSuccess { auditLogPage ->
                    _state.value = _state.value.copy(
                        logs = auditLogPage.logs,
                        currentPage = auditLogPage.currentPage,
                        lastPage = auditLogPage.lastPage,
                        total = auditLogPage.total,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load audit logs."
                    )
                }
        }
    }

    fun onModuleFilterChanged(value: String) {
        _state.value = _state.value.copy(moduleFilter = value, currentPage = 1)
        loadLogs()
    }

    fun onFromFilterChanged(value: String) {
        _state.value = _state.value.copy(fromFilter = value, currentPage = 1)
        loadLogs()
    }

    fun onToFilterChanged(value: String) {
        _state.value = _state.value.copy(toFilter = value, currentPage = 1)
        loadLogs()
    }

    fun onPerPageChanged(perPage: Int) {
        _state.value = _state.value.copy(perPage = perPage, currentPage = 1)
        loadLogs()
    }

    fun onPageChanges(page: Int) {
        if (page < 1 || page > _state.value.lastPage) return
        _state.value = _state.value.copy(currentPage = page)
        loadLogs()
    }

    fun onLogClicked(logId: Int) {
        val current = _state.value.expandedLogId
        _state.value = _state.value.copy(
            expandedLogId = if (current == logId) null else logId
        )
    }
}

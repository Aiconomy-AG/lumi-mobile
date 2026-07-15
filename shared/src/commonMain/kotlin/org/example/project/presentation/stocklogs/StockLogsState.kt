package org.example.project.presentation.stocklogs

import org.example.project.domain.auditlogs.AuditLog

data class StockLogsState(
    val logs: List<AuditLog> = emptyList(),
    val availableActions: List<String> = emptyList(),

    val actionFilter: String = "",
    val fromFilter: String = "",
    val toFilter: String = "",

    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val total: Int? = null,
    val perPage: Int = 20,

    val expandedLogId: Int? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
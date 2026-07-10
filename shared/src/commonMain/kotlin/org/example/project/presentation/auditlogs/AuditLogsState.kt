package org.example.project.presentation.auditlogs

import org.example.project.domain.auditlogs.AuditLog

data class AuditLogsState(
    val logs: List<AuditLog> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val total: Int? = null,
    val perPage: Int = 20,
    val moduleFilter: String = "",
    val fromFilter: String = "",
    val toFilter: String = "",
    val expandedLogId: Int? = null
)

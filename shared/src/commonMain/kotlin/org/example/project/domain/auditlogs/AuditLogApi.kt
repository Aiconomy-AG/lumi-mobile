package org.example.project.domain.auditlogs

interface AuditLogApi {
    suspend fun getAuditLogs(
        page: Int = 1,
        perPage: Int = 20,
        module: String? = null,
        action: String? = null,
        entityType: String? = null,
        entityId: Int? = null,
        actorUserId: Int? = null,
        from: String? = null,
        to: String? = null
    ): Result<AuditLogPage>
}

data class AuditLogPage(
    val logs: List<AuditLog>,
    val currentPage: Int,
    val lastPage: Int,
    val total: Int? = null,
    val perPage: Int? = null
)

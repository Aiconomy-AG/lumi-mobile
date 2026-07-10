package org.example.project.domain.auditlogs

import kotlinx.serialization.json.JsonElement

data class AuditLog(
    val id: Int,
    val module: String,
    val action: String,
    val entityType: String,
    val entityId: Int,
    val entityLabel: String?,
    val actorUserId: Int?,
    val actorName: String,
    val description: String?,
    val changes: AuditLogChanges?,
    val occurredAt: String
)

data class AuditLogChanges(
    val old: Map<String, JsonElement>?,
    val new: Map<String, JsonElement>?
)

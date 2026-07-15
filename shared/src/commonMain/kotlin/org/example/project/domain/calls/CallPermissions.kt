package org.example.project.domain.calls

import kotlinx.coroutines.flow.StateFlow

enum class CallPermissionState {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    REQUESTING,
}

expect object CallPermissions {
    val state: StateFlow<CallPermissionState>
    fun initialize(platformContext: Any? = null)
    suspend fun ensureForCall(type: String): Boolean
    suspend fun requestLaunchPermissionsIfNeeded(): Boolean
    fun hasAudio(): Boolean
    fun hasCamera(): Boolean
    fun refresh()
    fun openAppSettings()
}

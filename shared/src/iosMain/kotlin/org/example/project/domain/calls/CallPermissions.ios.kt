package org.example.project.domain.calls

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object CallPermissions {
    actual val state: StateFlow<CallPermissionState> =
        MutableStateFlow(CallPermissionState.GRANTED)

    actual fun initialize(platformContext: Any?) = Unit

    actual suspend fun ensureForCall(type: String): Boolean = true

    actual fun hasAudio(): Boolean = true

    actual fun hasCamera(): Boolean = true

    actual fun refresh() = Unit

    actual fun openAppSettings() = Unit
}

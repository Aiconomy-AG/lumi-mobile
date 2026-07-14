package org.example.project.domain.calls

actual object CallPermissions {
    actual fun initialize(platformContext: Any?) = Unit

    actual suspend fun ensureForCall(type: String): Boolean = true

    actual fun hasAudio(): Boolean = true

    actual fun hasCamera(): Boolean = true
}

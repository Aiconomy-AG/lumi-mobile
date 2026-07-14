package org.example.project.domain.calls

expect object CallPermissions {
    fun initialize(platformContext: Any? = null)
    suspend fun ensureForCall(type: String): Boolean
    fun hasAudio(): Boolean
    fun hasCamera(): Boolean
}

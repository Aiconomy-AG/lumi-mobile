package org.example.project.data.auth

expect object SessionStorage {
    fun initialize(platformContext: Any? = null)
    fun saveSession(session: UserSession)
    fun loadSession(): UserSession?
    fun clearSession()
}

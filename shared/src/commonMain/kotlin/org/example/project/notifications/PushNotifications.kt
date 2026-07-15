package org.example.project.notifications

expect object PushNotifications {
    fun initialize(platformContext: Any? = null)
    suspend fun requestPermission(): Boolean
    suspend fun getFcmToken(): String?
    suspend fun getVoipToken(): String?
}

package org.example.project.notifications

actual object PushNotifications {
    actual fun initialize(platformContext: Any?) {
        
    }

    actual suspend fun requestPermission(): Boolean = false

    actual suspend fun getFcmToken(): String? = null
}

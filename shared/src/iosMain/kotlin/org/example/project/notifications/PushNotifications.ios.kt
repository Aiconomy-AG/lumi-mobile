package org.example.project.notifications

actual object PushNotifications {
    actual fun initialize(platformContext: Any?) {
        // iOS push is configured in Swift; real implementation pending Apple Developer account.
    }

    actual suspend fun requestPermission(): Boolean = false

    actual suspend fun getFcmToken(): String? = null
}

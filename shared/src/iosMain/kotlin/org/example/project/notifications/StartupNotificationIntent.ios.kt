package org.example.project.notifications

actual fun processStartupNotificationIntent() {
    PendingNotificationIntent.consume()?.let { data ->
        val link = NotificationRouter.parse(data) ?: return
        NotificationRouter.emit(link)
    }
}

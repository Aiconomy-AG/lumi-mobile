package org.example.project.notifications

actual fun processStartupNotificationIntent() {
    PendingNotificationIntent.consume()?.let { AndroidNotificationIntents.handle(it) }
}

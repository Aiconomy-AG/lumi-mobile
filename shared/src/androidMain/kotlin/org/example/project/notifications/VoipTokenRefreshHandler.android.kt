package org.example.project.notifications

actual fun installVoipTokenRefreshHandler(handler: suspend (String) -> Unit) = Unit

package org.example.project.notifications

expect fun installVoipTokenRefreshHandler(handler: suspend (String) -> Unit)

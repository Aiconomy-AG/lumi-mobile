package org.example.project.notifications

expect fun installTokenRefreshHandler(handler: suspend (String) -> Unit)

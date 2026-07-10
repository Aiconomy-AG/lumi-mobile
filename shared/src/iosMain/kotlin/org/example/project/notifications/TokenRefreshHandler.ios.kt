package org.example.project.notifications

actual fun installTokenRefreshHandler(handler: suspend (String) -> Unit) {
    TokenRefreshHandlerHolder.handler = handler
}

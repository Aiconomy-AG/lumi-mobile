package org.example.project.notifications

internal object TokenRefreshHandlerHolder {
    var handler: (suspend (String) -> Unit)? = null
}

actual fun installTokenRefreshHandler(handler: suspend (String) -> Unit) {
    TokenRefreshHandlerHolder.handler = handler
}

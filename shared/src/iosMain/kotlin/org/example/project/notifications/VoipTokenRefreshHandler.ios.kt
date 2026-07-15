package org.example.project.notifications

internal object VoipTokenRefreshHandlerHolder {
    var handler: (suspend (String) -> Unit)? = null
}

actual fun installVoipTokenRefreshHandler(handler: suspend (String) -> Unit) {
    VoipTokenRefreshHandlerHolder.handler = handler
}

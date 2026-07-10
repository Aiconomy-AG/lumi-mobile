package org.example.project.notifications

internal object TokenRefreshHandlerHolder {
    var handler: (suspend (String) -> Unit)? = null
}

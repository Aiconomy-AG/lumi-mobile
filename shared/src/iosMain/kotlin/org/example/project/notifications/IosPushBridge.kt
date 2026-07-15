package org.example.project.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.example.project.domain.calls.CallPermissions

private val bridgeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

object IosPushTokenStore {
    var fcmToken: String? = null
        private set

    var permissionGranted: Boolean = false
        private set

    private val tokenWaiters = mutableListOf<(String?) -> Unit>()
    private val permissionWaiters = mutableListOf<(Boolean) -> Unit>()

    fun updateToken(token: String?) {
        fcmToken = token
        if (token != null) {
            val waiters = tokenWaiters.toList()
            tokenWaiters.clear()
            waiters.forEach { it(token) }
        }
    }

    fun updatePermission(granted: Boolean) {
        permissionGranted = granted
        val waiters = permissionWaiters.toList()
        permissionWaiters.clear()
        waiters.forEach { it(granted) }
    }

    fun awaitToken(callback: (String?) -> Unit) {
        val current = fcmToken
        if (current != null) {
            callback(current)
            return
        }
        tokenWaiters.add(callback)
    }

    fun awaitPermission(callback: (Boolean) -> Unit) {
        if (permissionGranted) {
            callback(true)
            return
        }
        permissionWaiters.add(callback)
    }
}

object IosVoipTokenStore {
    var voipToken: String? = null
        private set

    private val tokenWaiters = mutableListOf<(String?) -> Unit>()

    fun updateToken(token: String?) {
        voipToken = token
        if (token != null) {
            val waiters = tokenWaiters.toList()
            tokenWaiters.clear()
            waiters.forEach { it(token) }
        }
    }

    fun awaitToken(callback: (String?) -> Unit) {
        val current = voipToken
        if (current != null) {
            callback(current)
            return
        }
        tokenWaiters.add(callback)
    }
}

fun updateIosNotificationPermission(granted: Boolean) {
    IosPushTokenStore.updatePermission(granted)
}

fun onIosFcmTokenRefreshed(token: String) {
    IosPushTokenStore.updateToken(token)

    val handler = TokenRefreshHandlerHolder.handler ?: return
    bridgeScope.launch {
        handler(token)
    }
}

fun onIosVoipTokenRefreshed(token: String) {
    IosVoipTokenStore.updateToken(token)

    val handler = VoipTokenRefreshHandlerHolder.handler ?: return
    bridgeScope.launch {
        handler(token)
    }
}

fun onIosNotificationDataReceived(data: Map<String, String>) {
    val link = NotificationRouter.parse(data) ?: return
    PendingNotificationIntent.enqueue(data)
    NotificationRouter.emit(link)
}

fun refreshIosCallPermissions() {
    CallPermissions.refresh()
}

fun requestIosLaunchCallPermissions() {
    bridgeScope.launch {
        CallPermissions.requestLaunchPermissionsIfNeeded()
    }
}

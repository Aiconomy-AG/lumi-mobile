package org.example.project.notifications

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

actual object PushNotifications {
    actual fun initialize(platformContext: Any?) {
    }

    actual suspend fun requestPermission(): Boolean {
        if (IosPushTokenStore.permissionGranted) {
            return true
        }

        return withTimeoutOrNull(PERMISSION_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                IosPushTokenStore.awaitPermission { granted ->
                    continuation.resume(granted)
                }
            }
        } ?: IosPushTokenStore.permissionGranted
    }

    actual suspend fun getFcmToken(): String? {
        IosPushTokenStore.fcmToken?.let { return it }

        return withTimeoutOrNull(TOKEN_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                IosPushTokenStore.awaitToken { token ->
                    continuation.resume(token)
                }
            }
        }
    }

    private const val PERMISSION_TIMEOUT_MS = 30_000L
    private const val TOKEN_TIMEOUT_MS = 15_000L
}

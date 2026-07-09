package org.example.project.notifications

import io.ktor.client.HttpClient
import org.example.project.data.notifications.DeviceTokenApiService
import org.example.project.notifications.platform.currentDevicePlatform

object PushNotificationCoordinator {
    private var authToken: String? = null
    private var httpClient: HttpClient? = null
    private var baseUrl: String = ""
    private var lastFcmToken: String? = null

    fun configure(httpClient: HttpClient, baseUrl: String) {
        this.httpClient = httpClient
        this.baseUrl = baseUrl
    }

    suspend fun registerAfterLogin(authToken: String) {
        this.authToken = authToken
        val fcmToken = PushNotifications.getFcmToken() ?: return
        lastFcmToken = fcmToken
        registerToken(authToken, fcmToken)
    }

    suspend fun onTokenRefreshed(fcmToken: String) {
        lastFcmToken = fcmToken
        val token = authToken ?: return
        registerToken(token, fcmToken)
    }

    suspend fun unregisterOnLogout(authToken: String) {
        val client = httpClient ?: return
        val fcmToken = lastFcmToken ?: PushNotifications.getFcmToken()
        if (fcmToken != null) {
            try {
                DeviceTokenApiService(client, baseUrl, authToken)
                    .unregisterDeviceToken(fcmToken)
            } catch (_: Exception) {
            }
        }
        this.authToken = null
        lastFcmToken = null
    }

    private suspend fun registerToken(authToken: String, fcmToken: String) {
        val client = httpClient ?: return
        try {
            DeviceTokenApiService(client, baseUrl, authToken)
                .registerDeviceToken(
                    fcmToken = fcmToken,
                    platform = currentDevicePlatform(),
                )
        } catch (_: Exception) {
        }
    }
}

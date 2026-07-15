package org.example.project.notifications

import io.ktor.client.HttpClient
import org.example.project.data.notifications.DeviceTokenApiService
import org.example.project.data.notifications.DevicePlatform
import org.example.project.data.notifications.currentDeviceId
import org.example.project.notifications.platform.currentDevicePlatform

object PushNotificationCoordinator {
    private var authToken: String? = null
    private var httpClient: HttpClient? = null
    private var baseUrl: String = ""
    private var lastFcmToken: String? = null
    private var lastVoipToken: String? = null

    fun configure(httpClient: HttpClient, baseUrl: String) {
        this.httpClient = httpClient
        this.baseUrl = baseUrl
    }

    suspend fun registerAfterLogin(authToken: String) {
        this.authToken = authToken
        val fcmToken = PushNotifications.getFcmToken()
        if (fcmToken != null) {
            lastFcmToken = fcmToken
            registerToken(authToken, fcmToken, currentDevicePlatform())
        }
        val voipToken = PushNotifications.getVoipToken()
        if (voipToken != null) {
            lastVoipToken = voipToken
            registerToken(authToken, voipToken, DevicePlatform.APNS_VOIP)
        }
    }

    suspend fun reregisterIfPossible() {
        val token = authToken ?: return
        val fcmToken = PushNotifications.getFcmToken()
        if (fcmToken != null) {
            lastFcmToken = fcmToken
            registerToken(token, fcmToken, currentDevicePlatform())
        }
        val voipToken = PushNotifications.getVoipToken()
        if (voipToken != null) {
            lastVoipToken = voipToken
            registerToken(token, voipToken, DevicePlatform.APNS_VOIP)
        }
    }

    suspend fun onTokenRefreshed(fcmToken: String) {
        lastFcmToken = fcmToken
        val token = authToken ?: return
        registerToken(token, fcmToken, currentDevicePlatform())
    }

    suspend fun onVoipTokenRefreshed(voipToken: String) {
        lastVoipToken = voipToken
        val token = authToken ?: return
        registerToken(token, voipToken, DevicePlatform.APNS_VOIP)
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
        val voipToken = lastVoipToken ?: PushNotifications.getVoipToken()
        if (voipToken != null) {
            try {
                DeviceTokenApiService(client, baseUrl, authToken)
                    .unregisterDeviceToken(voipToken)
            } catch (_: Exception) {
            }
        }
        this.authToken = null
        lastFcmToken = null
        lastVoipToken = null
    }

    private suspend fun registerToken(
        authToken: String,
        token: String,
        platform: DevicePlatform,
    ) {
        val client = httpClient ?: return
        try {
            DeviceTokenApiService(client, baseUrl, authToken)
                .registerDeviceToken(
                    fcmToken = token,
                    platform = platform,
                    deviceId = currentDeviceId(),
                )
        } catch (_: Exception) {
        }
    }
}

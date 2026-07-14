package org.example.project.data.notifications

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.data.calls.ClientInstanceIdStorage

enum class DevicePlatform {
    @SerialName("fcm_android")
    FCM_ANDROID,

    @SerialName("android")
    ANDROID,

    @SerialName("ios")
    IOS,
}

class DeviceTokenApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) {
    suspend fun registerDeviceToken(
        fcmToken: String,
        platform: DevicePlatform,
        deviceId: String? = null,
    ): Result<Unit> {
        return try {
            val response = client.post("$baseUrl/device-tokens") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterDeviceTokenRequest(
                        token = fcmToken,
                        platform = platform,
                        deviceId = deviceId,
                    )
                )
            }

            val responseText = response.bodyAsText()
            if (response.status != HttpStatusCode.Created && response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not register device token."))
        }
    }

    suspend fun unregisterDeviceToken(fcmToken: String): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/device-tokens") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(UnregisterDeviceTokenRequest(token = fcmToken))
            }

            val responseText = response.bodyAsText()
            if (response.status != HttpStatusCode.NoContent && response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not unregister device token."))
        }
    }

    private fun HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            deviceTokenJson.decodeFromString<ApiErrorResponse>(responseText).message
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

fun currentDeviceId(): String = ClientInstanceIdStorage.getOrCreate()

@Serializable
private data class RegisterDeviceTokenRequest(
    val token: String,
    val platform: DevicePlatform,
    @SerialName("device_id") val deviceId: String? = null,
)

@Serializable
private data class UnregisterDeviceTokenRequest(
    val token: String,
)

@Serializable
private data class ApiErrorResponse(
    val message: String = "Request failed.",
)

private val deviceTokenJson = Json {
    ignoreUnknownKeys = true
}

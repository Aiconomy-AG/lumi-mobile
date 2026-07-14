package org.example.project.data.realtime

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

data class ReverbEvent(val name: String, val data: JsonElement?)

class ReverbPrivateChannelClient(
    private val client: HttpClient,
    private val baseUrl: String,
    private val appKey: String,
    private val host: String,
    private val port: Int,
    private val scheme: String,
    private val token: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val streams = mutableMapOf<String, Flow<ReverbEvent>>()

    fun events(channel: String): Flow<ReverbEvent> = streams.getOrPut(channel) {
        channelEvents(channel).shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            replay = 0,
        )
    }

    fun close() {
        scope.cancel()
        streams.clear()
    }

    private fun channelEvents(channel: String): Flow<ReverbEvent> = flow {
        require(appKey.isNotBlank()) { "Reverb app key is not configured." }
        val channelName = if (channel.startsWith("private-")) channel else "private-$channel"
        while (currentCoroutineContext().isActive) {
            try {
                client.webSocket(urlString = webSocketUrl()) {
                    for (frame in incoming) {
                        val envelope = realtimeJson.decodeFromString<Envelope>((frame as? Frame.Text)?.readText() ?: continue)
                        when (envelope.event) {
                            "pusher:connection_established" -> {
                                val socketId = envelope.data.decodeNested<ConnectionData>().socketId
                                val auth = authenticate(channelName, socketId)
                                send(realtimeJson.encodeToString(SubscribeEnvelope(data = SubscribeData(auth.auth, channelName, auth.channelData))))
                            }
                            "pusher:ping" -> send(realtimeJson.encodeToString(EventEnvelope(event = "pusher:pong")))
                            "pusher_internal:subscription_succeeded" -> Unit
                            else -> emit(ReverbEvent(envelope.event.removePrefix("."), envelope.data))
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                delay(3_000)
            }
        }
    }

    private suspend fun authenticate(channel: String, socketId: String): AuthResponse {
        val response = client.submitForm("${baseUrl.trimEnd('/')}/broadcasting/auth", Parameters.build {
            append("channel_name", channel); append("socket_id", socketId)
        }) {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "application/json")
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) error("Could not authenticate real-time channel.")
        return realtimeJson.decodeFromString(text)
    }

    private fun webSocketUrl(): String {
        val match = Regex("^(https?)://([^/:]+)(?::(\\d+))?").find(baseUrl)
        val baseScheme = match?.groupValues?.get(1) ?: "https"
        val baseHost = match?.groupValues?.get(2).orEmpty()
        val basePort = match?.groupValues?.getOrNull(3)?.toIntOrNull() ?: 0
        val wsScheme = if (scheme.ifBlank { baseScheme } in setOf("https", "wss")) "wss" else "ws"
        val resolvedPort = if (port > 0) port else if (basePort > 0) basePort else if (wsScheme == "wss") 443 else 80
        val suffix = if ((wsScheme == "wss" && resolvedPort == 443) || (wsScheme == "ws" && resolvedPort == 80)) "" else ":$resolvedPort"
        return "$wsScheme://${host.ifBlank { baseHost }}$suffix/app/$appKey?protocol=7&client=lumi-mobile&version=1.0&flash=false"
    }
}

internal inline fun <reified T> JsonElement?.decodeRealtime(): T = when (this) {
    is JsonPrimitive -> realtimeJson.decodeFromString(content)
    is JsonObject -> realtimeJson.decodeFromJsonElement(this)
    else -> error("Unexpected real-time payload.")
}

private inline fun <reified T> JsonElement?.decodeNested(): T = decodeRealtime()
@Serializable private data class Envelope(val event: String, val data: JsonElement? = null)
@Serializable private data class EventEnvelope(val event: String)
@Serializable private data class SubscribeEnvelope(val event: String = "pusher:subscribe", val data: SubscribeData)
@Serializable private data class SubscribeData(val auth: String, val channel: String, @SerialName("channel_data") val channelData: String? = null)
@Serializable private data class ConnectionData(@SerialName("socket_id") val socketId: String)
@Serializable private data class AuthResponse(val auth: String, @SerialName("channel_data") val channelData: String? = null)
internal val realtimeJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

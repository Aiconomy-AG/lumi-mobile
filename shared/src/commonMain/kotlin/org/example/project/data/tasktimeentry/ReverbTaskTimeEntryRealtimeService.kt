package org.example.project.data.tasktimeentry

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.domain.tasktimeentry.TaskTimeEntryRealtimeApi
import org.example.project.domain.tasktimeentry.TimeEntryRealtimeEvent
import kotlin.time.Instant

class ReverbTaskTimeEntryRealtimeService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val appKey: String,
    private val host: String,
    private val port: Int,
    private val scheme: String,
    private val token: String,
) : TaskTimeEntryRealtimeApi {

    override fun timeEntryEvents(userId: Int): Flow<TimeEntryRealtimeEvent> = flow {
        require(appKey.isNotBlank()) { "Reverb app key is not configured." }

        val channelName = "private-users.$userId"
        val webSocketUrl = buildWebSocketUrl()

        while (currentCoroutineContext().isActive) {
            try {
                client.webSocket(urlString = webSocketUrl) {
                    for (frame in incoming) {
                        val text = (frame as? Frame.Text)?.readText() ?: continue
                        val envelope = realtimeJson.decodeFromString<PusherEnvelope>(text)

                        when (envelope.event) {
                            "pusher:connection_established" -> {
                                val socketId = envelope.data.decodeNested<ConnectionEstablishedData>().socketId
                                val auth = authenticateChannel(channelName, socketId)
                                send(
                                    realtimeJson.encodeToString(
                                        PusherSubscribeEnvelope.serializer(),
                                        PusherSubscribeEnvelope(
                                            data = SubscribeData(
                                                auth = auth.auth,
                                                channel = channelName,
                                                channelData = auth.channelData,
                                            )
                                        )
                                    )
                                )
                            }

                            "pusher:ping" -> send(
                                realtimeJson.encodeToString(
                                    PusherEventEnvelope.serializer(),
                                    PusherEventEnvelope(event = "pusher:pong"),
                                )
                            )

                            "time-entry.started", ".time-entry.started" -> {
                                val payload = envelope.data.decodeNested<TimeEntryPayload>()
                                emit(TimeEntryRealtimeEvent.Started(payload.toTimeEntry()))
                            }

                            "time-entry.stopped", ".time-entry.stopped" -> {
                                val payload = envelope.data.decodeNested<TimeEntryPayload>()
                                emit(TimeEntryRealtimeEvent.Stopped(entryId = payload.id, taskId = payload.taskId))
                            }
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                delay(RECONNECT_DELAY_MS)
            }
        }
    }.flowOn(Dispatchers.Default)

    private suspend fun authenticateChannel(channelName: String, socketId: String): BroadcastAuthResponse {
        val response = client.submitForm(
            url = "${baseUrl.trimEnd('/')}/broadcasting/auth",
            formParameters = Parameters.build {
                append("channel_name", channelName)
                append("socket_id", socketId)
            },
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "application/json")
        }

        val text = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Could not authenticate real-time time-entry channel.")
        }

        return realtimeJson.decodeFromString(text)
    }

    private fun buildWebSocketUrl(): String {
        val baseParts = parseBaseUrl(baseUrl)
        val resolvedScheme = scheme.ifBlank { baseParts.scheme }
        val webSocketScheme = when (resolvedScheme) {
            "https", "wss" -> "wss"
            else -> "ws"
        }
        val resolvedHost = host.ifBlank { baseParts.host }
        val resolvedPort = when {
            port > 0 -> port
            baseParts.port > 0 -> baseParts.port
            webSocketScheme == "wss" -> 443
            else -> 80
        }
        val portSuffix = when {
            webSocketScheme == "wss" && resolvedPort == 443 -> ""
            webSocketScheme == "ws" && resolvedPort == 80 -> ""
            else -> ":$resolvedPort"
        }

        return "$webSocketScheme://$resolvedHost$portSuffix/app/$appKey?protocol=7&client=lumi-mobile&version=1.0&flash=false"
    }

    private fun parseBaseUrl(value: String): BaseUrlParts {
        val match = UrlPattern.find(value.trim())
            ?: return BaseUrlParts(scheme = "https", host = "", port = 0)
        val scheme = match.groupValues[1]
        val host = match.groupValues[2]
        val port = match.groupValues.getOrNull(3)?.toIntOrNull() ?: 0
        return BaseUrlParts(scheme = scheme, host = host, port = port)
    }

    private inline fun <reified T> JsonElement?.decodeNested(): T {
        return when (this) {
            is JsonPrimitive -> realtimeJson.decodeFromString(content)
            is JsonObject -> realtimeJson.decodeFromJsonElement(this)
            else -> throw IllegalArgumentException("Unexpected real-time payload.")
        }
    }

    private data class BaseUrlParts(
        val scheme: String,
        val host: String,
        val port: Int,
    )

    private companion object {
        const val RECONNECT_DELAY_MS = 3_000L
        val UrlPattern = Regex("""^(https?)://([^/:]+)(?::(\d+))?""")
    }
}

@Serializable
private data class PusherEnvelope(
    val event: String,
    val data: JsonElement? = null,
    val channel: String? = null,
)

@Serializable
private data class PusherEventEnvelope(
    val event: String,
)

@Serializable
private data class PusherSubscribeEnvelope(
    val event: String = "pusher:subscribe",
    val data: SubscribeData,
)

@Serializable
private data class SubscribeData(
    val auth: String,
    val channel: String,
    @SerialName("channel_data")
    val channelData: String? = null,
)

@Serializable
private data class ConnectionEstablishedData(
    @SerialName("socket_id")
    val socketId: String,
)

@Serializable
private data class BroadcastAuthResponse(
    val auth: String,
    @SerialName("channel_data")
    val channelData: String? = null,
)

@Serializable
private data class TimeEntryPayload(
    val id: Int,
    @SerialName("task_id")
    val taskId: Int,
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("started_at")
    val startedAt: String? = null,
    @SerialName("stopped_at")
    val stoppedAt: String? = null,
    @SerialName("duration_seconds")
    val durationSeconds: Int? = null,
) {
    fun toTimeEntry(): TaskTimeEntry = TaskTimeEntry(
        id = id,
        taskId = taskId,
        employeeId = employeeId,
        startedAt = Instant.parse(startedAt ?: throw IllegalArgumentException("Missing started_at")),
        stoppedAt = stoppedAt?.let { Instant.parse(it) },
        durationSeconds = durationSeconds,
    )
}

private val realtimeJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

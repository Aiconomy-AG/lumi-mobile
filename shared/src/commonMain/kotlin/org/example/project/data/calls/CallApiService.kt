package org.example.project.data.calls

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.calls.CallApi
import org.example.project.domain.calls.CallApiException
import org.example.project.domain.calls.CallHistoryPage
import org.example.project.domain.calls.WorkspaceCall

class CallApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) : CallApi {

    override suspend fun start(
        calleeIds: List<Int>,
        clientInstanceId: String,
        type: String,
        mode: String?,
    ): WorkspaceCall = post(
        "$baseUrl/calls",
        StartCallRequest(
            calleeIds = calleeIds,
            type = type,
            mode = mode,
            clientInstanceId = clientInstanceId,
        ),
    )

    override suspend fun startFromConversation(conversationId: Int, clientInstanceId: String): WorkspaceCall =
        post(
            "$baseUrl/workspace/conversations/$conversationId/calls",
            ClientRequest(clientInstanceId),
        )

    override suspend fun get(callId: String): WorkspaceCall {
        val response = client.get("$baseUrl/calls/$callId") { bearer() }
        val text = response.bodyAsText()
        ensureSuccess(response.status.isSuccess(), text)
        return callsJson.decodeFromString<CallResponse>(text).data
    }

    override suspend fun active(clientInstanceId: String): WorkspaceCall? {
        val response = client.get("$baseUrl/calls/active?client_instance_id=$clientInstanceId") { bearer() }
        val text = response.bodyAsText()
        ensureSuccess(response.status.isSuccess(), text)
        return callsJson.decodeFromString<NullableCallResponse>(text).data
    }

    override suspend fun history(page: Int, perPage: Int): CallHistoryPage {
        val response = client.get("$baseUrl/calls/history?per_page=$perPage&page=$page") { bearer() }
        val text = response.bodyAsText()
        ensureSuccess(response.status.isSuccess(), text)
        return callsJson.decodeFromString<CallHistoryPage>(text)
    }

    override suspend fun accept(callId: String, clientInstanceId: String): WorkspaceCall =
        post("$baseUrl/calls/$callId/accept", ClientRequest(clientInstanceId))

    override suspend fun decline(callId: String): WorkspaceCall =
        postWithoutBody("$baseUrl/calls/$callId/decline")

    override suspend fun cancel(callId: String): WorkspaceCall =
        postWithoutBody("$baseUrl/calls/$callId/cancel")

    override suspend fun leave(callId: String): WorkspaceCall =
        postWithoutBody("$baseUrl/calls/$callId/leave")

    override suspend fun invite(callId: String, userIds: List<Int>): WorkspaceCall =
        post("$baseUrl/calls/$callId/invite", InviteRequest(userIds))

    override suspend fun end(callId: String): WorkspaceCall =
        postWithoutBody("$baseUrl/calls/$callId/end")

    private suspend inline fun <reified T> post(url: String, body: T? = null): WorkspaceCall {
        val response = client.post(url) {
            bearer()
            contentType(ContentType.Application.Json)
            if (body != null) setBody(body)
        }
        val text = response.bodyAsText()
        ensureSuccess(response.status.isSuccess(), text)
        return callsJson.decodeFromString<CallResponse>(text).data
    }

    private suspend fun postWithoutBody(url: String): WorkspaceCall {
        val response = client.post(url) { bearer() }
        val text = response.bodyAsText()
        ensureSuccess(response.status.isSuccess(), text)
        return callsJson.decodeFromString<CallResponse>(text).data
    }

    private fun HttpRequestBuilder.bearer() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun ensureSuccess(success: Boolean, text: String) {
        if (success) return
        val error = runCatching { callsJson.decodeFromString<ApiError>(text) }.getOrNull()
        throw CallApiException(error?.message ?: "Call request failed.", error?.code)
    }
}

@Serializable
private data class CallResponse(val data: WorkspaceCall)

@Serializable
private data class NullableCallResponse(val data: WorkspaceCall? = null)

@Serializable
private data class ClientRequest(
    @SerialName("client_instance_id") val clientInstanceId: String,
)

@Serializable
private data class StartCallRequest(
    @SerialName("callee_ids") val calleeIds: List<Int>,
    val type: String,
    val mode: String? = null,
    @SerialName("client_instance_id") val clientInstanceId: String,
)

@Serializable
private data class InviteRequest(
    @SerialName("user_ids") val userIds: List<Int>,
)

@Serializable
private data class ApiError(
    val message: String = "Call request failed.",
    val code: String? = null,
)

internal val callsJson = Json { ignoreUnknownKeys = true; explicitNulls = false }

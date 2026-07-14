package org.example.project.data.calls

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.example.project.data.realtime.ReverbPrivateChannelClient
import org.example.project.data.realtime.decodeRealtime
import org.example.project.domain.calls.CallRealtimeApi
import org.example.project.domain.calls.WorkspaceCall

class ReverbCallRealtimeService(
    client: HttpClient,
    baseUrl: String,
    appKey: String,
    host: String,
    port: Int,
    scheme: String,
    token: String,
) : CallRealtimeApi {
    private val realtime = ReverbPrivateChannelClient(client, baseUrl, appKey, host, port, scheme, token)

    override fun events(userId: Int): Flow<WorkspaceCall> = realtime.events("users.$userId")
        .filter { it.name == "call.ringing" || it.name == "call.updated" }
        .map { it.data.decodeRealtime<WorkspaceCall>() }
}


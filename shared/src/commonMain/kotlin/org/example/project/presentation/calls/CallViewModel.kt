package org.example.project.presentation.calls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.calls.CallApi
import org.example.project.domain.calls.CallApiException
import org.example.project.domain.calls.CallRealtimeApi
import org.example.project.domain.calls.CallStatus
import org.example.project.domain.calls.PlatformCallController
import org.example.project.domain.calls.WorkspaceCall
import kotlin.random.Random

data class CallUiState(
    val call: WorkspaceCall? = null,
    val muted: Boolean = false,
    val connectionLabel: String = "Disconnected",
    val error: String? = null,
)

class CallViewModel(
    private val currentUserId: Int,
    private val api: CallApi,
    private val realtime: CallRealtimeApi,
    private val platform: PlatformCallController,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val instanceId = "mobile-$currentUserId-${Random.nextLong().toString(16)}"
    private val _state = MutableStateFlow(CallUiState())
    val state: StateFlow<CallUiState> = _state.asStateFlow()

    init {
        recover()
        scope.launch {
            realtime.events(currentUserId).collect(::handleRealtime)
        }
    }

    fun start(conversationId: Int) {
        scope.launch {
            runCallRequest { api.start(conversationId, instanceId) }
        }
    }

    fun close() = scope.cancel()

    fun recover() {
        scope.launch {
            runCatching { api.active(instanceId) }.onSuccess { call ->
                if (call != null) activate(call)
            }
        }
    }

    fun openFromNotification(action: String?) {
        scope.launch {
            val recovered = runCatching { api.active(instanceId) }.getOrNull() ?: return@launch
            activate(recovered)
            when (action) {
                "answer" -> runCallRequest { api.accept(recovered.id, instanceId) }
                "decline", "hangup" -> {
                    runCatching {
                        if (recovered.status == CallStatus.RINGING) {
                            api.decline(recovered.id)
                        } else {
                            api.end(recovered.id)
                        }
                    }
                    platform.disconnect()
                    platform.dismissIncoming(recovered.id)
                    _state.value = CallUiState()
                }
            }
        }
    }

    fun accept() = action { call -> api.accept(call.id, instanceId) }
    fun decline() = finish { call -> api.decline(call.id) }
    fun cancel() = finish { call -> api.cancel(call.id) }
    fun end() = finish { call -> api.end(call.id) }

    fun toggleMute() {
        scope.launch {
            val next = !_state.value.muted
            platform.setMuted(next)
            _state.value = _state.value.copy(muted = next)
        }
    }

    private fun action(block: suspend (WorkspaceCall) -> WorkspaceCall) {
        val call = _state.value.call ?: return
        scope.launch { runCallRequest { block(call) } }
    }

    private fun finish(block: suspend (WorkspaceCall) -> WorkspaceCall) {
        val call = _state.value.call ?: return
        scope.launch {
            runCatching { block(call) }
            platform.disconnect()
            platform.dismissIncoming(call.id)
            _state.value = CallUiState()
        }
    }

    private suspend fun runCallRequest(block: suspend () -> WorkspaceCall) {
        try {
            activate(block())
        } catch (error: CallApiException) {
            _state.value = _state.value.copy(error = error.message)
        } catch (error: Exception) {
            _state.value = _state.value.copy(error = error.message ?: "Call request failed.")
        }
    }

    private suspend fun activate(call: WorkspaceCall) {
        if (call.status.isTerminal) {
            platform.disconnect()
            platform.dismissIncoming(call.id)
            _state.value = CallUiState()
            return
        }
        _state.value = _state.value.copy(call = call, error = null)
        val incoming = call.status == CallStatus.RINGING && call.initiatedByUserId != currentUserId
        if (incoming) platform.showIncoming(call)
        if (call.connection != null) {
            _state.value = _state.value.copy(connectionLabel = "Connecting")
            platform.connect(call)
            _state.value = _state.value.copy(connectionLabel = "Connected")
        }
    }

    private suspend fun handleRealtime(call: WorkspaceCall) {
        val current = _state.value.call
        if (current != null && current.id != call.id) return
        if (call.status.isTerminal || (
                call.initiatedByUserId != currentUserId &&
                    call.status == CallStatus.ACTIVE &&
                    call.answeredClientInstanceId != instanceId
            )) {
            platform.disconnect()
            platform.dismissIncoming(call.id)
            _state.value = CallUiState(
                error = if (call.status == CallStatus.ACTIVE) "Answered on another device." else null,
            )
            return
        }
        activate(call)
    }
}

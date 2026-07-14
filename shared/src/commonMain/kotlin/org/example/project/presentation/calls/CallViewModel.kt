package org.example.project.presentation.calls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.calls.ClientInstanceIdStorage
import org.example.project.domain.calls.CallApi
import org.example.project.domain.calls.CallApiException
import org.example.project.domain.calls.CallPresenceRealtimeApi
import org.example.project.domain.calls.CallRealtimeApi
import org.example.project.domain.calls.CallStatus
import org.example.project.domain.calls.PlatformCallController
import org.example.project.domain.calls.WorkspaceCall

data class CallUiState(
    val call: WorkspaceCall? = null,
    val muted: Boolean = false,
    val cameraEnabled: Boolean = false,
    val connectionLabel: String = "Disconnected",
    val remoteParticipantCount: Int = 0,
    val error: String? = null,
)

class CallViewModel(
    private val currentUserId: Int,
    private val api: CallApi,
    private val realtime: CallRealtimeApi,
    private val presenceRealtime: CallPresenceRealtimeApi?,
    private val platform: PlatformCallController,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val instanceId = ClientInstanceIdStorage.getOrCreate()
    private val _state = MutableStateFlow(CallUiState())
    val state: StateFlow<CallUiState> = _state.asStateFlow()
    private var presenceJob: Job? = null

    init {
        recover()
        scope.launch {
            realtime.events(currentUserId).collect(::handleRealtime)
        }
        scope.launch {
            platform.remoteParticipantCount.collect { count ->
                _state.value = _state.value.copy(remoteParticipantCount = count)
            }
        }
    }

    fun close() {
        presenceJob?.cancel()
        scope.cancel()
    }

    fun recover() {
        scope.launch {
            runCatching { api.active(instanceId) }.onSuccess { call ->
                if (call != null) activate(call)
            }
        }
    }

    fun startDirectCall(otherUserId: Int, type: String = "audio") {
        scope.launch {
            runCallRequest {
                api.start(
                    calleeIds = listOf(otherUserId),
                    clientInstanceId = instanceId,
                    type = type,
                    mode = "1v1",
                )
            }
        }
    }

    fun startGroupCall(calleeIds: List<Int>, type: String = "audio") {
        if (calleeIds.isEmpty()) return
        scope.launch {
            runCallRequest {
                api.start(
                    calleeIds = calleeIds,
                    clientInstanceId = instanceId,
                    type = type,
                    mode = "group",
                )
            }
        }
    }

    fun startFromConversation(
        participantIds: List<Int>,
        type: String = "audio",
        conversationId: Int? = null,
    ) {
        val calleeIds = participantIds.filter { it != currentUserId }
        if (calleeIds.isEmpty()) return
        scope.launch {
            runCallRequest {
                if (conversationId != null && calleeIds.size == 1 && type == "audio") {
                    runCatching {
                        api.startFromConversation(conversationId, instanceId)
                    }.getOrElse {
                        api.start(
                            calleeIds = calleeIds,
                            clientInstanceId = instanceId,
                            type = type,
                            mode = if (calleeIds.size > 1) "group" else "1v1",
                        )
                    }
                } else {
                    api.start(
                        calleeIds = calleeIds,
                        clientInstanceId = instanceId,
                        type = type,
                        mode = if (calleeIds.size > 1) "group" else "1v1",
                    )
                }
            }
        }
    }

    fun openFromNotification(callId: String?, action: String?) {
        scope.launch {
            val call = when {
                !callId.isNullOrBlank() -> runCatching { api.get(callId) }.getOrNull()
                    ?: runCatching { api.active(instanceId) }.getOrNull()
                else -> runCatching { api.active(instanceId) }.getOrNull()
            } ?: return@launch

            when (action) {
                "answer" -> runCallRequest { api.accept(call.id, instanceId) }
                "decline", "hangup" -> {
                    runCatching {
                        if (call.status == CallStatus.RINGING) api.decline(call.id) else api.end(call.id)
                    }
                    clearCall(call.id)
                }
                else -> activate(call)
            }
        }
    }

    fun accept() = action { call -> api.accept(call.id, instanceId) }
    fun decline() = finish { call -> api.decline(call.id) }
    fun cancel() = finish { call -> api.cancel(call.id) }
    fun leave() = finish { call -> api.leave(call.id) }
    fun end() = finish { call -> api.end(call.id) }

    fun invite(userIds: List<Int>) {
        val call = _state.value.call ?: return
        if (userIds.isEmpty()) return
        scope.launch {
            runCallRequest { api.invite(call.id, userIds) }
        }
    }

    fun toggleMute() {
        scope.launch {
            val next = !_state.value.muted
            platform.setMuted(next)
            _state.value = _state.value.copy(muted = next)
        }
    }

    fun toggleCamera() {
        scope.launch {
            val next = !_state.value.cameraEnabled
            platform.setCameraEnabled(next)
            _state.value = _state.value.copy(cameraEnabled = next)
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
            clearCall(call.id)
        }
    }

    private suspend fun runCallRequest(block: suspend () -> WorkspaceCall) {
        try {
            activate(block())
        } catch (error: CallApiException) {
            if (error.code == "ANSWERED_ELSEWHERE") {
                clearCall(_state.value.call?.id)
                _state.value = CallUiState(error = error.message)
            } else {
                _state.value = _state.value.copy(error = error.message)
            }
        } catch (error: Exception) {
            _state.value = _state.value.copy(error = error.message ?: "Call request failed.")
        }
    }

    private suspend fun activate(call: WorkspaceCall) {
        if (call.status.isTerminal) {
            clearCall(call.id)
            return
        }

        val incoming = call.status == CallStatus.RINGING && call.initiatedByUserId != currentUserId
        _state.value = _state.value.copy(
            call = call,
            error = null,
            cameraEnabled = call.isVideo && !incoming,
        )

        subscribePresence(call)

        if (incoming) {
            platform.showIncoming(call)
        } else if (call.connection != null) {
            connectMedia(call)
        }
    }

    private suspend fun connectMedia(call: WorkspaceCall) {
        _state.value = _state.value.copy(connectionLabel = "Connecting")
        platform.connect(call)
        if (call.isVideo) {
            platform.setCameraEnabled(true)
            _state.value = _state.value.copy(cameraEnabled = true)
        }
        _state.value = _state.value.copy(connectionLabel = "Connected")
    }

    private suspend fun handleRealtime(call: WorkspaceCall) {
        val current = _state.value.call
        if (current != null && current.id != call.id) return

        if (shouldDismissForOtherDevice(call)) {
            clearCall(call.id)
            _state.value = CallUiState(error = "Answered on another device.")
            return
        }

        if (call.status.isTerminal) {
            clearCall(call.id)
            return
        }

        val wasIncoming = current?.status == CallStatus.RINGING &&
            current.initiatedByUserId != currentUserId
        activate(call)

        if (!wasIncoming && call.status == CallStatus.ACTIVE && call.connection != null &&
            _state.value.connectionLabel != "Connected"
        ) {
            connectMedia(call)
        }
    }

    private fun shouldDismissForOtherDevice(call: WorkspaceCall): Boolean {
        if (call.status.isTerminal) return false
        if (call.initiatedByUserId == currentUserId) return false
        return call.status == CallStatus.ACTIVE &&
            call.answeredClientInstanceId != null &&
            call.answeredClientInstanceId != instanceId &&
            !call.isGroup
    }

    private fun subscribePresence(call: WorkspaceCall) {
        presenceJob?.cancel()
        if (!call.isGroup || presenceRealtime == null) return
        presenceJob = scope.launch {
            presenceRealtime.presenceEvents(call.id).collect { event ->
                if (_state.value.call?.id == event.call.id) {
                    _state.value = _state.value.copy(call = event.call)
                }
            }
        }
    }

    private suspend fun clearCall(callId: String?) {
        presenceJob?.cancel()
        presenceJob = null
        platform.disconnect()
        if (!callId.isNullOrBlank()) {
            platform.dismissIncoming(callId)
        }
        _state.value = CallUiState()
    }
}

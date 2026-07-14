package org.example.project.presentation.calls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.data.calls.ClientInstanceIdStorage
import org.example.project.domain.calls.CallApi
import org.example.project.domain.calls.CallApiException
import org.example.project.domain.calls.CallPermissions
import org.example.project.domain.calls.CallPresenceRealtimeApi
import org.example.project.domain.calls.CallRealtimeApi
import org.example.project.domain.calls.CallStatus
import org.example.project.domain.calls.PlatformCallController
import org.example.project.domain.calls.WorkspaceCall

data class CallUiState(
    val call: WorkspaceCall? = null,
    val muted: Boolean = false,
    val cameraEnabled: Boolean = false,
    val remoteCameraEnabled: Boolean = false,
    val connectionLabel: String = "",
    val elapsedSeconds: Int = 0,
    val remoteParticipantCount: Int = 0,
    val accepting: Boolean = false,
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
    private var durationJob: Job? = null
    private var onCallEnded: ((Int?) -> Unit)? = null
    private val connectMutex = Mutex()
    private var acceptingCall = false

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
        scope.launch {
            platform.remoteCameraEnabled.collect { enabled ->
                _state.value = _state.value.copy(remoteCameraEnabled = enabled)
            }
        }
    }

    fun setOnCallEnded(callback: (Int?) -> Unit) {
        onCallEnded = callback
    }

    fun close() {
        presenceJob?.cancel()
        durationJob?.cancel()
        scope.cancel()
    }

    fun recover() {
        scope.launch {
            runCatching { api.active(instanceId) }.onSuccess { call ->
                if (call == null) return@onSuccess
                val incoming = call.status == CallStatus.RINGING &&
                    call.initiatedByUserId != currentUserId
                if (incoming) {
                    activate(call)
                    return@onSuccess
                }
                if (call.status == CallStatus.ACTIVE && call.connection != null &&
                    canJoinActiveCall(call)
                ) {
                    activate(call)
                    connectMedia(call, permissionsAlreadyGranted = true)
                }
            }
        }
    }

    fun startDirectCall(otherUserId: Int, type: String = "audio") {
        scope.launch {
            if (!ensurePermissions(type)) return@launch
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
            if (!ensurePermissions(type)) return@launch
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
            if (!ensurePermissions(type)) return@launch
            runCallRequest {
                if (conversationId != null) {
                    api.startFromConversation(conversationId, instanceId, type)
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
                "answer" -> {
                    activate(call)
                    acceptIncomingCall()
                }
                "decline", "hangup" -> {
                    runCatching {
                        if (call.status == CallStatus.RINGING) api.decline(call.id) else api.end(call.id)
                    }
                    clearCall(call.id)
                }
                else -> {
                    activate(call)
                }
            }
        }
    }

    fun accept() {
        scope.launch { acceptIncomingCall() }
    }

    private suspend fun acceptIncomingCall() {
        val call = _state.value.call ?: return
        if (acceptingCall || call.status != CallStatus.RINGING) return

        acceptingCall = true
        _state.value = _state.value.copy(accepting = true, error = null)
        try {
            val mediaType = if (call.isVideo) "video" else "audio"
            if (!ensurePermissions(mediaType)) {
                return
            }
            val acceptedCall = api.accept(call.id, instanceId)
            joinAcceptedCall(acceptedCall)
        } catch (error: CallApiException) {
            if (error.code == "ANSWERED_ELSEWHERE") {
                clearCall(call.id)
                _state.value = CallUiState(error = error.message)
            } else {
                _state.value = _state.value.copy(error = error.message)
            }
        } catch (error: Exception) {
            _state.value = _state.value.copy(
                error = error.message ?: "Could not answer call.",
            )
        } finally {
            acceptingCall = false
            _state.value = _state.value.copy(accepting = false)
        }
    }

    private suspend fun joinAcceptedCall(call: WorkspaceCall) {
        _state.value = _state.value.copy(
            call = call,
            error = null,
            cameraEnabled = if (call.isVideo) CallPermissions.hasCamera() else false,
            connectionLabel = buildConnectionLabel(call, ""),
        )
        subscribePresence(call)
        if (call.connection != null) {
            connectMedia(call, permissionsAlreadyGranted = true)
        }
    }

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
            runCatching { platform.setMuted(next) }
                .onSuccess {
                    _state.value = _state.value.copy(muted = platform.isMuted())
                }
                .onFailure {
                    _state.value = _state.value.copy(error = it.message ?: "Could not change microphone.")
                }
        }
    }

    fun toggleCamera() {
        scope.launch {
            val next = !_state.value.cameraEnabled
            if (next && !CallPermissions.hasCamera()) {
                if (!CallPermissions.ensureForCall("video")) {
                    _state.value = _state.value.copy(
                        error = "Camera permission is required for video calls.",
                    )
                    return@launch
                }
            }
            runCatching { platform.setCameraEnabled(next) }
                .onSuccess {
                    _state.value = _state.value.copy(cameraEnabled = platform.isCameraEnabled())
                }
                .onFailure {
                    _state.value = _state.value.copy(error = it.message ?: "Could not change camera.")
                }
        }
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
            cameraEnabled = if (incoming) false else call.isVideo,
            connectionLabel = buildConnectionLabel(call, ""),
        )

        subscribePresence(call)

        if (incoming) {
            platform.showIncoming(call)
            return
        } else if (call.connection != null) {
            connectMedia(call)
        }
    }

    private suspend fun connectMedia(call: WorkspaceCall, permissionsAlreadyGranted: Boolean = false) {
        connectMutex.withLock {
            if (_state.value.connectionLabel == "Connected") return
            if (!permissionsAlreadyGranted) {
                if (!ensurePermissions(if (call.isVideo) "video" else "audio")) return
            } else if (!hasRequiredPermissions(call)) {
                return
            }
            _state.value = _state.value.copy(connectionLabel = "Connecting…")
            try {
                platform.connect(call)
                if (call.isVideo && CallPermissions.hasCamera()) {
                    platform.setCameraEnabled(true)
                }
                _state.value = _state.value.copy(
                    connectionLabel = "Connected",
                    muted = platform.isMuted(),
                    cameraEnabled = platform.isCameraEnabled(),
                )
                startDurationTimer()
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    connectionLabel = "Connection failed",
                    error = error.message ?: "Could not connect to call.",
                )
            }
        }
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
            clearCall(call.id, call.conversationId)
            return
        }

        activate(call)

        if (acceptingCall) return

        val incomingRinging = call.status == CallStatus.RINGING &&
            call.initiatedByUserId != currentUserId
        if (incomingRinging) return

        if (call.status == CallStatus.ACTIVE && call.connection != null &&
            _state.value.connectionLabel != "Connected" &&
            canJoinActiveCall(call) &&
            hasRequiredPermissions(call)
        ) {
            connectMedia(call, permissionsAlreadyGranted = true)
        }
    }

    private fun canJoinActiveCall(call: WorkspaceCall): Boolean {
        if (call.initiatedByUserId == currentUserId) return true
        return call.answeredClientInstanceId == instanceId
    }

    private fun hasRequiredPermissions(call: WorkspaceCall): Boolean {
        if (!CallPermissions.hasAudio()) return false
        if (call.isVideo && !CallPermissions.hasCamera()) return false
        return true
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

    private suspend fun clearCall(callId: String?, fallbackConversationId: Int? = null) {
        val conversationId = _state.value.call?.conversationId ?: fallbackConversationId
        presenceJob?.cancel()
        presenceJob = null
        durationJob?.cancel()
        durationJob = null
        platform.disconnect()
        if (!callId.isNullOrBlank()) {
            platform.dismissIncoming(callId)
        }
        _state.value = CallUiState()
        conversationId?.let { onCallEnded?.invoke(it) }
    }

    private suspend fun ensurePermissions(type: String): Boolean {
        if (CallPermissions.ensureForCall(type)) return true
        val message = if (type == "video") {
            "Camera and microphone permissions are required for video calls."
        } else {
            "Microphone permission is required for calls."
        }
        _state.value = _state.value.copy(error = message)
        return false
    }

    private fun buildConnectionLabel(call: WorkspaceCall, mediaState: String): String {
        return when {
            mediaState.isNotBlank() -> mediaState
            call.status == CallStatus.RINGING && call.initiatedByUserId == currentUserId -> "Calling…"
            call.status == CallStatus.RINGING -> "Ringing…"
            call.status == CallStatus.ACTIVE && call.connection == null -> "Connecting…"
            else -> ""
        }
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = scope.launch {
            var seconds = _state.value.elapsedSeconds
            while (true) {
                delay(1_000)
                seconds += 1
                if (_state.value.call?.status != CallStatus.ACTIVE) break
                _state.value = _state.value.copy(
                    elapsedSeconds = seconds,
                    connectionLabel = formatDuration(seconds),
                )
            }
        }
    }

    private fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

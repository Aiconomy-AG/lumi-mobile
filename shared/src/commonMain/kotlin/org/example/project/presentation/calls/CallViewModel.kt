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
import org.example.project.domain.calls.CallMediaParticipant
import org.example.project.domain.calls.CallPermissions
import org.example.project.domain.calls.CallPermissionPolicy
import org.example.project.domain.calls.CallPermissionState
import org.example.project.domain.calls.CallPresenceRealtimeApi
import org.example.project.domain.calls.CallRealtimeApi
import org.example.project.domain.calls.CallStatus
import org.example.project.domain.calls.PlatformCallController
import org.example.project.domain.calls.WorkspaceCall

data class CallUiState(
    val call: WorkspaceCall? = null,
    val uiMode: CallUiMode = CallUiMode.Hidden,
    val muted: Boolean = false,
    val cameraEnabled: Boolean = false,
    val remoteCameraEnabled: Boolean = false,
    val connectionLabel: String = "",
    val elapsedSeconds: Int = 0,
    val remoteParticipantCount: Int = 0,
    val mediaParticipants: List<CallMediaParticipant> = emptyList(),
    val accepting: Boolean = false,
    val permissionState: CallPermissionState = CallPermissionState.DENIED,
    val error: String? = null,
)

class CallViewModel(
    private val currentUserId: Int,
    private val api: CallApi,
    private val realtime: CallRealtimeApi,
    private val presenceRealtime: CallPresenceRealtimeApi?,
    private val platform: PlatformCallController,
    clientInstanceId: String = ClientInstanceIdStorage.getOrCreate(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val instanceId = clientInstanceId
    private val _state = MutableStateFlow(emptyCallState())
    val state: StateFlow<CallUiState> = _state.asStateFlow()
    private var presenceJob: Job? = null
    private var durationJob: Job? = null
    private var onCallEnded: ((Int?) -> Unit)? = null
    private val connectMutex = Mutex()
    private val acceptMutex = Mutex()
    private var acceptingCall = false
    private var callerSyncJob: Job? = null
    private val incomingPresentedCallIds = mutableSetOf<String>()

    init {
        recover()
        scope.launch {
            realtime.events(currentUserId).collect { call ->
                runCatching { handleRealtime(call) }
            }
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
        scope.launch {
            platform.mediaParticipants.collect { participants ->
                _state.value = _state.value.copy(mediaParticipants = participants)
            }
        }
        scope.launch {
            CallPermissions.state.collect { permissionState ->
                _state.value = _state.value.copy(permissionState = permissionState)
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
                    connectMedia(call)
                }
            }
        }
    }

    fun startDirectCall(otherUserId: Int, type: String = "audio") {
        scope.launch {
            if (!ensureCallMediaPermissions()) return@launch
            runCallRequest {
                validateStartedCall(api.start(
                    calleeIds = listOf(otherUserId),
                    clientInstanceId = instanceId,
                    type = type,
                    mode = "1v1",
                ), type)
            }
        }
    }

    fun startGroupCall(calleeIds: List<Int>, type: String = "audio") {
        if (calleeIds.isEmpty()) return
        scope.launch {
            if (!ensureCallMediaPermissions()) return@launch
            runCallRequest {
                validateStartedCall(api.start(
                    calleeIds = calleeIds,
                    clientInstanceId = instanceId,
                    type = type,
                    mode = "group",
                ), type)
            }
        }
    }

    fun startFromConversation(
        participantIds: List<Int>,
        type: String = "audio",
        conversationId: Int? = null,
        isGroupConversation: Boolean = false,
    ) {
        val calleeIds = participantIds.filter { it != currentUserId }
        if (calleeIds.isEmpty()) return
        scope.launch {
            if (!ensureCallMediaPermissions()) return@launch
            runCallRequest {
                val startedCall = if (shouldUseConversationEndpoint(
                    conversationId = conversationId,
                    calleeCount = calleeIds.size,
                    isGroupConversation = isGroupConversation,
                    type = type,
                )) {
                    api.startFromConversation(conversationId!!, instanceId, type)
                } else {
                    api.start(
                        calleeIds = calleeIds,
                        clientInstanceId = instanceId,
                        type = type,
                        mode = if (calleeIds.size > 1 || isGroupConversation) "group" else "1v1",
                    )
                }
                validateStartedCall(startedCall, type)
            }
        }
    }

    fun minimize() {
        val call = _state.value.call ?: return
        if (call.status == CallStatus.ACTIVE) {
            _state.value = _state.value.copy(uiMode = CallUiMode.Minimized)
        }
    }

    fun expand() {
        if (_state.value.call != null) {
            _state.value = _state.value.copy(uiMode = CallUiMode.FullScreen)
        }
    }

    fun openFromNotification(callId: String?, action: String?) {
        scope.launch {
            val call = when {
                !callId.isNullOrBlank() -> runCatching { api.get(callId, instanceId) }.getOrNull()
                    ?: runCatching { api.active(instanceId) }.getOrNull()
                else -> runCatching { api.active(instanceId) }.getOrNull()
            } ?: return@launch

            when (action) {
                "answer" -> {
                    applyCallToState(call)
                    if (call.status == CallStatus.ACTIVE && call.answeredClientInstanceId == instanceId) {
                        joinAcceptedCall(call)
                    } else {
                        acceptIncomingCall()
                    }
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

    fun openPermissionSettings() {
        CallPermissions.openAppSettings()
    }

    private suspend fun acceptIncomingCall() {
        val call = _state.value.call ?: return
        if (call.status != CallStatus.RINGING || !acceptMutex.tryLock()) return

        acceptingCall = true
        _state.value = _state.value.copy(accepting = true, error = null)
        try {
            prepareIncomingAnswer(call.id)
            if (!ensureCallMediaPermissions()) {
                return
            }
            val acceptedCall = api.accept(call.id, instanceId)
            joinAcceptedCall(acceptedCall)
        } catch (error: CallApiException) {
            if (error.code == "ANSWERED_ELSEWHERE") {
                clearCall(call.id)
                _state.value = emptyCallState(error.message)
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
            acceptMutex.unlock()
        }
    }

    private fun prepareIncomingAnswer(callId: String) {
        platform.onIncomingAnswered(callId)
    }

    private suspend fun joinAcceptedCall(call: WorkspaceCall) {
        var resolvedCall = call
        var attempts = 0
        while (resolvedCall.connection == null && attempts < 8) {
            delay(300)
            val activeCall = runCatching { api.active(instanceId) }.getOrNull()
            if (activeCall?.id == call.id) resolvedCall = activeCall
            attempts += 1
        }
        applyCallToState(resolvedCall)
        subscribePresence(resolvedCall)
        if (resolvedCall.connection != null) {
            connectMedia(resolvedCall)
        } else {
            _state.value = _state.value.copy(
                connectionLabel = "Connection failed",
                error = "The call was accepted but no media connection was provided.",
            )
        }
    }

    fun decline() = finish { call -> api.decline(call.id) }
    fun cancel() = finish { call -> api.cancel(call.id) }
    fun leave() = finish { call -> api.leave(call.id) }

    fun end() {
        val call = _state.value.call ?: return
        if (call.isGroup && call.status == CallStatus.ACTIVE) {
            leave()
            return
        }
        finish { api.end(it.id) }
    }

    fun invite(userIds: List<Int>) {
        val call = _state.value.call ?: return
        if (userIds.isEmpty()) return
        scope.launch {
            runCatching { api.invite(call.id, userIds) }
                .onSuccess { updated ->
                    applyCallToState(updated)
                }
                .onFailure { error ->
                    val message = when (error) {
                        is CallApiException -> error.message
                        else -> error.message
                    }
                    _state.value = _state.value.copy(error = message ?: "Could not invite participants.")
                }
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
                if (!CallPermissions.ensureForCall("camera")) {
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
                _state.value = emptyCallState(error.message)
            } else {
                _state.value = _state.value.copy(error = error.message)
            }
        } catch (error: Exception) {
            _state.value = _state.value.copy(error = error.message ?: "Call request failed.")
        }
    }

    private fun validateStartedCall(call: WorkspaceCall, requestedType: String): WorkspaceCall {
        if (!CallPermissionPolicy.matchesRequestedMedia(
                requestedType = requestedType,
                returnedType = call.type,
                returnedMediaType = call.mediaType,
            )
        ) {
            throw CallApiException(
                "The server returned an audio call for a video request.",
                "INVALID_CALL_MEDIA_TYPE",
            )
        }
        return call
    }

    private suspend fun activate(call: WorkspaceCall) {
        if (call.status.isTerminal) {
            clearCall(call.id)
            return
        }

        val incoming = call.status == CallStatus.RINGING && call.initiatedByUserId != currentUserId
        applyCallToState(call)

        subscribePresence(call)

        if (incoming) {
            if (incomingPresentedCallIds.add(call.id)) {
                runCatching { platform.showIncoming(call) }
            }
            return
        }

        if (call.initiatedByUserId == currentUserId && call.status == CallStatus.RINGING) {
            startCallerSyncPolling(call)
        } else {
            callerSyncJob?.cancel()
            callerSyncJob = null
        }

        if (call.connection != null) {
            connectMedia(call)
        }
    }

    private fun applyCallToState(call: WorkspaceCall) {
        val currentMode = _state.value.uiMode
        _state.value = _state.value.copy(
            call = call,
            uiMode = resolveUiMode(call, currentMode),
            error = null,
            cameraEnabled = if (call.status == CallStatus.RINGING && call.initiatedByUserId != currentUserId) {
                false
            } else {
                call.isVideo && CallPermissions.hasCamera()
            },
            connectionLabel = buildConnectionLabel(call, _state.value.connectionLabel),
        )
    }

    private fun resolveUiMode(call: WorkspaceCall, current: CallUiMode): CallUiMode {
        if (current == CallUiMode.Minimized && call.status == CallStatus.ACTIVE) {
            return CallUiMode.Minimized
        }
        return when {
            call.status == CallStatus.RINGING && call.initiatedByUserId != currentUserId ->
                CallUiMode.Incoming
            call.status == CallStatus.RINGING && call.initiatedByUserId == currentUserId && call.isGroup ->
                CallUiMode.OutgoingRinging
            call.status == CallStatus.RINGING ->
                CallUiMode.FullScreen
            call.status == CallStatus.ACTIVE ->
                if (current == CallUiMode.Minimized) CallUiMode.Minimized else CallUiMode.FullScreen
            else -> CallUiMode.Hidden
        }
    }

    private fun startCallerSyncPolling(call: WorkspaceCall) {
        if (call.initiatedByUserId != currentUserId || call.status != CallStatus.RINGING) return
        callerSyncJob?.cancel()
        callerSyncJob = scope.launch {
            repeat(30) {
                delay(1_000)
                val current = _state.value.call ?: return@launch
                if (current.id != call.id || current.status != CallStatus.RINGING) return@launch
                val fresh = runCatching { api.get(call.id, instanceId) }.getOrNull() ?: return@repeat
                if (fresh.status != CallStatus.RINGING) {
                    handleRealtime(fresh)
                }
            }
        }
    }

    private suspend fun connectMedia(call: WorkspaceCall) {
        connectMutex.withLock {
            if (_state.value.connectionLabel == "Connected") return
            if (!hasAllCallPermissions()) {
                _state.value = _state.value.copy(
                    error = "Camera and microphone permissions are required for calls.",
                )
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
            _state.value = emptyCallState("Answered on another device.")
            return
        }

        if (call.status.isTerminal) {
            clearCall(call.id, call.conversationId)
            return
        }

        activate(call)

        if (isAnsweringCallee(call)) {
            prepareIncomingAnswer(call.id)
        }

        if (acceptingCall) return

        val incomingRinging = call.status == CallStatus.RINGING &&
            call.initiatedByUserId != currentUserId
        if (incomingRinging) return

        if (call.status == CallStatus.ACTIVE && call.connection != null &&
            _state.value.connectionLabel != "Connected" &&
            canJoinActiveCall(call) &&
            hasAllCallPermissions()
        ) {
            connectMedia(call)
        }
    }

    private fun canJoinActiveCall(call: WorkspaceCall): Boolean {
        if (call.initiatedByUserId == currentUserId) return true
        return call.answeredClientInstanceId == instanceId
    }

    private fun isAnsweringCallee(call: WorkspaceCall): Boolean {
        return call.status == CallStatus.ACTIVE &&
            call.answeredClientInstanceId == instanceId &&
            call.initiatedByUserId != currentUserId
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
        callerSyncJob?.cancel()
        callerSyncJob = null
        if (!callId.isNullOrBlank()) {
            incomingPresentedCallIds.remove(callId)
        }
        platform.disconnect()
        if (!callId.isNullOrBlank()) {
            platform.dismissIncoming(callId)
        }
        _state.value = emptyCallState()
        conversationId?.let { onCallEnded?.invoke(it) }
    }

    private suspend fun ensureCallMediaPermissions(): Boolean {
        if (CallPermissions.ensureForCall("video") && hasAllCallPermissions()) return true
        val message = if (CallPermissions.state.value == CallPermissionState.PERMANENTLY_DENIED) {
            "Camera and microphone permissions are disabled. Enable them in App Settings."
        } else {
            "Camera and microphone permissions are required for calls."
        }
        _state.value = _state.value.copy(error = message)
        return false
    }

    private fun hasAllCallPermissions(): Boolean = CallPermissionPolicy.canUseCalls(
        state = CallPermissions.state.value,
        hasAudio = CallPermissions.hasAudio(),
        hasCamera = CallPermissions.hasCamera(),
    )

    private fun buildConnectionLabel(call: WorkspaceCall, mediaState: String): String {
        val effectiveMediaState = if (
            call.status == CallStatus.ACTIVE &&
            (mediaState.contains("Calling", ignoreCase = true) || mediaState.contains("Ringing", ignoreCase = true))
        ) {
            ""
        } else {
            mediaState
        }
        return when {
            effectiveMediaState.isNotBlank() -> effectiveMediaState
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

    private fun emptyCallState(error: String? = null) = CallUiState(
        uiMode = CallUiMode.Hidden,
        permissionState = CallPermissions.state.value,
        error = error,
    )
}

internal fun shouldUseConversationEndpoint(
    conversationId: Int?,
    calleeCount: Int,
    isGroupConversation: Boolean,
    type: String,
): Boolean {
    return conversationId != null && (
        isGroupConversation ||
        calleeCount > 1 ||
        (calleeCount == 1 && type == "audio")
    )
}

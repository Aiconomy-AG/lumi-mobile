package org.example.project.domain.calls

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.DisconnectCause
import androidx.core.telecom.CallAttributesCompat
import androidx.core.telecom.CallControlScope
import androidx.core.telecom.CallsManager
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.notifications.IncomingCallRingingService

object AndroidCallRuntime {
    private var applicationContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var callsManager: CallsManager? = null
    private val controls = mutableMapOf<String, CallControlScope>()

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && callsManager == null) {
            callsManager = CallsManager(context.applicationContext).also {
                it.registerAppWithTelecom(CallsManager.CAPABILITY_BASELINE)
            }
        }
    }

    internal fun context(): Context = checkNotNull(applicationContext) { "AndroidCallRuntime is not initialized." }

    fun reportIncoming(callId: String, callerUserId: String, callerName: String, isVideo: Boolean = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || controls.containsKey(callId)) return
        val manager = callsManager ?: return
        scope.launch {
            runCatching {
                manager.addCall(
                    CallAttributesCompat(
                        callerName,
                        Uri.parse("sip:lumi-user-$callerUserId@lumi.internal"),
                        CallAttributesCompat.DIRECTION_INCOMING,
                        if (isVideo) {
                            CallAttributesCompat.CALL_TYPE_VIDEO_CALL
                        } else {
                            CallAttributesCompat.CALL_TYPE_AUDIO_CALL
                        },
                        0,
                    ),
                    onAnswer = { openCallAction(callId, "answer") },
                    onDisconnect = { openCallAction(callId, "decline") },
                    onSetActive = {},
                    onSetInactive = {},
                ) { controls[callId] = this }
            }
        }
    }

    fun dismiss(callId: String) {
        val control = controls.remove(callId) ?: return
        scope.launch { control.disconnect(DisconnectCause(DisconnectCause.REMOTE)) }
        IncomingCallRingingService.stop(context(), callId)
    }

    fun openCallAction(callId: String, action: String) {
        IncomingCallRingingService.stop(context(), callId)
        val intent = Intent(context(), IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("type", "workspace_call_incoming")
            putExtra("call_id", callId)
            if (action.isNotEmpty()) putExtra("call_action", action)
        }
        context().startActivity(intent)
    }
}

private class AndroidLiveKitCallController : PlatformCallController {
    private var room: Room? = null
    private val mediaScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _remoteParticipantCount = MutableStateFlow(0)
    override val remoteParticipantCount: StateFlow<Int> = _remoteParticipantCount.asStateFlow()

    init {
        mediaScope.launch {
            AndroidLiveKitRoomHolder.remoteParticipantCount.collect {
                _remoteParticipantCount.value = it
            }
        }
    }

    override suspend fun connect(call: WorkspaceCall) {
        val connection = call.connection ?: return
        withContext(Dispatchers.Main) {
            disconnect()
            room = LiveKit.create(AndroidCallRuntime.context()).also { connectedRoom ->
                connectedRoom.connect(connection.url, connection.token)
                connectedRoom.localParticipant.setMicrophoneEnabled(true)
                if (call.isVideo) {
                    connectedRoom.localParticipant.setCameraEnabled(true)
                }
                AndroidLiveKitRoomHolder.attach(connectedRoom, mediaScope)
            }
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.Main) {
            room?.disconnect()
            room = null
            AndroidLiveKitRoomHolder.clear()
        }
    }

    override suspend fun setMuted(muted: Boolean) {
        withContext(Dispatchers.Main) {
            room?.localParticipant?.setMicrophoneEnabled(!muted)
        }
    }

    override suspend fun setCameraEnabled(enabled: Boolean) {
        withContext(Dispatchers.Main) {
            room?.localParticipant?.setCameraEnabled(enabled)
            room?.let { AndroidLiveKitRoomHolder.refresh(it) }
        }
    }

    override fun isMuted(): Boolean {
        val publication = room?.localParticipant
            ?.getTrackPublication(io.livekit.android.room.track.Track.Source.MICROPHONE)
            ?: return false
        return publication.muted
    }

    override fun isCameraEnabled(): Boolean {
        val publication = room?.localParticipant
            ?.getTrackPublication(io.livekit.android.room.track.Track.Source.CAMERA)
            ?: return false
        return publication.track != null && !publication.muted
    }

    override fun showIncoming(call: WorkspaceCall) {
        AndroidCallRuntime.reportIncoming(
            call.id,
            call.caller.id.toString(),
            call.caller.name,
            call.isVideo,
        )
    }

    override fun dismissIncoming(callId: String) = AndroidCallRuntime.dismiss(callId)
}

actual fun createPlatformCallController(): PlatformCallController = AndroidLiveKitCallController()

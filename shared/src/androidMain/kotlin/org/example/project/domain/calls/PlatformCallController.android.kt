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
import kotlinx.coroutines.launch

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

    fun reportIncoming(callId: String, callerName: String, phoneNumber: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || controls.containsKey(callId)) return
        val manager = callsManager ?: return
        scope.launch {
            runCatching {
                manager.addCall(
                    CallAttributesCompat(
                        callerName,
                        Uri.parse("lumi:$phoneNumber"),
                        CallAttributesCompat.DIRECTION_INCOMING,
                        CallAttributesCompat.CALL_TYPE_AUDIO_CALL,
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
    }

    private fun openCallAction(callId: String, action: String) {
        context().packageManager.getLaunchIntentForPackage(context().packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("type", "workspace_call_incoming")
            putExtra("call_id", callId)
            putExtra("call_action", action)
        }?.let(context()::startActivity)
    }
}

private class AndroidLiveKitCallController : PlatformCallController {
    private var room: Room? = null

    override suspend fun connect(call: WorkspaceCall) {
        val connection = call.connection ?: return
        disconnect()
        room = LiveKit.create(AndroidCallRuntime.context()).also {
            it.connect(connection.url, connection.token)
            it.localParticipant.setMicrophoneEnabled(true)
        }
    }

    override suspend fun disconnect() {
        room?.disconnect()
        room = null
    }

    override suspend fun setMuted(muted: Boolean) {
        room?.localParticipant?.setMicrophoneEnabled(!muted)
    }

    override fun showIncoming(call: WorkspaceCall) {
        AndroidCallRuntime.reportIncoming(call.id, call.caller.name, call.caller.phoneNumber)
    }
    override fun dismissIncoming(callId: String) = AndroidCallRuntime.dismiss(callId)
}

actual fun createPlatformCallController(): PlatformCallController = AndroidLiveKitCallController()

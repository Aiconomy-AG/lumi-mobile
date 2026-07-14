package org.example.project.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import org.example.project.domain.calls.AndroidCallRuntime
import org.example.project.domain.calls.IncomingCallActivity

class IncomingCallRingingService : Service() {
    private var ringtone: Ringtone? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable { stopSelf() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val callId = intent?.getStringExtra(EXTRA_CALL_ID).orEmpty()
        if (callId.isBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val callerName = intent?.getStringExtra(EXTRA_CALLER_NAME).orEmpty().ifBlank { "Incoming call" }
        val callerUserId = intent?.getStringExtra(EXTRA_CALLER_USER_ID).orEmpty()
        val isVideo = intent?.getBooleanExtra(EXTRA_IS_VIDEO, false) == true
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: callerName
        val body = intent?.getStringExtra(EXTRA_BODY) ?: if (isVideo) "Incoming video call" else "Incoming audio call"

        NotificationChannels.create(this)
        AndroidCallRuntime.initialize(this)
        AndroidCallRuntime.reportIncoming(callId, callerUserId, callerName, isVideo)

        val notification = buildNotification(callId, callerName, title, body, intent?.extras)
        startForeground(notificationId(callId), notification)
        startRinging()

        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, RING_TIMEOUT_MS)

        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeoutRunnable)
        stopRinging()
        val callId = currentCallId
        if (!callId.isNullOrBlank()) {
            getSystemService(NotificationManager::class.java)?.cancel(notificationId(callId))
        }
        currentCallId = null
        super.onDestroy()
    }

    private fun buildNotification(
        callId: String,
        callerName: String,
        title: String,
        body: String,
        extras: android.os.Bundle?,
    ): Notification {
        val data = mutableMapOf(
            "type" to "workspace_call_incoming",
            "call_id" to callId,
            "caller_name" to callerName,
        )
        extras?.keySet()?.forEach { key ->
            extras.getString(key)?.let { data[key] = it }
        }

        val answerIntent = callPendingIntent(data, "answer")
        val declineIntent = callPendingIntent(data, "decline")
        val viewIntent = callPendingIntent(data, "")
        val caller = Person.Builder().setName(callerName).setImportant(true).build()

        return NotificationCompat.Builder(this, NotificationChannels.CALL_CHANNEL_ID)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(viewIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, declineIntent, answerIntent))
            .setFullScreenIntent(viewIntent, true)
            .build()
    }

    private fun callPendingIntent(data: Map<String, String>, action: String): PendingIntent {
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
            if (action.isNotEmpty()) putExtra("call_action", action)
        }
        return PendingIntent.getActivity(
            this,
            (data["call_id"].orEmpty() + action).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun startRinging() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, uri)?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLooping = true
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
            }
            play()
        }
        vibrate()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = getSystemService(VibratorManager::class.java)?.defaultVibrator
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0),
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
        }
    }

    private fun stopRinging() {
        ringtone?.stop()
        ringtone = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator?.cancel()
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(VIBRATOR_SERVICE) as? Vibrator)?.cancel()
        }
    }

    companion object {
        private const val ACTION_STOP = "org.example.project.STOP_RINGING"
        private const val EXTRA_CALL_ID = "call_id"
        private const val EXTRA_CALLER_NAME = "caller_name"
        private const val EXTRA_CALLER_USER_ID = "caller_user_id"
        private const val EXTRA_IS_VIDEO = "is_video"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_BODY = "body"
        private const val RING_TIMEOUT_MS = 45_000L

        private var currentCallId: String? = null

        fun start(context: Context, data: Map<String, String>) {
            val callId = data["call_id"] ?: return
            currentCallId = callId
            val intent = Intent(context, IncomingCallRingingService::class.java).apply {
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_CALLER_NAME, data["caller_name"])
                putExtra(EXTRA_CALLER_USER_ID, data["caller_user_id"])
                putExtra(EXTRA_IS_VIDEO, data["call_type"] == "video")
                putExtra(EXTRA_TITLE, data["title"])
                putExtra(EXTRA_BODY, data["body"])
                data.forEach { (key, value) -> putExtra(key, value) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context, callId: String?) {
            if (callId.isNullOrBlank()) return
            val intent = Intent(context, IncomingCallRingingService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_CALL_ID, callId)
            }
            context.startService(intent)
        }

        private fun notificationId(callId: String): Int = ("ring-$callId").hashCode()
    }
}
